package com.playme.player.feature.player.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.model.VideoItem
import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.core.player.PlayerEngineListener
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

    private var progressRecorder: ((VideoItem, Long, Long) -> Unit)? = null

    private val effects = PlayerSessionEffects(_state, viewModelScope) { engine }
    private val transport = PlayerTransportController(_state, { engine }, effects)
    private val queue = PlayerQueueController(
        state = _state,
        effects = effects,
        transport = transport,
        onPlaybackCompleted = { recordCurrentProgress(completed = true) }
    ) { video, playlist ->
        setInitialVideo(video, playlist)
    }

    init {
        resetControlsTimer()
    }

    fun setProgressRecorder(recorder: ((VideoItem, Long, Long) -> Unit)?) {
        progressRecorder = recorder
    }

    fun attachEngine(playerEngine: PlayerEngineContract?) {
        if (this.engine === playerEngine) return
        this.engine?.setListener(NO_OP_LISTENER)
        this.engine = playerEngine
        if (playerEngine == null) return

        playerEngine.setListener(
            PlayerEngineBridge.createEngineListener(
                state = _state,
                updateState = { updater -> _state.update(updater) },
                onSeekTo = { transport.seekTo(it) },
                onShowToast = { effects.showToast(it) },
                onPlaybackEnded = { queue.onPlaybackEnded() },
                isCurrentEngine = { this.engine === playerEngine }
            )
        )
        applySessionSettings(playerEngine, _state.value)
    }

    /**
     * Publishes the selected video to the UI before the engine exists, so the
     * player screen can paint the cached poster on its very first frame
     * instead of showing an empty "Unknown Video" shell.
     */
    fun prepareVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        applyVideoSelection(video, playlist, resetQueueMode = true)
    }

    fun setInitialVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        val prev = _state.value
        val prevVideo = prev.currentVideo
        if (prevVideo != null && prevVideo.id != video.id && prev.durationMs > 0L && prev.currentPositionMs > 0L) {
            progressRecorder?.invoke(prevVideo, prev.currentPositionMs, prev.durationMs)
        }
        applyVideoSelection(video, playlist, resetQueueMode = false)
        engine?.playVideo(video)
        resetControlsTimer()
    }

    fun recordCurrentProgress(completed: Boolean = false) {
        val snapshot = _state.value
        val video = snapshot.currentVideo ?: return
        val dur = snapshot.durationMs
        if (dur <= 0L) return
        val pos = if (completed) dur else snapshot.currentPositionMs
        progressRecorder?.invoke(video, pos, dur)
    }

    private fun applyVideoSelection(video: VideoItem, playlist: List<VideoItem>, resetQueueMode: Boolean) {
        _state.value = PlayerVideoSelection.apply(_state.value, video, playlist, resetQueueMode)
    }

    private fun applySessionSettings(target: PlayerEngineContract, snapshot: PlayerState) {
        target.setDecoderMode(snapshot.decoderMode)
        target.setSpeed(if (snapshot.isSpeedBoostActive) snapshot.boostSpeed else snapshot.playbackSpeed)
        target.setAspectRatio(snapshot.aspectRatioMode)
        target.setVolume(1.0f)
        target.setMuted(snapshot.isMuted)
        target.setVolumeBoost(snapshot.volumeBoostPercent)
        target.setAudioDelay(snapshot.audioDelayMs)
        target.setSubtitleDelay(snapshot.subtitleDelayMs)
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.TogglePlay -> transport.togglePlay()
            PlayerAction.Play -> transport.play()
            PlayerAction.Pause -> transport.pause()
            is PlayerAction.SeekTo -> transport.seekTo(action.positionMs)
            is PlayerAction.SeekBy -> {
                val before = _state.value
                val target = (before.currentPositionMs + action.deltaMs)
                    .coerceIn(0L, before.durationMs)
                transport.seekTo(target)
                if (target != before.currentPositionMs) {
                    effects.flashSeek(target - before.currentPositionMs, target)
                }
            }
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
            is PlayerAction.SyncSystemVolume -> transport.syncSystemVolume(action.volume)
            is PlayerAction.SetBrightness -> transport.setBrightness(action.brightness)
            is PlayerAction.StartGesture -> _state.update { state ->
                if (action.type == GestureType.SEEK) {
                    effects.cancelSeekFlash()
                    state.copy(
                        activeGesture = GestureType.SEEK,
                        gestureValue = 0f,
                        seekPreviewPositionMs = state.currentPositionMs
                    )
                } else {
                    val value = if (action.value < 0f &&
                        (action.type == GestureType.BRIGHTNESS || action.type == GestureType.VOLUME)
                    ) 0.5f else action.value
                    state.copy(activeGesture = action.type, gestureValue = value)
                }
            }
            is PlayerAction.UpdateGesture -> when (action.type) {
                GestureType.VOLUME -> transport.setVolume(action.value)
                GestureType.BRIGHTNESS -> transport.setBrightness(action.value)
                GestureType.SEEK -> _state.update { state ->
                    val deltaMs = (action.value - state.gestureValue).toLong()
                    val target = (state.seekPreviewPositionMs + deltaMs)
                        .coerceIn(0L, state.durationMs)
                    state.copy(
                        gestureValue = action.value,
                        seekPreviewPositionMs = target
                    )
                }
                else -> _state.update { it.copy(gestureValue = action.value) }
            }
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
            PlayerAction.ToggleFullscreen -> _state.update {
                val targetLandscape = !it.isFullscreen
                it.copy(isFullscreen = targetLandscape, orientationOverride = targetLandscape)
            }
            is PlayerAction.SyncConfigurationOrientation -> _state.update {
                if (it.orientationOverride == null || it.orientationOverride == action.isLandscape) {
                    it.copy(isFullscreen = action.isLandscape)
                } else {
                    it
                }
            }
            PlayerAction.ClearOrientationOverride -> _state.update { it.copy(orientationOverride = null) }
            PlayerAction.ClosePlayer -> {}
            PlayerAction.ToggleShuffle -> queue.toggleShuffle()
            PlayerAction.CycleRepeatMode -> queue.cycleRepeatMode()
            PlayerAction.ToggleAutoplay -> _state.update { it.copy(isAutoplay = !it.isAutoplay) }
            is PlayerAction.RemoveFromQueue -> queue.removeFromQueue(action.videoId)
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
        engine?.setListener(NO_OP_LISTENER)
        engine?.release()
        engine = null
    }

    private companion object {
        val NO_OP_LISTENER = object : PlayerEngineListener {
            override fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long) {}
            override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {}
            override fun onSubtitleCue(cueText: String?) {}
            override fun onError(errorMessage: String) {}
        }
    }
}
