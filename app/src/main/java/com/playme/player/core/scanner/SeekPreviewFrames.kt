package com.playme.player.core.scanner

import android.content.Context
import android.graphics.Bitmap
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File
import java.util.Locale

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

    fun bucketFor(durationMs: Long, positionMs: Long): Int {
        if (durationMs <= 0L) return 0
        val ratio = (positionMs.toDouble() / durationMs).coerceIn(0.0, 0.9999)
        return (ratio * BUCKET_COUNT).toInt().coerceIn(0, BUCKET_COUNT - 1)
    }

    private fun frameFile(context: Context, videoId: String, bucket: Int): File =
        File(context.cacheDir, "seek_${CACHE_VERSION}_${videoId}_$bucket.jpg")

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

        if (!file.exists() || file.length() == 0L) {
            // Sample the middle of the bucket so the preview matches roughly
            // what the user sees after releasing the thumb.
            val seekMs = (durationMs.toDouble() / BUCKET_COUNT * (bucket + 0.5)).toLong()
            val arguments = arrayOf(
                "-ss", String.format(Locale.US, "%.3f", seekMs / 1000.0),
                "-i", filePath,
                "-frames:v", "1",
                "-vf", scaleFilter(),
                "-q:v", "5",
                "-y", file.absolutePath
            )
            val ok = runCatching {
                FFmpegKit.executeWithArguments(arguments)
            }.isSuccess
            if (!ok || !file.exists() || file.length() == 0L) return null
        }

        return runCatching {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        }.getOrNull()
    }

    /** Letterboxes rather than stretching, so portrait clips stay readable. */
    private fun scaleFilter(): String = buildString {
        append("scale=").append(TILE_WIDTH).append(':').append(TILE_HEIGHT)
        append(":force_original_aspect_ratio=decrease")
        append(",pad=").append(TILE_WIDTH).append(':').append(TILE_HEIGHT)
        append(":(ow-iw)/2:(oh-ih)/2")
    }
}
