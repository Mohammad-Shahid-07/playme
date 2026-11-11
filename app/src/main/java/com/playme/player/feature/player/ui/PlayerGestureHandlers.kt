package com.playme.player.feature.player.ui

import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playme.player.feature.player.model.GestureType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

internal fun handleMove(
    change: PointerInputChange,
    runtime: PlayerGestureRuntime,
    values: PlayerGestureValues,
    actions: PlayerGestureActions,
    density: Density,
    touchSlop: Float
) {
    val dx = change.position.x - runtime.downX
    val dy = change.position.y - runtime.downY

    if (runtime.boostActive) {
        val stepPx = with(density) { BOOST_STEP_DP.dp.toPx() }.coerceAtLeast(1f)
        val index = (runtime.boostBaseIndex + (dx / stepPx).roundToInt())
            .coerceIn(0, BOOST_PRESETS.lastIndex)
        if (index != runtime.boostIndex) {
            runtime.boostIndex = index
            actions.onBoostSpeedChange(BOOST_PRESETS[index])
        }
        change.consume()
        return
    }

    if (!runtime.dragging) {
        if (hypot(dx, dy) <= touchSlop) return
        runtime.moved = true
        runtime.longPressJob?.cancel()

        val absX = abs(dx)
        val absY = abs(dy)
        val verticalSlop = with(density) { VERTICAL_DEADZONE_DP.dp.toPx() }
        val coef = if (absX < 1f) Float.MAX_VALUE else absY / absX

        when {
            coef > 2f && absY >= verticalSlop -> startVertical(runtime, actions)
            absX > touchSlop -> {
                if (values.durationMs <= 0L) return
                runtime.dragging = true
                runtime.axis = DragAxis.HORIZONTAL_SEEK
                runtime.startPositionMs = values.positionMs
                runtime.seekTargetMs = values.positionMs
                actions.onGestureStart(GestureType.SEEK, 0f)
            }
            else -> return
        }
    }

    when (runtime.axis) {
        DragAxis.VERTICAL_BRIGHTNESS -> {
            val value = (runtime.startBrightness - dy / runtime.height * VERTICAL_SENSITIVITY)
                .coerceIn(0.05f, 1f)
            actions.onGestureUpdate(GestureType.BRIGHTNESS, value)
        }
        DragAxis.VERTICAL_VOLUME -> {
            val value = (runtime.startVolume - dy / runtime.height * VERTICAL_SENSITIVITY)
                .coerceIn(0f, 1f)
            actions.onGestureUpdate(GestureType.VOLUME, value)
        }
        DragAxis.HORIZONTAL_SEEK -> {
            val range = (values.durationMs * SEEK_RANGE_FRACTION)
                .coerceIn(MIN_SEEK_RANGE_MS, MAX_SEEK_RANGE_MS)
            val target = (runtime.startPositionMs + (dx / runtime.width * range).toLong())
                .coerceIn(0L, values.durationMs)
            runtime.seekTargetMs = target
            actions.onGestureUpdate(
                GestureType.SEEK,
                (target - runtime.startPositionMs).toFloat()
            )
        }
        DragAxis.NONE -> return
    }
    change.consume()
}

private fun startVertical(runtime: PlayerGestureRuntime, actions: PlayerGestureActions) {
    runtime.dragging = true
    if (runtime.downX < runtime.width / 2f) {
        runtime.axis = DragAxis.VERTICAL_BRIGHTNESS
        actions.onGestureStart(GestureType.BRIGHTNESS, runtime.startBrightness)
    } else {
        runtime.axis = DragAxis.VERTICAL_VOLUME
        actions.onGestureStart(GestureType.VOLUME, runtime.startVolume)
    }
}

internal fun handleUp(
    up: PointerInputChange,
    runtime: PlayerGestureRuntime,
    isLocked: Boolean,
    doubleTapTimeout: Long,
    scope: CoroutineScope,
    actions: PlayerGestureActions
) {
    if (runtime.boostActive) {
        runtime.boostActive = false
        actions.onSpeedBoost(false)
        return
    }

    if (runtime.dragging) {
        if (runtime.axis == DragAxis.HORIZONTAL_SEEK) {
            actions.onSeekCommit(runtime.seekTargetMs)
        }
        actions.onGestureEnd()
        runtime.dragging = false
        return
    }

    if (isLocked) {
        actions.onLockedTouchAlert()
        return
    }

    if (runtime.moved) return

    val now = up.uptimeMillis
    if (runtime.lastTapUpTime > 0L && now - runtime.lastTapUpTime <= doubleTapTimeout) {
        runtime.lastTapUpTime = 0L
        val x = up.position.x
        val edge = runtime.width * DOUBLE_TAP_EDGE_FRACTION
        when {
            x < edge -> actions.onDoubleTapSeek(-DOUBLE_TAP_SEEK_MS)
            x > runtime.width - edge -> actions.onDoubleTapSeek(DOUBLE_TAP_SEEK_MS)
            else -> actions.onTogglePlay()
        }
        return
    }

    runtime.lastTapUpTime = now
    runtime.singleTapJob = scope.launch {
        delay(doubleTapTimeout)
        actions.onSingleTap()
    }
}

internal fun cancelActiveGesture(runtime: PlayerGestureRuntime, actions: PlayerGestureActions) {
    if (runtime.boostActive) {
        runtime.boostActive = false
        actions.onSpeedBoost(false)
    }
    if (runtime.dragging) {
        runtime.dragging = false
        actions.onGestureEnd()
    }
}

private const val BOOST_STEP_DP = 38f
private const val VERTICAL_DEADZONE_DP = 24f
private const val VERTICAL_SENSITIVITY = 1.15f
private const val SEEK_RANGE_FRACTION = 0.25f
private const val MIN_SEEK_RANGE_MS = 30_000f
private const val MAX_SEEK_RANGE_MS = 180_000f
private const val DOUBLE_TAP_EDGE_FRACTION = 0.30f
private const val DOUBLE_TAP_SEEK_MS = 10_000L
