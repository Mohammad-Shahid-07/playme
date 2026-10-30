package com.playme.player.feature.player.vm

import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import com.playme.player.feature.player.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Applies transport and live-playback changes to state and the active engine.
 * Every method is a single user intent, which keeps the ViewModel's action
 * router declarative.
 */
class PlayerTransportController(
    private val state: MutableStateFlow<PlayerState>,
    private val engine: () -> PlayerEngineContract?,
    private val effects: PlayerSessionEffects
) {

    fun play() {
        state.update { it.copy(isPlaying = true) }
        engine()?.play()
    }

    fun pause() {
        state.update { it.copy(isPlaying = false) }
        engine()?.pause()
    }

    fun togglePlay() {
        val next = !state.value.isPlaying
        state.update { it.copy(isPlaying = next) }
        if (next) engine()?.play() else engine()?.pause()
        effects.resetControlsTimer()
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, state.value.durationMs)
        state.update { it.copy(currentPositionMs = clamped) }
        engine()?.seekTo(clamped)
        effects.resetControlsTimer()
    }

    fun seekBy(deltaMs: Long) =
        seekTo((state.value.currentPositionMs + deltaMs).coerceIn(0L, state.value.durationMs))

    fun setSpeed(speed: Float) {
        state.update { it.copy(playbackSpeed = speed) }
        engine()?.setSpeed(speed)
        effects.showToast("Speed: ${speed}x")
    }

    fun toggleMute() {
        val muted = !state.value.isMuted
        state.update { it.copy(isMuted = muted) }
        engine()?.setMuted(muted)
        effects.showToast(if (muted) "Muted" else "Unmuted")
    }

    fun toggleLock() {
        val locked = !state.value.isLocked
        state.update { it.copy(isLocked = locked, isControlsVisible = !locked) }
        effects.showToast(if (locked) "Controls Locked" else "Controls Unlocked")
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        state.update { it.copy(aspectRatioMode = mode) }
        engine()?.setAspectRatio(mode)
        effects.showToast(mode.label)
    }

    fun setDecoderMode(mode: DecoderMode) {
        state.update { it.copy(decoderMode = mode) }
        engine()?.setDecoderMode(mode)
        effects.showToast("Decoder: ${mode.name}")
    }

    fun toggleControls() {
        if (state.value.isLocked) {
            effects.showToast("Controls Locked")
            return
        }
        val visible = !state.value.isControlsVisible
        state.update { it.copy(isControlsVisible = visible) }
        if (visible) effects.resetControlsTimer()
    }

    fun showControls() {
        if (state.value.isLocked) return
        state.update { it.copy(isControlsVisible = true) }
        effects.resetControlsTimer()
    }

    fun toggleAbLoop() {
        val nextState = PlayerSettingsDelegate.createAbLoopState(state.value)
        state.value = nextState
        effects.showToast(if (nextState.isAbLoopActive) "A-B Loop Active" else "A-B Loop Off")
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        state.update { it.copy(volumePercent = clamped) }
        engine()?.setVolume(clamped)
    }

    fun setSpeedBoost(enabled: Boolean) {
        state.update { it.copy(isSpeedBoostActive = enabled) }
        engine()?.setSpeed(if (enabled) state.value.boostSpeed else state.value.playbackSpeed)
    }

    fun setSpeedBoostValue(speed: Float) {
        state.update { it.copy(boostSpeed = speed) }
        if (state.value.isSpeedBoostActive) engine()?.setSpeed(speed)
    }

    fun setVolumeBoost(percent: Int) {
        state.update { it.copy(volumeBoostPercent = percent) }
        engine()?.setVolumeBoost(percent)
        effects.showToast("Volume Boost: $percent%")
    }

    fun adjustAudioDelay(deltaMs: Long) =
        applyDelegateResult(PlayerSettingsDelegate.applyAudioDelay(state.value, deltaMs, engine()))

    fun adjustSubtitleDelay(deltaMs: Long) =
        applyDelegateResult(PlayerSettingsDelegate.applySubtitleDelay(state.value, deltaMs, engine()))

    fun resetDelays() =
        applyDelegateResult(PlayerSettingsDelegate.resetDelays(state.value, engine()))

    fun selectAudioTrack(track: String) =
        applyDelegateResult(PlayerSettingsDelegate.selectAudio(state.value, track, engine()))

    fun selectSubtitleTrack(track: String) =
        applyDelegateResult(PlayerSettingsDelegate.selectSubtitle(state.value, track, engine()))

    private fun applyDelegateResult(result: Pair<PlayerState, String>) {
        state.value = result.first
        effects.showToast(result.second)
    }
}
