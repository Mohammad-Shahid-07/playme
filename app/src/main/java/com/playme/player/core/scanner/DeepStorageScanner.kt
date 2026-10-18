package com.playme.player.core.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import com.playme.player.core.model.BadgeVariant
import com.playme.player.core.model.VideoBadge
import com.playme.player.core.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeepStorageScanner(private val context: Context) {

    private val videoExtensions = setOf(
        "mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "m4v", "ts", "wmv"
    )

    private val ignoredDirNames = setOf(
        ".git", "node_modules", "cache", "thumbnails", ".thumbnails"
    )

    suspend fun scanDeepVideos(existingPaths: Set<String>): List<VideoItem> = withContext(Dispatchers.IO) {
        val discoveredVideos = mutableListOf<VideoItem>()
        val rootsToScan = mutableListOf<File>()

        // 1. Primary external storage root
        val primaryStorage = Environment.getExternalStorageDirectory()
        if (primaryStorage != null && primaryStorage.exists()) {
            rootsToScan.add(primaryStorage)
        }

        // 2. Scan each root recursively
        val visitedDirs = mutableSetOf<String>()
        for (root in rootsToScan) {
            scanDirectory(root, existingPaths, discoveredVideos, visitedDirs, depth = 0, maxDepth = 7)
        }

        discoveredVideos
    }

    private fun scanDirectory(
        directory: File,
        existingPaths: Set<String>,
        results: MutableList<VideoItem>,
        visitedDirs: MutableSet<String>,
        depth: Int,
        maxDepth: Int
    ) {
        if (depth > maxDepth || !directory.exists() || !directory.isDirectory) return
        val canonical = try { directory.canonicalPath } catch (_: Exception) { directory.absolutePath }
        if (!visitedDirs.add(canonical)) return

        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                val dirName = file.name
                // Skip non-media system caches, but specifically allow Android/media (Telegram/WhatsApp live here!)
                if (dirName.startsWith(".") && dirName != ".nomedia") continue
                if (ignoredDirNames.contains(dirName.lowercase())) continue
                // Android/data is restricted on API 30+, but Android/media is accessible
                if (dirName.equals("data", ignoreCase = true) && file.parentFile?.name.equals("Android", ignoreCase = true)) {
                    continue
                }

                scanDirectory(file, existingPaths, results, visitedDirs, depth + 1, maxDepth)
            } else if (file.isFile && file.length() > 500_000L) { // Min 500KB to ignore tiny cached clips
                val ext = file.extension.lowercase()
                if (videoExtensions.contains(ext)) {
                    val filePath = file.absolutePath
                    // Avoid duplicating files already found by MediaStore
                    if (!existingPaths.contains(filePath)) {
                        buildVideoItemFromFile(file)?.let { results.add(it) }
                    }
                }
            }
        }
    }

    private fun buildVideoItemFromFile(file: File): VideoItem? {
        val path = file.absolutePath
        val sizeBytes = file.length()
        val dateModified = file.lastModified()
        val format = file.extension.uppercase()
        val parent = file.parentFile?.name ?: "Storage"

        var durationMs = 0L
        var resolution = "HD"

        // Extract metadata via MediaMetadataRetriever
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val durString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durString?.toLongOrNull() ?: 0L

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            if (width > 0 && height > 0) {
                val maxDim = maxOf(width, height)
                resolution = if (maxDim >= 3840) "4K" else if (maxDim >= 1920) "FHD" else if (maxDim >= 1280) "HD" else "SD"
            }
            retriever.release()
        } catch (_: Exception) {
            // Unreadable video or permission fallback
        }

        val badges = mutableListOf<VideoBadge>()
        val variant = when (resolution) {
            "4K" -> BadgeVariant.GOLD
            "FHD" -> BadgeVariant.GRAY
            else -> BadgeVariant.BLUE
        }
        badges.add(VideoBadge(resolution, variant))
        badges.add(VideoBadge(format, BadgeVariant.GRAY))

        val uri = Uri.fromFile(file)

        return VideoItem(
            id = path.hashCode().toString(),
            title = file.nameWithoutExtension,
            uri = uri,
            path = path,
            durationMs = durationMs,
            durationFormatted = formatDuration(durationMs),
            sizeBytes = sizeBytes,
            fileSizeFormatted = formatFileSize(sizeBytes),
            format = format,
            resolution = resolution,
            thumbnailUrl = uri.toString(),
            dateModifiedMs = dateModified,
            isNew = System.currentTimeMillis() - dateModified < 86_400_000L * 3,
            badges = badges,
            parentFolder = parent
        )
    }

    private fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000L
        val hours = totalSec / 3600L
        val minutes = (totalSec % 3600L) / 60L
        val seconds = totalSec % 60L
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1000.0) {
            "%.1f GB".format(mb / 1024.0)
        } else {
            "%.0f MB".format(mb)
        }
    }
}
