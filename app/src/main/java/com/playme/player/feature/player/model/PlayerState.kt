package com.playme.player.feature.player.model

import com.playme.player.core.model.MediaTrackInfo
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
    val preShufflePlaylist: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.BEST_FIT,
    val decoderMode: DecoderMode = DecoderMode.HW,
    val activeEngineType: com.playme.player.core.player.EngineType = com.playme.player.core.player.EngineType.EXO,
    val isControlsVisible: Boolean = true,
    val isAbLoopActive: Boolean = false,
    val abLoopStartMs: Long? = null,
    val abLoopEndMs: Long? = null,
    val volumePercent: Float = -1f,
    val brightnessPercent: Float = -1f,
    val activeGesture: GestureType = GestureType.NONE,
    val gestureValue: Float = 0f,
    val seekPreviewPositionMs: Long = 0L,
    val isSpeedBoostActive: Boolean = false,
    val boostSpeed: Float = 2.0f,
    val toastMessage: String? = null,
    val isQueueOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val activeSettingsTab: SettingsTab = SettingsTab.AUDIO,
    val audioDelayMs: Long = 0L,
    val subtitleDelayMs: Long = 0L,
    val currentSubtitle: String? = null,
    val isFullscreen: Boolean = false,
    val isShuffle: Boolean = false,
    val repeatMode: String = "all", // "off", "all", "one"
    val isAutoplay: Boolean = true,
    val isSpeedPickerOpen: Boolean = false,
    val isDialogueClarityActive: Boolean = true,
    val volumeBoostPercent: Int = 100, // 100% to 200%
    val ambientGlowEnabled: Boolean = true,
    val sleepTimerMinutes: Int = 0, // 0 = Off
    val backgroundPlayEnabled: Boolean = false,
    val availableAudioTracks: List<MediaTrackInfo> = emptyList(),
    val availableSubtitleTracks: List<MediaTrackInfo> = emptyList(),
    val selectedAudioTrack: String = "default",
    val selectedSubtitleTrack: String = "none",
    val subtitleSize: String = "Normal"
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val bufferPercent: Float
        get() = if (durationMs > 0) (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
