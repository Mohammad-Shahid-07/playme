package com.playme.player.core.repository

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import com.playme.player.core.cache.LibrarySnapshotStore
import com.playme.player.core.cache.MediaMetadataStore
import com.playme.player.core.grouping.SeriesGrouper
import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.LibraryPhase
import com.playme.player.core.model.LibrarySnapshot
import com.playme.player.core.model.SeriesItem
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoItem
import com.playme.player.core.permissions.StorageAccess
import com.playme.player.core.scanner.DeepStorageScanner
import com.playme.player.core.scanner.MediaStoreScanner
import com.playme.player.core.util.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import java.io.File

/**
 * Single source of truth for the library.
 *
 * The scan is published in stages — cached snapshot, MediaStore, then the
 * recursive storage walk — so the UI never blocks on the slowest source.
 */
class VideoRepository(private val context: Context) {

    private val mediaStoreScanner = MediaStoreScanner(context)
    private val snapshotStore = LibrarySnapshotStore(context)
    private val metadataStore = MediaMetadataStore(context)
    private val deepStorageScanner = DeepStorageScanner(metadataStore)

    fun observeLibrary(): Flow<LibrarySnapshot> = channelFlow {
        if (!StorageAccess.hasLibraryAccess(context)) {
            send(snapshotOf(emptyList(), LibraryPhase.COMPLETE))
            return@channelFlow
        }

        val cached = snapshotStore.read().filterExisting()
        if (cached.isNotEmpty()) send(snapshotOf(cached, LibraryPhase.CACHE))

        val mediaStoreVideos = mediaStoreScanner.scanMediaStoreVideos()
        var merged = merge(mediaStoreVideos, cached)
        send(snapshotOf(merged, LibraryPhase.MEDIA_STORE))

        if (StorageAccess.hasDeepScanAccess(context)) {
            metadataStore.load()
            val indexed = mediaStoreVideos.mapNotNull { it.path.ifBlank { null } }.toHashSet()
            val discovered = ArrayList<VideoItem>()
            var lastEmit = 0L

            deepStorageScanner.scan(indexed) { item ->
                discovered += item
                val now = SystemClock.elapsedRealtime()
                if (now - lastEmit >= EMIT_INTERVAL_MS) {
                    lastEmit = now
                    trySend(snapshotOf(merge(mediaStoreVideos + discovered, cached), LibraryPhase.DEEP_SCAN))
                }
            }

            merged = merge(mediaStoreVideos + discovered, cached)
            metadataStore.flush()
        }

        snapshotStore.write(merged)
        send(snapshotOf(merged, LibraryPhase.COMPLETE))
    }
        .buffer(Channel.CONFLATED)
        .flowOn(Dispatchers.IO)

    /**
     * Fires when MediaStore gains or loses a video, so a file copied onto the
     * phone shows up without restarting the app. Debounced because a single
     * copy can produce a burst of change notifications.
     */
    @OptIn(FlowPreview::class)
    fun observeMediaChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(Unit)
            }
        }
        val registered = runCatching {
            context.contentResolver.registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
        }.isSuccess
        awaitClose {
            if (registered) {
                runCatching { context.contentResolver.unregisterContentObserver(observer) }
            }
        }
    }.debounce(MEDIA_CHANGE_DEBOUNCE_MS)

    fun groupIntoFolders(videos: List<VideoItem>): List<FolderItem> {
        val folderMap = mutableMapOf<String, MutableList<VideoItem>>()

        for (video in videos) {
            val folderPath = if (video.path.isNotEmpty()) {
                File(video.path).parentFile?.absolutePath ?: INTERNAL_STORAGE
            } else {
                INTERNAL_STORAGE
            }
            folderMap.getOrPut(folderPath) { mutableListOf() }.add(video)
        }

        return folderMap.map { (path, items) ->
            val name = if (path == INTERNAL_STORAGE) INTERNAL_STORAGE else File(path).name
            val totalBytes = items.sumOf { it.sizeBytes }
            FolderItem(
                id = path.hashCode().toString(),
                name = name,
                path = path,
                videoCount = items.size,
                totalSizeBytes = totalBytes,
                totalSizeFormatted = formatBytes(totalBytes),
                thumbnailUrl = items.firstOrNull()?.thumbnailUrl
            )
        }.sortedByDescending { it.videoCount }
    }

    fun sortVideos(videos: List<VideoItem>, sortOption: SortOption): List<VideoItem> =
        when (sortOption) {
            SortOption.NEWEST -> videos.sortedByDescending { it.dateModifiedMs }
            SortOption.NAME -> videos.sortedBy { it.title.lowercase() }
            SortOption.SIZE -> videos.sortedByDescending { it.sizeBytes }
            SortOption.DURATION -> videos.sortedByDescending { it.durationMs }
        }

    fun sortSeries(series: List<SeriesItem>, sortOption: SortOption): List<SeriesItem> =
        when (sortOption) {
            SortOption.NEWEST -> series.sortedByDescending { it.latestModifiedMs }
            SortOption.NAME -> series.sortedBy { it.title.lowercase() }
            SortOption.SIZE -> series.sortedByDescending { it.totalSizeBytes }
            SortOption.DURATION -> series.sortedByDescending { it.totalDurationMs }
        }

    fun searchVideos(videos: List<VideoItem>, query: String): List<VideoItem> {
        if (query.isBlank()) return videos
        val clean = query.trim().lowercase()
        return videos.filter {
            it.title.lowercase().contains(clean) || it.parentFolder.lowercase().contains(clean)
        }
    }

    fun searchSeries(series: List<SeriesItem>, query: String): List<SeriesItem> {
        if (query.isBlank()) return series
        val clean = query.trim().lowercase()
        return series.filter { it.title.lowercase().contains(clean) }
    }

    private fun snapshotOf(videos: List<VideoItem>, phase: LibraryPhase): LibrarySnapshot =
        LibrarySnapshot(
            videos = videos,
            phase = phase,
            series = SeriesGrouper.group(videos)
        )

    /** Fresh results win; the cached snapshot only fills the gaps. */
    private fun merge(primary: List<VideoItem>, fallback: List<VideoItem>): List<VideoItem> {
        val seen = HashSet<String>(primary.size + fallback.size)
        val result = ArrayList<VideoItem>(primary.size + fallback.size)
        primary.forEach { if (seen.add(it.dedupeKey())) result += it }
        fallback.forEach { if (seen.add(it.dedupeKey())) result += it }
        return result
    }

    /** Drops cached rows whose file was deleted since the last scan. */
    private fun List<VideoItem>.filterExisting(): List<VideoItem> =
        filter { it.path.isBlank() || File(it.path).exists() }

    private fun VideoItem.dedupeKey(): String = path.ifBlank { id }

    private companion object {
        const val INTERNAL_STORAGE = "Internal Storage"
        const val EMIT_INTERVAL_MS = 800L
        const val MEDIA_CHANGE_DEBOUNCE_MS = 1_200L
    }
}
