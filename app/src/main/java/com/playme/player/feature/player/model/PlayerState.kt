package com.playme.player.feature.player.model

import com.playme.player.core.model.VideoItem

enum class VideoScaleMode {
    FIT, FILL, STRETCH, RATIO_16_9, RATIO_4_3
}

enum class SettingsTab {
    AUDIO, SUBTITLES, DISPLAY, PLAYBACK
}

data class QueueItem(
    val id: String,
    val title: String,
    val durationFormatted: String,
    val isPlaying: Boolean = false,
    val resolutionBadge: String? = null
)

data class GestureSeekState(
    val isSeeking: Boolean = false,
    val deltaSeconds: Int = 0,
    val targetPositionMs: Long = 0L
)

data class PlayerState(
    val currentVideo: VideoItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isQuickBoosting: Boolean = false,
    val quickBoostSpeed: Float = 2.0f,
    val isSpeedMenuOpen: Boolean = false,
    val isQueueDrawerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val activeSettingsTab: SettingsTab = SettingsTab.AUDIO,
    val isControlsLocked: Boolean = false,
    val areControlsVisible: Boolean = true,
    val scaleMode: VideoScaleMode = VideoScaleMode.FIT,
    val volume: Float = 1.0f,
    val brightness: Float = 0.8f,
    val gestureSeek: GestureSeekState = GestureSeekState(),
    val queue: List<QueueItem> = emptyList()
)
