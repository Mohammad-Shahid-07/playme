package com.playme.player.core.model

import android.net.Uri
import java.util.Locale
import java.util.concurrent.TimeUnit

data class VideoItem(
    val id: Long,
    val title: String,
    val displayName: String,
    val path: String,
    val contentUri: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long,
    val width: Int,
    val height: Int,
    val folderName: String,
    val folderPath: String,
    val lastPlayedPositionMs: Long = 0L,
    val lastWatchedTimestamp: Long = 0L
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "00:00"
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.getDefault(), "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                else -> String.format(Locale.getDefault(), "%.0f KB", kb)
            }
        }

    val resolutionBadge: String
        get() {
            val maxDimension = maxOf(width, height)
            return when {
                maxDimension >= 3840 -> "4K UHD"
                maxDimension >= 2560 -> "2K QHD"
                maxDimension >= 1920 -> "1080p FHD"
                maxDimension >= 1280 -> "720p HD"
                maxDimension > 0 -> "SD"
                else -> ""
            }
        }

    val simpleResolution: String
        get() {
            val maxDimension = maxOf(width, height)
            return when {
                maxDimension >= 3840 -> "4K"
                maxDimension >= 2560 -> "2K"
                maxDimension >= 1920 -> "1080p"
                maxDimension >= 1280 -> "720p"
                maxDimension > 0 -> "480p"
                else -> "1080p"
            }
        }

    val extension: String
        get() = displayName.substringAfterLast('.', "MP4").uppercase(Locale.getDefault())

    val remainingText: String?
        get() {
            if (durationMs <= 0 || lastPlayedPositionMs <= 0) return null
            val remainingMs = (durationMs - lastPlayedPositionMs).coerceAtLeast(0L)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs)
            return when {
                minutes < 1 -> "< 1m left"
                minutes < 60 -> "${minutes}m left"
                else -> {
                    val hours = minutes / 60
                    val remMins = minutes % 60
                    "${hours}h ${remMins}m left"
                }
            }
        }

    val progressFraction: Float
        get() {
            if (durationMs <= 0 || lastPlayedPositionMs <= 0) return 0f
            return (lastPlayedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

    val isNew: Boolean
        get() {
            if (dateAdded <= 0L) return false
            val now = System.currentTimeMillis()
            val addedMs = if (dateAdded > 1000000000000L) dateAdded else dateAdded * 1000L
            return (now - addedMs) < (14L * 24 * 3600 * 1000L)
        }

    val uri: Uri
        get() = Uri.parse(contentUri)
}

data class VideoFolder(
    val id: String,
    val name: String,
    val path: String,
    val videoCount: Int,
    val thumbnailUri: String? = null
)

enum class AspectRatioMode(val label: String) {
    FIT("Fit Screen"),
    ZOOM("Crop & Zoom"),
    STRETCH("Stretch"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3")
}

data class SubtitleTrackItem(
    val id: String,
    val name: String,
    val language: String? = null,
    val isSelected: Boolean = false,
    val isExternal: Boolean = false,
    val uri: String? = null
)

data class AudioTrackItem(
    val id: String,
    val name: String,
    val language: String? = null,
    val channels: Int = 2,
    val bitrate: String? = null,
    val codec: String? = null,
    val isHardwareAccelerated: Boolean = true,
    val isSelected: Boolean = false
)

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val minMilliBels: Int = -1200,
    val maxMilliBels: Int = 1200,
    val levelMilliBels: Int = 0
) {
    val formattedFreq: String
        get() = when {
            centerFreqHz >= 1000 -> "${centerFreqHz / 1000}k"
            else -> "${centerFreqHz}Hz"
        }

    val dB: Int
        get() = levelMilliBels / 100
}

data class EqualizerPreset(
    val name: String,
    val bandLevels: List<Int> // dB values (-12 to +12)
)

data class SleepTimerConfig(
    val minutes: Int, // 0 = Off, -1 = End of video, >0 = minutes
    val waitForMediaEnd: Boolean = true,
    val resetOnInteraction: Boolean = true
)

data class ABRepeatState(
    val isEnabled: Boolean = false,
    val pointA: Long? = null,
    val pointB: Long? = null
)

data class NerdStatsInfo(
    val resolution: String = "1920x1080",
    val videoCodec: String = "H.264 / AVC",
    val decoderName: String = "c2.android.avc.decoder",
    val isHardwareAccelerated: Boolean = true,
    val bitrateKbps: Long = 4850L,
    val frameRate: Float = 24.0f,
    val droppedFrames: Int = 0,
    val audioCodec: String = "AAC-LC",
    val audioChannels: Int = 2,
    val sampleRateHz: Int = 48000,
    val bufferDurationMs: Long = 3500L
)

data class OpenSubtitleItem(
    val id: String,
    val fileName: String,
    val language: String,
    val matchPercentage: Int,
    val downloadUrl: String,
    val isDownloaded: Boolean = false
)
