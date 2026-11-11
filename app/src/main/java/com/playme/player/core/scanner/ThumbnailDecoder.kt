package com.playme.player.core.scanner

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Produces a thumbnail bitmap for a video file.
 *
 * The steps are ordered cheapest-first, because most files are answered by the
 * system in microseconds and only genuinely undecodable ones reach FFmpeg:
 *
 *  1. disk cache
 *  2. MediaStore thumbnail (API 29+ loadThumbnail, API 26–28 Video.Thumbnails)
 *  3. `MediaMetadataRetriever` at 10% (also covers `.nomedia` folders)
 *  4. FFmpeg software decode
 *
 * Pure decoding — caching and concurrency live in [ThumbnailManager].
 */
internal object ThumbnailDecoder {

    fun decode(
        context: Context,
        videoId: String,
        uri: Uri,
        filePath: String,
        durationMs: Long,
        targetWidth: Int,
        targetHeight: Int,
        diskCacheFile: java.io.File
    ): Bitmap? {
        diskCached(diskCacheFile)?.let { return it }

        val systemThumbnail = systemThumbnail(
            context, videoId, uri, targetWidth, targetHeight
        )?.let(::toSoftwareBitmap)

        // System thumbnails are absent for `.nomedia` files and some API 28 codecs,
        // or can be a black fade-in frame. Try MediaMetadataRetriever before FFmpeg.
        val candidate = when {
            systemThumbnail == null -> retrieverFrame(context, uri, filePath)
            isMostlyBlack(systemThumbnail) -> retrieverFrame(context, uri, filePath) ?: systemThumbnail
            else -> systemThumbnail
        }?.let(::toSoftwareBitmap)

        candidate?.let { return writeDiskCache(it, diskCacheFile, targetWidth, targetHeight) }

        // Nothing from the platform. FFmpeg decodes and writes the cache itself.
        if (filePath.isEmpty()) return null
        val seekMs = if (durationMs > 10_000L) durationMs / 10L else 1_000L
        if (!FfmpegFrameExtractor.extractFrame(filePath, diskCacheFile, seekMs)) return null

        return runCatching {
            android.graphics.BitmapFactory.decodeFile(diskCacheFile.absolutePath)
        }.getOrNull()
    }

    private fun diskCached(file: java.io.File): Bitmap? {
        if (!file.exists()) return null
        return runCatching {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        }.getOrNull()
    }

    private fun systemThumbnail(
        context: Context,
        videoId: String,
        uri: Uri,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        if (uri.scheme != "content") return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return runCatching {
                context.contentResolver.loadThumbnail(uri, Size(targetWidth, targetHeight), null)
            }.getOrNull()
        }

        @Suppress("DEPRECATION")
        return runCatching {
            videoId.toLongOrNull()?.let { id ->
                MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver,
                    id,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
                )
            }
        }.getOrNull()
    }

    /** Grabs a representative frame: 10% in for anything longer than 10s. */
    private fun retrieverFrame(context: Context, uri: Uri, filePath: String): Bitmap? {
        if (filePath.isEmpty() && uri.scheme != "content") return null
        val retriever = MediaMetadataRetriever()
        return try {
            if (filePath.isNotEmpty()) {
                try {
                    retriever.setDataSource(filePath)
                } catch (e: Exception) {
                    if (uri.scheme == "content") retriever.setDataSource(context, uri) else throw e
                }
            } else {
                retriever.setDataSource(context, uri)
            }
            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val atUs = when {
                durationMs > 10_000L -> durationMs * 1_000L / 10L
                durationMs > 0L -> (durationMs * 500L).coerceAtMost(1_000_000L)
                else -> 1_000_000L
            }
            retriever.getFrameAtTime(atUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun toSoftwareBitmap(bitmap: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            bitmap.config == Bitmap.Config.HARDWARE
        ) {
            return bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
        }
        return bitmap
    }

    /**
     * Downsample preserving the source aspect ratio — forcing a fixed target
     * size here squashed portrait (9:16) videos into a 16:9 box.
     */
    private fun writeDiskCache(
        source: Bitmap,
        file: java.io.File,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val scale = max(
            targetWidth.toFloat() / source.width,
            targetHeight.toFloat() / source.height
        ).coerceAtMost(1f)

        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                source,
                (source.width * scale).roundToInt().coerceAtLeast(1),
                (source.height * scale).roundToInt().coerceAtLeast(1),
                true
            )
        } else {
            source
        }

        runCatching {
            java.io.FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
        }
        return scaled
    }

    /** Cheap luminance sampling — 144 pixels is enough to spot a black frame. */
    private fun isMostlyBlack(bitmap: Bitmap): Boolean {
        if (bitmap.width == 0 || bitmap.height == 0) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            bitmap.config == Bitmap.Config.HARDWARE
        ) {
            return false
        }
        val stepX = (bitmap.width / 12).coerceAtLeast(1)
        val stepY = (bitmap.height / 12).coerceAtLeast(1)
        var dark = 0
        var samples = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val luminance = android.graphics.Color.red(pixel) * 0.299f +
                    android.graphics.Color.green(pixel) * 0.587f +
                    android.graphics.Color.blue(pixel) * 0.114f
                if (luminance < 20f) dark++
                samples++
                x += stepX
            }
            y += stepY
        }
        return samples > 0 && dark.toFloat() / samples > 0.92f
    }
}
