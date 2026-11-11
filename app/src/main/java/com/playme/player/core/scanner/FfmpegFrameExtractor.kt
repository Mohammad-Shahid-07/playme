package com.playme.player.core.scanner

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

/**
 * Grabs a single frame with FFmpeg's software decoders.
 *
 * This is the last resort in the thumbnail pipeline and the only path that
 * works for streams no Android platform decoder accepts — 10-bit HEVC being the
 * common case on MediaTek devices, where `c2.mtk.hevc.decoder`,
 * `c2.android.hevc.decoder` and `OMX.google.hevc.decoder` all fail.
 *
 * Arguments are passed as an array rather than a command string so filenames
 * containing spaces, brackets or `@` need no escaping.
 */
internal object FfmpegFrameExtractor {

    private const val SCALE_FILTER = "scale=320:-2"
    private const val MIN_SEEK_MS = 1_000L
    private const val FALLBACK_SEEK_SECONDS = "3"

    /**
     * @param seekMs where to grab the frame. Callers pass ~10% of the duration
     * so episodes sharing an intro don't all produce the same picture.
     */
    fun extractFrame(videoPath: String, outputFile: File, seekMs: Long): Boolean {
        if (outputFile.exists()) outputFile.delete()

        val seekSeconds = if (seekMs >= MIN_SEEK_MS) {
            String.format(java.util.Locale.US, "%.3f", seekMs / 1000.0)
        } else {
            FALLBACK_SEEK_SECONDS
        }

        val arguments = arrayOf(
            "-ss", seekSeconds,
            "-i", videoPath,
            "-frames:v", "1",
            "-vf", SCALE_FILTER,
            "-q:v", "4",
            "-y", outputFile.absolutePath
        )

        return runCatching {
            val session = FFmpegKit.executeWithArguments(arguments)
            ReturnCode.isSuccess(session.returnCode) && outputFile.exists() &&
                outputFile.length() > 0L
        }.getOrDefault(false)
    }
}
