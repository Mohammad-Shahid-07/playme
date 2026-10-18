package com.playme.player.feature.player.model

sealed interface PlayerAction {
    data object TogglePlayPause : PlayerAction
    data object Play : PlayerAction
    data object Pause : PlayerAction
    data class SeekTo(val positionMs: Long) : PlayerAction
    data class SeekRelative(val deltaSeconds: Int) : PlayerAction

    // Gesture Seeking
    data class UpdateGestureSeek(val deltaSeconds: Int) : PlayerAction
    data class EndGestureSeek(val apply: Boolean = true) : PlayerAction

    // Quick Speed Boost (2x Long Press)
    data class StartQuickBoost(val speed: Float = 2.0f) : PlayerAction
    data object StopQuickBoost : PlayerAction

    // Speed Selector Menu
    data class SetPlaybackSpeed(val speed: Float) : PlayerAction
    data class ToggleSpeedMenu(val open: Boolean? = null) : PlayerAction

    // Drawers & Sidebars
    data class ToggleQueueDrawer(val open: Boolean? = null) : PlayerAction
    data class ToggleSettings(val open: Boolean? = null) : PlayerAction
    data class SelectSettingsTab(val tab: SettingsTab) : PlayerAction
    data class SelectQueueItem(val id: String) : PlayerAction

    // Controls
    data object ToggleControlsLock : PlayerAction
    data class SetControlsVisible(val visible: Boolean) : PlayerAction
    data object ToggleControlsVisible : PlayerAction
    data object CycleScaleMode : PlayerAction
    data class SetVolume(val volume: Float) : PlayerAction
    data class SetBrightness(val brightness: Float) : PlayerAction

    // Navigation
    data object ClosePlayer : PlayerAction
}
