package com.playme.player.feature.player.model

import com.playme.player.core.model.VideoItem

sealed interface PlayerAction {
    data object TogglePlay : PlayerAction
    data object Play : PlayerAction
    data object Pause : PlayerAction
    data class SeekTo(val positionMs: Long) : PlayerAction
    data class SeekBy(val deltaMs: Long) : PlayerAction
    data class SetSpeed(val speed: Float) : PlayerAction
    data object CycleSpeed : PlayerAction
    data object ToggleMute : PlayerAction
    data object ToggleLock : PlayerAction
    data object CycleAspectRatio : PlayerAction
    data object ToggleDecoderMode : PlayerAction
    data object ToggleControls : PlayerAction
    data object HideControls : PlayerAction
    data object ShowControls : PlayerAction
    data object ToggleAbLoop : PlayerAction
    data class SetVolume(val volume: Float) : PlayerAction
    data class SyncSystemVolume(val volume: Float) : PlayerAction
    data class SetBrightness(val brightness: Float) : PlayerAction
    data class StartGesture(val type: GestureType, val value: Float) : PlayerAction
    data class UpdateGesture(val type: GestureType, val value: Float) : PlayerAction
    data object EndGesture : PlayerAction
    data class SetSpeedBoost(val enabled: Boolean) : PlayerAction
    data class ShowToast(val message: String) : PlayerAction
    data object ClearToast : PlayerAction
    data class SetQueueOpen(val isOpen: Boolean) : PlayerAction
    data class SetSettingsOpen(val isOpen: Boolean, val tab: SettingsTab = SettingsTab.AUDIO) : PlayerAction
    data class SetSettingsTab(val tab: SettingsTab) : PlayerAction
    data class AdjustAudioDelay(val deltaMs: Long) : PlayerAction
    data class AdjustSubtitleDelay(val deltaMs: Long) : PlayerAction
    data object ResetDelays : PlayerAction
    data class PlayTrack(val video: VideoItem) : PlayerAction
    data object PlayNext : PlayerAction
    data object PlayPrevious : PlayerAction
    data object ToggleFullscreen : PlayerAction
    data object ClosePlayer : PlayerAction
    data object ToggleShuffle : PlayerAction
    data object CycleRepeatMode : PlayerAction
    data object ToggleAutoplay : PlayerAction
    data class RemoveFromQueue(val videoId: String) : PlayerAction
    data object ClearQueue : PlayerAction
    data class SetSpeedPickerOpen(val isOpen: Boolean) : PlayerAction
    data object ToggleDialogueClarity : PlayerAction
    data class SetVolumeBoost(val percent: Int) : PlayerAction
    data object ToggleAmbientGlow : PlayerAction
    data class SetSleepTimer(val minutes: Int) : PlayerAction
    data object ToggleBackgroundPlay : PlayerAction
    data class SelectAudioTrack(val track: String) : PlayerAction
    data class SelectSubtitleTrack(val track: String) : PlayerAction
    data class SetSubtitleSize(val size: String) : PlayerAction
    data class SetAspectRatio(val mode: AspectRatioMode) : PlayerAction
    data class SetDecoderMode(val mode: DecoderMode) : PlayerAction
    data class SetSpeedBoostValue(val speed: Float) : PlayerAction
}
