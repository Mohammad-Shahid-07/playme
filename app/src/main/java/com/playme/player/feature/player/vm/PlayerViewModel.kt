package com.playme.player.feature.player.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.GestureSeekState
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.model.PlayerState
import com.playme.player.feature.player.model.QueueItem
import com.playme.player.feature.player.model.VideoScaleMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        PlayerState(
            currentVideo = VideoItem(
                id = "1",
                title = "Cyberpunk Edgerunners - Ep 01",
                durationMs = 734_000L,
                durationFormatted = "12:14"
            ),
            isPlaying = true,
            currentPositionMs = 3_000L,
            durationMs = 734_000L,
            bufferedPositionMs = 120_000L,
            playbackSpeed = 1.0f,
            queue = listOf(
                QueueItem("1", "Cyberpunk Edgerunners - Ep 01", "24:15", isPlaying = true, resolutionBadge = "4K"),
                QueueItem("2", "Cyberpunk Edgerunners - Ep 02", "25:02", resolutionBadge = "4K"),
                QueueItem("3", "Cyberpunk Edgerunners - Ep 03", "23:48", resolutionBadge = "1080p"),
                QueueItem("4", "Cyberpunk Edgerunners - Ep 04", "24:30", resolutionBadge = "1080p"),
                QueueItem("5", "Cyberpunk Edgerunners - Ep 05", "26:10", resolutionBadge = "HDR")
            )
        )
    )
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    fun onAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.TogglePlayPause -> {
                _state.update { it.copy(isPlaying = !it.isPlaying) }
            }
            is PlayerAction.Play -> {
                _state.update { it.copy(isPlaying = true) }
            }
            is PlayerAction.Pause -> {
                _state.update { it.copy(isPlaying = false) }
            }
            is PlayerAction.SeekTo -> {
                _state.update {
                    it.copy(currentPositionMs = action.positionMs.coerceIn(0L, it.durationMs))
                }
            }
            is PlayerAction.SeekRelative -> {
                _state.update {
                    val newPos = (it.currentPositionMs + action.deltaSeconds * 1000L).coerceIn(0L, it.durationMs)
                    it.copy(currentPositionMs = newPos)
                }
            }
            is PlayerAction.UpdateGestureSeek -> {
                _state.update {
                    val target = (it.currentPositionMs + action.deltaSeconds * 1000L).coerceIn(0L, it.durationMs)
                    it.copy(
                        gestureSeek = GestureSeekState(
                            isSeeking = true,
                            deltaSeconds = action.deltaSeconds,
                            targetPositionMs = target
                        )
                    )
                }
            }
            is PlayerAction.EndGestureSeek -> {
                _state.update {
                    val newPos = if (action.apply) it.gestureSeek.targetPositionMs else it.currentPositionMs
                    it.copy(
                        currentPositionMs = newPos,
                        gestureSeek = GestureSeekState(isSeeking = false)
                    )
                }
            }
            is PlayerAction.StartQuickBoost -> {
                _state.update {
                    it.copy(
                        isQuickBoosting = true,
                        quickBoostSpeed = action.speed
                    )
                }
            }
            is PlayerAction.StopQuickBoost -> {
                _state.update {
                    it.copy(isQuickBoosting = false) }
            }
            is PlayerAction.SetPlaybackSpeed -> {
                _state.update {
                    it.copy(playbackSpeed = action.speed, isSpeedMenuOpen = false)
                }
            }
            is PlayerAction.ToggleSpeedMenu -> {
                _state.update {
                    it.copy(isSpeedMenuOpen = action.open ?: !it.isSpeedMenuOpen)
                }
            }
            is PlayerAction.ToggleQueueDrawer -> {
                _state.update {
                    it.copy(isQueueDrawerOpen = action.open ?: !it.isQueueDrawerOpen)
                }
            }
            is PlayerAction.ToggleSettings -> {
                _state.update {
                    it.copy(isSettingsOpen = action.open ?: !it.isSettingsOpen)
                }
            }
            is PlayerAction.SelectSettingsTab -> {
                _state.update {
                    it.copy(activeSettingsTab = action.tab)
                }
            }
            is PlayerAction.SelectQueueItem -> {
                _state.update { current ->
                    val updatedQueue = current.queue.map { item ->
                        item.copy(isPlaying = item.id == action.id)
                    }
                    val selected = updatedQueue.firstOrNull { it.id == action.id }
                    current.copy(
                        queue = updatedQueue,
                        currentVideo = current.currentVideo?.copy(
                            id = action.id,
                            title = selected?.title ?: current.currentVideo.title
                        )
                    )
                }
            }
            is PlayerAction.ToggleControlsLock -> {
                _state.update { it.copy(isControlsLocked = !it.isControlsLocked) }
            }
            is PlayerAction.SetControlsVisible -> {
                _state.update { it.copy(areControlsVisible = action.visible) }
            }
            is PlayerAction.ToggleControlsVisible -> {
                _state.update { it.copy(areControlsVisible = !it.areControlsVisible) }
            }
            is PlayerAction.CycleScaleMode -> {
                _state.update {
                    val next = when (it.scaleMode) {
                        VideoScaleMode.FIT -> VideoScaleMode.FILL
                        VideoScaleMode.FILL -> VideoScaleMode.STRETCH
                        VideoScaleMode.STRETCH -> VideoScaleMode.RATIO_16_9
                        VideoScaleMode.RATIO_16_9 -> VideoScaleMode.RATIO_4_3
                        VideoScaleMode.RATIO_4_3 -> VideoScaleMode.FIT
                    }
                    it.copy(scaleMode = next)
                }
            }
            is PlayerAction.SetVolume -> {
                _state.update { it.copy(volume = action.volume.coerceIn(0f, 1f)) }
            }
            is PlayerAction.SetBrightness -> {
                _state.update { it.copy(brightness = action.brightness.coerceIn(0f, 1f)) }
            }
            is PlayerAction.ClosePlayer -> {
                // Navigation handler
            }
        }
    }
}
