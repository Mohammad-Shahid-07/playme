package com.playme.player.core.scanner

import android.content.Context
import android.graphics.Bitmap
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.util.Locale
import java.util.UUID

/**
 * On-demand frames for the seek bar preview.
 *
 * The timeline is divided into fixed buckets and each bucket is decoded only
 * when the user actually scrubs into it, then cached on disk.
 *
 * A sprite sheet built in one pass was the obvious design and is wrong here:
 * FFmpeg's `tile` filter needs every selected frame, so a single pass decodes
 * the whole file — minutes for a multi-hour `.ts` recording. A bucket seek
 * costs one fast input seek instead.
 */
object SeekPreviewFrames {

    /** How many positions across the timeline get their own cached frame. */
    const val BUCKET_COUNT = 24

    private const val TILE_WIDTH = 160
    private const val TILE_HEIGHT = 90
    private const val CACHE_VERSION = "f1"
    private val targetLocks = Array(64) { Any() }

    fun bucketFor(durationMs: Long, positionMs: Long): Int {
        if (durationMs <= 0L) return 0
        val ratio = (positionMs.toDouble() / durationMs).coerceIn(0.0, 0.9999)
        return (ratio * BUCKET_COUNT).toInt().coerceIn(0, BUCKET_COUNT - 1)
    }

    private fun frameFile(context: Context, videoId: String, bucket: Int): File =
        File(context.cacheDir, "seek_${CACHE_VERSION}_${videoId}_$bucket.jpg")

    private fun lockFor(file: File): Any =
        targetLocks[file.absolutePath.hashCode() and 0x3F]

    /**
     * Blocking — call from a background dispatcher. Returns the frame for the
     * bucket containing [positionMs], generating it on first use.
     */
    fun frameAt(
        context: Context,
        videoId: String,
        filePath: String,
        durationMs: Long,
        positionMs: Long
    ): Bitmap? {
        if (filePath.isEmpty() || durationMs <= 0L) return null

        val bucket = bucketFor(durationMs, positionMs)
        val file = frameFile(context, videoId, bucket)

        return synchronized(lockFor(file)) {
            if (!file.exists() || file.length() == 0L) {
                if (!generateFrame(context, file, filePath, durationMs, bucket)) {
                    return@synchronized null
                }
            }
            runCatching {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            }.getOrNull()
        }
    }

    private fun generateFrame(
        context: Context,
        targetFile: File,
        filePath: String,
        durationMs: Long,
        bucket: Int
    ): Boolean {
        // Sample the middle of the bucket so the preview matches roughly
        // what the user sees after releasing the thumb.
        val seekMs = (durationMs.toDouble() / BUCKET_COUNT * (bucket + 0.5)).toLong()
        val tempFile = File(context.cacheDir, "${targetFile.name}.${UUID.randomUUID()}.tmp.jpg")
        val arguments = arrayOf(
            "-ss", String.format(Locale.US, "%.3f", seekMs / 1000.0),
            "-i", filePath,
            "-frames:v", "1",
            "-vf", scaleFilter(),
            "-q:v", "5",
            "-y", tempFile.absolutePath
        )

        return try {
            val session = FFmpegKit.executeWithArguments(arguments)
            val ok = ReturnCode.isSuccess(session.returnCode) &&
                tempFile.exists() &&
                tempFile.length() > 0L
            if (!ok) return false

            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
            }
            targetFile.exists() && targetFile.length() > 0L
        } catch (_: Exception) {
            false
        } finally {
            runCatching { if (tempFile.exists()) tempFile.delete() }
        }
    }

    /** Letterboxes rather than stretching, so portrait clips stay readable. */
    private fun scaleFilter(): String = buildString {
        append("scale=").append(TILE_WIDTH).append(':').append(TILE_HEIGHT)
        append(":force_original_aspect_ratio=decrease")
        append(",pad=").append(TILE_WIDTH).append(':').append(TILE_HEIGHT)
        append(":(ow-iw)/2:(oh-ih)/2")
    }
}
