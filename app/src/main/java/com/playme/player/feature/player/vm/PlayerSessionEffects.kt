package com.playme.player.feature.player.vm

import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.feature.player.model.GestureType
import com.playme.player.feature.player.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the short-lived UI effects of the player: toast messages, the HUD
 * auto-hide countdown, and the sleep timer. Keeping the coroutine Jobs here
 * stops the ViewModel from becoming a timer dumping ground.
 */
class PlayerSessionEffects(
    private val state: MutableStateFlow<PlayerState>,
    private val scope: CoroutineScope,
    private val engine: () -> PlayerEngineContract?
) {

    private var autoHideJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var seekFlashJob: Job? = null
    private var toastJob: Job? = null

    fun resetControlsTimer() {
        autoHideJob?.cancel()
        if (!state.value.isPlaying || !state.value.isControlsVisible || state.value.isLocked) return
        autoHideJob = scope.launch {
            delay(CONTROLS_AUTO_HIDE_MS)
            state.update { it.copy(isControlsVisible = false) }
        }
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        state.update { it.copy(toastMessage = message) }
        toastJob = scope.launch {
            delay(TOAST_DURATION_MS)
            state.update { if (it.toastMessage == message) it.copy(toastMessage = null) else it }
        }
    }

    /**
     * Briefly mirrors a double-tap seek in the gesture HUD so the jump has the
     * same visual feedback as a swipe.
     */
    fun flashSeek(deltaMs: Long, targetMs: Long) {
        seekFlashJob?.cancel()
        state.update {
            it.copy(
                activeGesture = GestureType.SEEK,
                gestureValue = deltaMs.toFloat(),
                seekPreviewPositionMs = targetMs.coerceIn(0L, it.durationMs)
            )
        }
        seekFlashJob = scope.launch {
            delay(SEEK_FLASH_MS)
            state.update {
                if (it.activeGesture == GestureType.SEEK) {
                    it.copy(activeGesture = GestureType.NONE)
                } else {
                    it
                }
            }
        }
    }

    fun cancelSeekFlash() {
        seekFlashJob?.cancel()
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val next = PlayerPlaybackCoordinator.getNextSleepTimer(state.value.sleepTimerMinutes, minutes)
        state.update { it.copy(sleepTimerMinutes = next) }

        if (next > 0) {
            showToast("Sleep Timer: ${next}m")
            sleepTimerJob = scope.launch {
                delay(next * 60_000L)
                engine()?.pause()
                state.update { it.copy(isPlaying = false, sleepTimerMinutes = 0) }
                showToast("Sleep Timer: Playback Paused")
            }
        } else {
            showToast("Sleep Timer Off")
        }
    }

    private companion object {
        const val CONTROLS_AUTO_HIDE_MS = 3500L
        const val TOAST_DURATION_MS = 2000L
        const val SEEK_FLASH_MS = 650L
    }
}
