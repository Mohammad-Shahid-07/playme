package com.playme.player.core.repository

import android.content.Context
import android.os.Build
import android.os.Environment
import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoItem
import com.playme.player.core.scanner.DeepStorageScanner
import com.playme.player.core.scanner.MediaStoreScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoRepository(private val context: Context) {

    private val mediaStoreScanner = MediaStoreScanner(context)
    private val deepStorageScanner = DeepStorageScanner(context)

    suspend fun getAllVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        // 1. Instant MediaStore scan
        val mediaStoreVideos = mediaStoreScanner.scanMediaStoreVideos()
        val indexedPaths = mediaStoreVideos.mapNotNull { it.path.ifEmpty { null } }.toSet()

        // 2. Deep recursive scan for Telegram, WhatsApp, and .nomedia files
        val deepVideos = deepStorageScanner.scanDeepVideos(indexedPaths)

        // 3. Combined & deduplicated list
        (mediaStoreVideos + deepVideos).distinctBy { it.path.ifEmpty { it.id } }
    }

    fun groupIntoFolders(videos: List<VideoItem>): List<FolderItem> {
        val folderMap = mutableMapOf<String, MutableList<VideoItem>>()

        for (video in videos) {
            val folderPath = if (video.path.isNotEmpty()) {
                File(video.path).parentFile?.absolutePath ?: "Internal Storage"
            } else {
                "Internal Storage"
            }
            folderMap.getOrPut(folderPath) { mutableListOf() }.add(video)
        }

        return folderMap.map { (path, items) ->
            val name = if (path == "Internal Storage") "Internal Storage" else File(path).name
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

    fun sortVideos(videos: List<VideoItem>, sortOption: SortOption): List<VideoItem> {
        return when (sortOption) {
            SortOption.NEWEST -> videos.sortedByDescending { it.dateModifiedMs }
            SortOption.NAME -> videos.sortedBy { it.title.lowercase() }
            SortOption.SIZE -> videos.sortedByDescending { it.sizeBytes }
            SortOption.DURATION -> videos.sortedByDescending { it.durationMs }
        }
    }

    fun searchVideos(videos: List<VideoItem>, query: String): List<VideoItem> {
        if (query.isBlank()) return videos
        val clean = query.trim().lowercase()
        return videos.filter {
            it.title.lowercase().contains(clean) || it.parentFolder.lowercase().contains(clean)
        }
    }

    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1000.0) {
            "%.1f GB".format(mb / 1024.0)
        } else {
            "%.0f MB".format(mb)
        }
    }
}
