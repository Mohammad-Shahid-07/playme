package com.playme.player.core.scanner

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import com.playme.player.core.cache.MediaMetadataStore
import com.playme.player.core.model.BadgeVariant
import com.playme.player.core.model.VideoBadge
import com.playme.player.core.model.VideoItem
import com.playme.player.core.util.formatBytes
import com.playme.player.core.util.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext

/**
 * Recursive storage walk for videos the MediaStore index does not expose
 * (Telegram, WhatsApp, `.nomedia` folders).
 *
 * Results stream through [onItem] so the library fills in progressively, and
 * metadata is memoised by path+size+mtime so a second launch never pays the
 * `MediaMetadataRetriever` cost again.
 */
class DeepStorageScanner(private val metadataStore: MediaMetadataStore) {

    private val videoExtensions = setOf(
        "mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "m4v", "ts", "wmv", "mpg", "mpeg"
    )

    private val ignoredDirNames = setOf(
        "cache", "thumbnails", "lost.dir", "miui", "backup", "backups", "tmp", "temp"
    )

    suspend fun scan(
        existingPaths: Set<String>,
        onItem: (VideoItem) -> Unit
    ): List<VideoItem> = withContext(Dispatchers.IO) {
        val results = ArrayList<VideoItem>()
        val visited = HashSet<String>()
        val pending = ArrayDeque<Pair<File, Int>>()

        @Suppress("DEPRECATION")
        val root = Environment.getExternalStorageDirectory()
        if (root != null && root.exists()) pending.addLast(root to 0)

        while (pending.isNotEmpty()) {
            coroutineContext.ensureActive()
            val (directory, depth) = pending.removeLast()
            if (depth > MAX_DEPTH) continue

            val canonical = runCatching { directory.canonicalPath }
                .getOrDefault(directory.absolutePath)
            if (!visited.add(canonical)) continue

            val children = runCatching { directory.listFiles() }.getOrNull() ?: continue
            for (child in children) {
                if (child.isDirectory) {
                    if (!shouldSkip(child)) pending.addLast(child to depth + 1)
                } else {
                    val item = buildItem(child, existingPaths) ?: continue
                    results += item
                    onItem(item)
                }
            }
        }
        results
    }

    private fun shouldSkip(directory: File): Boolean {
        val name = directory.name
        if (name.startsWith(".")) return true
        if (name.lowercase() in ignoredDirNames) return true
        val parentIsAndroid = directory.parentFile?.name.equals("Android", ignoreCase = true)
        return parentIsAndroid && (name.equals("data", true) || name.equals("obb", true))
    }

    private fun buildItem(file: File, existingPaths: Set<String>): VideoItem? {
        if (file.length() < MIN_SIZE_BYTES) return null
        val extension = file.extension.lowercase()
        if (extension !in videoExtensions) return null

        val path = file.absolutePath
        if (path in existingPaths) return null

        val size = file.length()
        val modified = file.lastModified()
        val key = MediaMetadataStore.keyFor(path, size, modified)
        val cached = metadataStore.get(key)

        val durationMs: Long
        val resolution: String
        if (cached != null) {
            durationMs = cached.durationMs
            resolution = cached.resolution
        } else {
            val probed = probeMetadata(file)
            durationMs = probed.first
            resolution = probed.second
            metadataStore.put(key, MediaMetadataStore.Entry(durationMs, resolution))
        }

        val format = extension.uppercase()
        val uri = Uri.fromFile(file)
        val badges = buildList {
            add(VideoBadge(resolution, resolutionVariant(resolution)))
            add(VideoBadge(format, BadgeVariant.GRAY))
        }

        return VideoItem(
            id = path.hashCode().toString(),
            title = file.nameWithoutExtension,
            uri = uri,
            path = path,
            durationMs = durationMs,
            durationFormatted = formatDuration(durationMs),
            sizeBytes = size,
            fileSizeFormatted = formatBytes(size),
            format = format,
            resolution = resolution,
            thumbnailUrl = uri.toString(),
            dateModifiedMs = modified,
            isNew = System.currentTimeMillis() - modified < NEW_WINDOW_MS,
            badges = badges,
            parentFolder = file.parentFile?.name ?: "Storage"
        )
    }

    private fun probeMetadata(file: File): Pair<Long, String> {
        var durationMs = 0L
        var resolution = "HD"
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            resolution = resolutionOf(width, height)
        } catch (_: Exception) {
            // Unreadable container: keep the file with unknown metadata.
        } finally {
            runCatching { retriever.release() }
        }
        return durationMs to resolution
    }

    private fun resolutionOf(width: Int, height: Int): String {
        val maxDim = maxOf(width, height)
        return when {
            maxDim >= 3840 -> "4K"
            maxDim >= 2560 -> "2K"
            maxDim >= 1920 -> "FHD"
            maxDim >= 1280 -> "HD"
            maxDim > 0 -> "SD"
            else -> "HD"
        }
    }

    private fun resolutionVariant(resolution: String): BadgeVariant = when (resolution) {
        "4K" -> BadgeVariant.GOLD
        "FHD" -> BadgeVariant.GRAY
        else -> BadgeVariant.BLUE
    }

    private companion object {
        const val MAX_DEPTH = 7
        const val MIN_SIZE_BYTES = 500_000L
        const val NEW_WINDOW_MS = 86_400_000L * 3
    }
}
