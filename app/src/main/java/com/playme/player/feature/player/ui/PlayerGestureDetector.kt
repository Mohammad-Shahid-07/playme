package com.playme.player.feature.player.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * VLC-style gesture arbitration:
 * - tap toggles the HUD after the double-tap window
 * - double tap on the outer thirds seeks, the center toggles play/pause
 * - vertical swipe on the left adjusts brightness, on the right the volume
 * - horizontal swipe scrubs the timeline and commits on release
 * - hold for 280 ms enables a temporary speed boost; sliding picks the speed
 */
internal fun Modifier.playerGestureInput(
    scope: CoroutineScope,
    values: State<PlayerGestureValues>,
    actions: State<PlayerGestureActions>
): Modifier = pointerInput(Unit) {
    val density = this
    val touchSlop = viewConfiguration.touchSlop
    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
    val runtime = PlayerGestureRuntime()

    try {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()

            runtime.onDown(down.position, values.value, size.width.toFloat(), size.height.toFloat())

            if (!values.value.isLocked) {
                runtime.longPressJob = scope.launch {
                    delay(LONG_PRESS_MS)
                    if (runtime.pointerDown && !runtime.moved && !runtime.dragging) {
                        runtime.boostActive = true
                        actions.value.onSpeedBoost(true)
                    }
                }
            }

            var upChange: PointerInputChange? = null
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) {
                    upChange = change
                    break
                }
                if (!values.value.isLocked) {
                    handleMove(change, runtime, values.value, actions.value, density, touchSlop)
                }
            }

            runtime.pointerDown = false
            runtime.longPressJob?.cancel()

            if (upChange == null) {
                cancelActiveGesture(runtime, actions.value)
            } else {
                handleUp(
                    upChange,
                    runtime,
                    values.value.isLocked,
                    doubleTapTimeout,
                    scope,
                    actions.value
                )
            }
        }
    } finally {
        runtime.pointerDown = false
        runtime.longPressJob?.cancel()
        runtime.singleTapJob?.cancel()
        if (runtime.boostActive) {
            runtime.boostActive = false
            actions.value.onSpeedBoost(false)
        }
        if (runtime.dragging) {
            actions.value.onGestureEnd()
        }
    }
}

private const val LONG_PRESS_MS = 280L
