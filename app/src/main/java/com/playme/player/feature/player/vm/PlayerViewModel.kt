package com.playme.player.feature.player.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.model.VideoItem
import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.feature.player.model.GestureType
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    var engine by mutableStateOf<PlayerEngineContract?>(null)
        private set

    private val effects = PlayerSessionEffects(_state, viewModelScope) { engine }
    private val transport = PlayerTransportController(_state, { engine }, effects)
    private val queue = PlayerQueueController(_state, effects, transport) { video, playlist ->
        setInitialVideo(video, playlist)
    }

    init {
        resetControlsTimer()
    }

    fun attachEngine(playerEngine: PlayerEngineContract?) {
        if (this.engine === playerEngine) return
        this.engine = playerEngine
        if (playerEngine == null) return

        playerEngine.setListener(
            PlayerEngineBridge.createEngineListener(
                state = _state,
                updateState = { updater -> _state.update(updater) },
                onSeekTo = { transport.seekTo(it) },
                onShowToast = { effects.showToast(it) },
                onPlaybackEnded = { queue.onPlaybackEnded() }
            )
        )
        playerEngine.setDecoderMode(_state.value.decoderMode)
    }

    /**
     * Publishes the selected video to the UI before the engine exists, so the
     * player screen can paint the cached poster on its very first frame
     * instead of showing an empty "Unknown Video" shell.
     */
    fun prepareVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        applyVideoSelection(video, playlist)
    }

    fun setInitialVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        applyVideoSelection(video, playlist)
        engine?.playVideo(video)
        resetControlsTimer()
    }

    private fun applyVideoSelection(video: VideoItem, playlist: List<VideoItem>) {
        _state.value = PlayerVideoSelection.apply(_state.value, video, playlist)
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.TogglePlay -> transport.togglePlay()
            PlayerAction.Play -> transport.play()
            PlayerAction.Pause -> transport.pause()
            is PlayerAction.SeekTo -> transport.seekTo(action.positionMs)
            is PlayerAction.SeekBy -> transport.seekBy(action.deltaMs)
            is PlayerAction.SetSpeed -> transport.setSpeed(action.speed)
            PlayerAction.CycleSpeed -> transport.setSpeed(PlayerPlaybackCoordinator.getNextSpeed(_state.value.playbackSpeed))
            PlayerAction.ToggleMute -> transport.toggleMute()
            PlayerAction.ToggleLock -> transport.toggleLock()
            PlayerAction.CycleAspectRatio -> transport.setAspectRatio(PlayerSettingsDelegate.cycleAspectRatio(_state.value.aspectRatioMode))
            PlayerAction.ToggleDecoderMode -> transport.setDecoderMode(PlayerSettingsDelegate.toggleDecoder(_state.value.decoderMode))
            PlayerAction.ToggleControls -> transport.toggleControls()
            PlayerAction.HideControls -> _state.update { it.copy(isControlsVisible = false) }
            PlayerAction.ShowControls -> transport.showControls()
            PlayerAction.ToggleAbLoop -> transport.toggleAbLoop()
            is PlayerAction.SetVolume -> transport.setVolume(action.volume)
            is PlayerAction.SetBrightness -> _state.update { it.copy(brightnessPercent = action.brightness.coerceIn(0.05f, 1f)) }
            is PlayerAction.StartGesture -> _state.update { it.copy(activeGesture = action.type, gestureValue = action.value) }
            is PlayerAction.UpdateGesture -> _state.update { it.copy(gestureValue = action.value) }
            PlayerAction.EndGesture -> _state.update { it.copy(activeGesture = GestureType.NONE) }
            is PlayerAction.SetSpeedBoost -> transport.setSpeedBoost(action.enabled)
            is PlayerAction.ShowToast -> effects.showToast(action.message)
            PlayerAction.ClearToast -> _state.update { it.copy(toastMessage = null) }
            is PlayerAction.SetQueueOpen -> _state.update { it.copy(isQueueOpen = action.isOpen) }
            is PlayerAction.SetSettingsOpen -> _state.update { it.copy(isSettingsOpen = action.isOpen, activeSettingsTab = action.tab) }
            is PlayerAction.SetSettingsTab -> _state.update { it.copy(activeSettingsTab = action.tab) }
            is PlayerAction.AdjustAudioDelay -> transport.adjustAudioDelay(action.deltaMs)
            is PlayerAction.AdjustSubtitleDelay -> transport.adjustSubtitleDelay(action.deltaMs)
            PlayerAction.ResetDelays -> transport.resetDelays()
            is PlayerAction.PlayTrack -> setInitialVideo(action.video, _state.value.playlist)
            PlayerAction.PlayNext -> queue.playNext()
            PlayerAction.PlayPrevious -> queue.playPrevious()
            PlayerAction.ToggleFullscreen -> _state.update { it.copy(isFullscreen = !it.isFullscreen) }
            PlayerAction.ClosePlayer -> {}
            PlayerAction.ToggleShuffle -> queue.toggleShuffle()
            PlayerAction.CycleRepeatMode -> queue.cycleRepeatMode()
            PlayerAction.ToggleAutoplay -> _state.update { it.copy(isAutoplay = !it.isAutoplay) }
            is PlayerAction.RemoveFromQueue -> _state.update { it.copy(playlist = PlayerQueueDelegate.removeTrack(it.playlist, action.videoId)) }
            PlayerAction.ClearQueue -> queue.clearQueue()
            is PlayerAction.SetSpeedPickerOpen -> _state.update { it.copy(isSpeedPickerOpen = action.isOpen) }
            PlayerAction.ToggleDialogueClarity -> applyDelegateResult(PlayerSettingsDelegate.toggleDialogueClarity(_state.value))
            PlayerAction.ToggleAmbientGlow -> applyDelegateResult(PlayerSettingsDelegate.toggleAmbientGlow(_state.value))
            PlayerAction.ToggleBackgroundPlay -> applyDelegateResult(PlayerSettingsDelegate.toggleBackgroundPlay(_state.value))
            is PlayerAction.SetVolumeBoost -> transport.setVolumeBoost(action.percent)
            is PlayerAction.SetSleepTimer -> effects.setSleepTimer(action.minutes)
            is PlayerAction.SelectAudioTrack -> transport.selectAudioTrack(action.track)
            is PlayerAction.SelectSubtitleTrack -> transport.selectSubtitleTrack(action.track)
            is PlayerAction.SetSubtitleSize -> _state.update { it.copy(subtitleSize = action.size) }
            is PlayerAction.SetAspectRatio -> transport.setAspectRatio(action.mode)
            is PlayerAction.SetDecoderMode -> transport.setDecoderMode(action.mode)
            is PlayerAction.SetSpeedBoostValue -> transport.setSpeedBoostValue(action.speed)
        }
    }

    private fun applyDelegateResult(result: Pair<PlayerState, String>) {
        _state.value = result.first
        effects.showToast(result.second)
    }

    fun resetControlsTimer() = effects.resetControlsTimer()

    override fun onCleared() {
        super.onCleared()
        engine?.release()
    }
}
