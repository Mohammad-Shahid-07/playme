package com.playme.player.feature.player.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.model.VideoItem
import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.core.player.PlayerEngineListener
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import com.playme.player.feature.player.model.GestureType
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.model.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var autoHideControlsJob: Job? = null
    private val speedPresets = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.5f)
    var engine: PlayerEngineContract? = null
        private set

    init {
        resetControlsTimer()
    }

    fun attachEngine(playerEngine: PlayerEngineContract?) {
        if (this.engine === playerEngine) return
        this.engine = playerEngine
        if (playerEngine == null) return

        playerEngine.setListener(object : PlayerEngineListener {
            override fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long) {
                _state.update {
                    it.copy(
                        currentPositionMs = positionMs,
                        durationMs = if (durationMs > 0) durationMs else it.durationMs,
                        bufferedPositionMs = bufferMs
                    )
                }
            }

            override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onSubtitleCue(cueText: String?) {
                _state.update { it.copy(currentSubtitle = cueText) }
            }

            override fun onError(errorMessage: String) {
                showToast(errorMessage)
            }

            override fun onEngineSwitched(engineType: com.playme.player.core.player.EngineType) {
                val label = if (engineType == com.playme.player.core.player.EngineType.VLC) "Universal Codec Engine (LibVLC)" else "Hardware Engine (Media3)"
                showToast("Using $label")
            }
        })
        _state.value.currentVideo?.let { playerEngine.playVideo(it) }
    }

    fun setInitialVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        val idx = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
        _state.update {
            it.copy(
                currentVideo = video,
                playlist = playlist,
                currentIndex = idx,
                durationMs = video.durationMs.coerceAtLeast(10_000L),
                currentPositionMs = 0L,
                bufferedPositionMs = 0L
            )
        }
        engine?.playVideo(video)
        resetControlsTimer()
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.TogglePlay -> togglePlay()
            PlayerAction.Play -> { _state.update { it.copy(isPlaying = true) }; engine?.play() }
            PlayerAction.Pause -> { _state.update { it.copy(isPlaying = false) }; engine?.pause() }
            is PlayerAction.SeekTo -> seekTo(action.positionMs)
            is PlayerAction.SeekBy -> seekBy(action.deltaMs)
            is PlayerAction.SetSpeed -> setSpeed(action.speed)
            PlayerAction.CycleSpeed -> cycleSpeed()
            PlayerAction.ToggleMute -> toggleMute()
            PlayerAction.ToggleLock -> toggleLock()
            PlayerAction.CycleAspectRatio -> cycleAspectRatio()
            PlayerAction.ToggleDecoderMode -> toggleDecoderMode()
            PlayerAction.ToggleControls -> toggleControls()
            PlayerAction.HideControls -> hideControls()
            PlayerAction.ShowControls -> showControls()
            PlayerAction.ToggleAbLoop -> toggleAbLoop()
            is PlayerAction.SetVolume -> setVolume(action.volume)
            is PlayerAction.SetBrightness -> setBrightness(action.brightness)
            is PlayerAction.StartGesture -> startGesture(action.type, action.value)
            is PlayerAction.UpdateGesture -> updateGesture(action.value)
            PlayerAction.EndGesture -> endGesture()
            is PlayerAction.SetSpeedBoost -> setSpeedBoost(action.enabled)
            is PlayerAction.ShowToast -> showToast(action.message)
            PlayerAction.ClearToast -> _state.update { it.copy(toastMessage = null) }
            is PlayerAction.SetQueueOpen -> _state.update { it.copy(isQueueOpen = action.isOpen) }
            is PlayerAction.SetSettingsOpen -> _state.update { it.copy(isSettingsOpen = action.isOpen, activeSettingsTab = action.tab) }
            is PlayerAction.SetSettingsTab -> _state.update { it.copy(activeSettingsTab = action.tab) }
            is PlayerAction.AdjustAudioDelay -> adjustAudioDelay(action.deltaMs)
            is PlayerAction.AdjustSubtitleDelay -> adjustSubtitleDelay(action.deltaMs)
            PlayerAction.ResetDelays -> _state.update { it.copy(audioDelayMs = 0L, subtitleDelayMs = 0L) }
            is PlayerAction.PlayTrack -> playTrack(action.video)
            PlayerAction.PlayNext -> playNext()
            PlayerAction.PlayPrevious -> playPrevious()
            PlayerAction.ToggleFullscreen -> _state.update { it.copy(isFullscreen = !it.isFullscreen) }
            PlayerAction.ClosePlayer -> { /* Handled in Composable */ }
            PlayerAction.ToggleShuffle -> toggleShuffle()
            PlayerAction.CycleRepeatMode -> cycleRepeatMode()
            PlayerAction.ToggleAutoplay -> _state.update { it.copy(isAutoplay = !it.isAutoplay) }
            is PlayerAction.RemoveFromQueue -> removeFromQueue(action.videoId)
            PlayerAction.ClearQueue -> clearQueue()
            is PlayerAction.SetSpeedPickerOpen -> _state.update { it.copy(isSpeedPickerOpen = action.isOpen) }
            PlayerAction.ToggleDialogueClarity -> toggleDialogueClarity()
            is PlayerAction.SetVolumeBoost -> setVolumeBoost(action.percent)
            PlayerAction.ToggleAmbientGlow -> toggleAmbientGlow()
            is PlayerAction.SetSleepTimer -> setSleepTimer(action.minutes)
            PlayerAction.ToggleBackgroundPlay -> toggleBackgroundPlay()
            is PlayerAction.SelectAudioTrack -> _state.update { it.copy(selectedAudioTrack = action.track) }
            is PlayerAction.SelectSubtitleTrack -> _state.update { it.copy(selectedSubtitleTrack = action.track) }
            is PlayerAction.SetSubtitleSize -> _state.update { it.copy(subtitleSize = action.size) }
            is PlayerAction.SetAspectRatio -> {
                _state.update { it.copy(aspectRatioMode = action.mode) }
                showToast(action.mode.label)
            }
            is PlayerAction.SetDecoderMode -> {
                _state.update { it.copy(decoderMode = action.mode) }
                engine?.setDecoderMode(action.mode)
                showToast("Decoder: ${action.mode.name}")
            }
            is PlayerAction.SetSpeedBoostValue -> {
                _state.update { it.copy(boostSpeed = action.speed) }
                if (_state.value.isSpeedBoostActive) {
                    engine?.setSpeed(action.speed)
                }
            }
        }
    }

    private fun togglePlay() {
        val next = !_state.value.isPlaying
        _state.update { it.copy(isPlaying = next) }
        if (next) engine?.play() else engine?.pause()
        resetControlsTimer()
    }

    private fun seekTo(posMs: Long) {
        val clamped = posMs.coerceIn(0L, _state.value.durationMs)
        _state.update { it.copy(currentPositionMs = clamped) }
        engine?.seekTo(clamped)
        resetControlsTimer()
    }

    private fun seekBy(deltaMs: Long) {
        val next = (_state.value.currentPositionMs + deltaMs).coerceIn(0L, _state.value.durationMs)
        seekTo(next)
    }

    private fun setSpeed(speed: Float) {
        _state.update { it.copy(playbackSpeed = speed) }
        engine?.setSpeed(speed)
        showToast("Speed: ${speed}x")
    }

    private fun cycleSpeed() {
        val currentSpeed = _state.value.playbackSpeed
        val nextIdx = (speedPresets.indexOf(currentSpeed) + 1) % speedPresets.size
        setSpeed(speedPresets[nextIdx])
    }

    private fun toggleMute() {
        val muted = !_state.value.isMuted
        _state.update { it.copy(isMuted = muted) }
        engine?.setMuted(muted)
        showToast(if (muted) "Muted" else "Unmuted")
    }

    private fun toggleLock() {
        val locked = !_state.value.isLocked
        _state.update { it.copy(isLocked = locked, isControlsVisible = !locked) }
        showToast(if (locked) "Controls Locked" else "Controls Unlocked")
    }

    private fun cycleAspectRatio() {
        val all = AspectRatioMode.entries
        val next = all[(all.indexOf(_state.value.aspectRatioMode) + 1) % all.size]
        _state.update { it.copy(aspectRatioMode = next) }
        showToast(next.label)
    }

    private fun toggleDecoderMode() {
        val next = if (_state.value.decoderMode == DecoderMode.HW) DecoderMode.SW else DecoderMode.HW
        _state.update { it.copy(decoderMode = next) }
        engine?.setDecoderMode(next)
        showToast("Decoder: ${next.name}")
    }

    private fun toggleControls() {
        if (_state.value.isLocked) {
            showToast("Controls Locked")
            return
        }
        val visible = !_state.value.isControlsVisible
        _state.update { it.copy(isControlsVisible = visible) }
        if (visible) resetControlsTimer()
    }

    private fun hideControls() {
        _state.update { it.copy(isControlsVisible = false) }
    }

    private fun showControls() {
        if (!_state.value.isLocked) {
            _state.update { it.copy(isControlsVisible = true) }
            resetControlsTimer()
        }
    }

    private fun toggleAbLoop() {
        val active = !_state.value.isAbLoopActive
        _state.update {
            it.copy(
                isAbLoopActive = active,
                abLoopStartMs = if (active) it.currentPositionMs else null,
                abLoopEndMs = if (active) (it.currentPositionMs + 5000L).coerceAtMost(it.durationMs) else null
            )
        }
        showToast(if (active) "A-B Loop Active" else "A-B Loop Off")
    }

    private fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _state.update { it.copy(volumePercent = clamped) }
        engine?.setVolume(clamped)
    }

    private fun setBrightness(b: Float) {
        _state.update { it.copy(brightnessPercent = b.coerceIn(0.05f, 1f)) }
    }

    private fun startGesture(type: GestureType, value: Float) {
        _state.update { it.copy(activeGesture = type, gestureValue = value) }
    }

    private fun updateGesture(value: Float) {
        _state.update { it.copy(gestureValue = value) }
    }

    private fun endGesture() {
        _state.update { it.copy(activeGesture = GestureType.NONE) }
    }

    private fun setSpeedBoost(enabled: Boolean) {
        _state.update { it.copy(isSpeedBoostActive = enabled) }
        engine?.setSpeed(if (enabled) _state.value.boostSpeed else _state.value.playbackSpeed)
    }

    private fun adjustAudioDelay(deltaMs: Long) {
        val next = PlayerDelaysDelegate.stepAudio(_state.value.audioDelayMs, deltaMs)
        _state.update { it.copy(audioDelayMs = next) }
        showToast("Audio delay: ${next}ms")
    }

    private fun adjustSubtitleDelay(deltaMs: Long) {
        val next = PlayerDelaysDelegate.stepSubtitle(_state.value.subtitleDelayMs, deltaMs)
        _state.update { it.copy(subtitleDelayMs = next) }
        showToast("Subtitle delay: ${next}ms")
    }

    private fun toggleShuffle() {
        val next = !_state.value.isShuffle
        val updated = if (next) PlayerQueueDelegate.shuffleQueue(_state.value.playlist, _state.value.currentVideo) else _state.value.playlist
        _state.update { it.copy(isShuffle = next, playlist = updated) }
        showToast(if (next) "Shuffle ON" else "Shuffle OFF")
    }

    private fun cycleRepeatMode() {
        val next = PlayerQueueDelegate.cycleRepeat(_state.value.repeatMode)
        _state.update { it.copy(repeatMode = next) }
        showToast("Repeat: $next")
    }

    private fun removeFromQueue(id: String) {
        val updated = PlayerQueueDelegate.removeTrack(_state.value.playlist, id)
        _state.update { it.copy(playlist = updated) }
    }

    private fun clearQueue() {
        val current = _state.value.currentVideo
        _state.update { it.copy(playlist = if (current != null) listOf(current) else emptyList()) }
        showToast("Queue Cleared")
    }

    private fun toggleDialogueClarity() {
        val active = !_state.value.isDialogueClarityActive
        _state.update { it.copy(isDialogueClarityActive = active) }
        showToast(if (active) "Dialogue Clarity ON" else "Dialogue Clarity OFF")
    }

    private fun setVolumeBoost(percent: Int) {
        _state.update { it.copy(volumeBoostPercent = percent) }
        showToast("Volume Boost: $percent%")
    }

    private fun toggleAmbientGlow() {
        val active = !_state.value.ambientGlowEnabled
        _state.update { it.copy(ambientGlowEnabled = active) }
        showToast(if (active) "Ambilight ON" else "Ambilight OFF")
    }

    private fun setSleepTimer(minutes: Int) {
        val next = if (minutes == -1) {
            when (_state.value.sleepTimerMinutes) {
                0 -> 15
                15 -> 30
                30 -> 60
                else -> 0
            }
        } else minutes
        _state.update { it.copy(sleepTimerMinutes = next) }
        showToast(if (next == 0) "Sleep Timer Off" else "Sleep Timer: ${next}m")
    }

    private fun toggleBackgroundPlay() {
        val active = !_state.value.backgroundPlayEnabled
        _state.update { it.copy(backgroundPlayEnabled = active) }
        showToast(if (active) "Background Play ON" else "Background Play OFF")
    }

    private fun playTrack(video: VideoItem) {
        setInitialVideo(video, _state.value.playlist)
    }

    private fun playNext() {
        val list = _state.value.playlist
        if (list.isEmpty()) return
        val nextIdx = (_state.value.currentIndex + 1) % list.size
        playTrack(list[nextIdx])
    }

    private fun playPrevious() {
        val list = _state.value.playlist
        if (list.isEmpty()) return
        val prevIdx = if (_state.value.currentIndex - 1 < 0) list.size - 1 else _state.value.currentIndex - 1
        playTrack(list[prevIdx])
    }

    private fun showToast(msg: String) {
        _state.update { it.copy(toastMessage = msg) }
        viewModelScope.launch {
            delay(2000L)
            _state.update { if (it.toastMessage == msg) it.copy(toastMessage = null) else it }
        }
    }

    fun resetControlsTimer() {
        autoHideControlsJob?.cancel()
        if (!_state.value.isPlaying || !_state.value.isControlsVisible || _state.value.isLocked) return
        autoHideControlsJob = viewModelScope.launch {
            delay(3500L)
            _state.update { it.copy(isControlsVisible = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine?.release()
    }
}
