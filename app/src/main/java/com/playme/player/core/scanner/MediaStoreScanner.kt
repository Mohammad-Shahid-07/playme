package com.playme.player.core.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.playme.player.core.model.BadgeVariant
import com.playme.player.core.model.VideoBadge
import com.playme.player.core.model.VideoItem
import com.playme.player.core.util.formatBytes
import com.playme.player.core.util.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreScanner(private val context: Context) {

    suspend fun scanMediaStoreVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()
        val collection: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.RESOLUTION
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                // Optional on some OEM MediaProviders / API 28 rows where WIDTH/HEIGHT are 0.
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val resCol = cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(nameCol) ?: "Untitled Video"
                    val path = cursor.getString(dataCol) ?: ""
                    val durationMs = cursor.getLong(durCol).coerceAtLeast(0L)
                    val sizeBytes = cursor.getLong(sizeCol).coerceAtLeast(0L)
                    val dateModifiedMs = cursor.getLong(dateCol) * 1000L
                    var width = if (widthCol != -1) cursor.getInt(widthCol) else 0
                    var height = if (heightCol != -1) cursor.getInt(heightCol) else 0
                    if ((width <= 0 || height <= 0) && resCol != -1) {
                        val parsed = parseResolutionString(cursor.getString(resCol))
                        if (parsed != null) {
                            width = parsed.first
                            height = parsed.second
                        }
                    }

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val fileName = path.substringAfterLast('/').ifEmpty { title }
                    val format = fileName.substringAfterLast('.', "").ifBlank { "MP4" }.uppercase()
                    val resolution = determineResolution(width, height)
                    val parent = if (path.isNotEmpty()) File(path).parentFile?.name ?: "Internal" else "Internal"

                    val badges = mutableListOf<VideoBadge>()
                    if (resolution.isNotEmpty()) {
                        val variant = when (resolution) {
                            "4K" -> BadgeVariant.GOLD
                            "FHD" -> BadgeVariant.GRAY
                            else -> BadgeVariant.BLUE
                        }
                        badges.add(VideoBadge(resolution, variant))
                    }
                    badges.add(VideoBadge(format, BadgeVariant.GRAY))

                    videos.add(
                        VideoItem(
                            id = id.toString(),
                            title = cleanTitle(title),
                            uri = contentUri,
                            path = path,
                            durationMs = durationMs,
                            durationFormatted = formatDuration(durationMs),
                            sizeBytes = sizeBytes,
                            fileSizeFormatted = formatBytes(sizeBytes),
                            format = format,
                            resolution = resolution,
                            thumbnailUrl = contentUri.toString(),
                            dateModifiedMs = dateModifiedMs,
                            isNew = System.currentTimeMillis() - dateModifiedMs < 86_400_000L * 3, // 3 days
                            badges = badges,
                            parentFolder = parent
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Log or fallback
        }

        videos
    }

    private fun parseResolutionString(raw: String?): Pair<Int, Int>? {
        if (raw.isNullOrBlank()) return null
        val parts = raw.trim().split('x', 'X', '×')
        if (parts.size != 2) return null
        val w = parts[0].trim().toIntOrNull() ?: return null
        val h = parts[1].trim().toIntOrNull() ?: return null
        return if (w > 0 && h > 0) w to h else null
    }

    private fun determineResolution(width: Int, height: Int): String {
        val maxDim = maxOf(width, height)
        val minDim = minOf(width, height)
        return when {
            maxDim >= 3840 || minDim >= 2160 -> "4K"
            maxDim >= 2560 || minDim >= 1440 -> "2K"
            maxDim >= 1920 || minDim >= 1080 -> "FHD"
            maxDim >= 1280 || minDim >= 720 -> "HD"
            minDim >= 480 -> "480p"
            else -> "SD"
        }
    }

    private fun cleanTitle(raw: String): String =
        raw.substringBeforeLast('.').ifBlank { raw }
}
