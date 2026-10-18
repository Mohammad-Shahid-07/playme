package com.playme.player.feature.player.model

import com.playme.player.core.model.VideoItem

enum class AspectRatioMode(val label: String) {
    BEST_FIT("Best Fit"),
    FIT_SCREEN("Fit Screen"),
    FILL("Fill"),
    SIXTEEN_NINE("16:9"),
    FOUR_THREE("4:3"),
    ORIGINAL("Original")
}

enum class DecoderMode {
    HW, SW
}

enum class GestureType {
    NONE, VOLUME, BRIGHTNESS, SEEK, SPEED_BOOST
}

enum class SettingsTab {
    AUDIO, SUBTITLES, VIDEO, PLAYBACK
}

data class PlayerState(
    val currentVideo: VideoItem? = null,
    val playlist: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.BEST_FIT,
    val decoderMode: DecoderMode = DecoderMode.HW,
    val isControlsVisible: Boolean = true,
    val isAbLoopActive: Boolean = false,
    val abLoopStartMs: Long? = null,
    val abLoopEndMs: Long? = null,
    val volumePercent: Float = 0.5f,
    val brightnessPercent: Float = 0.6f,
    val activeGesture: GestureType = GestureType.NONE,
    val gestureValue: Float = 0f,
    val seekPreviewPositionMs: Long = 0L,
    val isSpeedBoostActive: Boolean = false,
    val toastMessage: String? = null,
    val isQueueOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val activeSettingsTab: SettingsTab = SettingsTab.AUDIO,
    val audioDelayMs: Long = 0L,
    val subtitleDelayMs: Long = 0L,
    val currentSubtitle: String? = null,
    val isFullscreen: Boolean = false
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val bufferPercent: Float
        get() = if (durationMs > 0) (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
