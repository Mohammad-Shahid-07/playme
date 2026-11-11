package com.playme.player.feature.player.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.playme.player.feature.player.model.GestureType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.WeakHashMap
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

private val lastTapPositions = WeakHashMap<PlayerGestureRuntime, Offset>()
private val lastTapDensities = WeakHashMap<PlayerGestureRuntime, Density>()

internal fun handleMove(
    change: PointerInputChange,
    runtime: PlayerGestureRuntime,
    values: PlayerGestureValues,
    actions: PlayerGestureActions,
    density: Density,
    touchSlop: Float
) {
    lastTapDensities[runtime] = density
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

        if (isInSystemEdgeDeadzone(runtime, density)) return

        val absX = abs(dx)
        val absY = abs(dy)
        val verticalSlop = with(density) { VERTICAL_DEADZONE_DP.dp.toPx() }
        val coef = if (absX < 1f) Float.MAX_VALUE else absY / absX

        when {
            coef > VERTICAL_SLOPE_MIN && absY >= verticalSlop -> startVertical(runtime, actions)
            absX > touchSlop && absX >= absY * HORIZONTAL_SLOPE_MIN -> {
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

    val safeHeight = runtime.height.coerceAtLeast(1f)
    val safeWidth = runtime.width.coerceAtLeast(1f)
    val effectiveBrightness = if (runtime.startBrightness < 0f) 0.5f else runtime.startBrightness
    val effectiveVolume = if (runtime.startVolume < 0f) 0.5f else runtime.startVolume

    when (runtime.axis) {
        DragAxis.VERTICAL_BRIGHTNESS -> {
            val value = (effectiveBrightness - dy / safeHeight * VERTICAL_SENSITIVITY)
                .coerceIn(0.05f, 1f)
            actions.onGestureUpdate(GestureType.BRIGHTNESS, value)
        }
        DragAxis.VERTICAL_VOLUME -> {
            val value = (effectiveVolume - dy / safeHeight * VERTICAL_SENSITIVITY)
                .coerceIn(0f, 1f)
            actions.onGestureUpdate(GestureType.VOLUME, value)
        }
        DragAxis.HORIZONTAL_SEEK -> {
            val range = (values.durationMs * SEEK_RANGE_FRACTION)
                .coerceIn(MIN_SEEK_RANGE_MS, MAX_SEEK_RANGE_MS)
            val target = (runtime.startPositionMs + (dx / safeWidth * range).toLong())
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

private fun isInSystemEdgeDeadzone(runtime: PlayerGestureRuntime, density: Density): Boolean {
    val w = runtime.width
    val h = runtime.height
    if (w <= 0f || h <= 0f) return false
    val horizPx = with(density) { EDGE_DEADZONE_HORIZONTAL_DP.dp.toPx() }
    val vertPx = with(density) { EDGE_DEADZONE_VERTICAL_DP.dp.toPx() }
    return runtime.downX < horizPx ||
        runtime.downX > w - horizPx ||
        runtime.downY < vertPx ||
        runtime.downY > h - vertPx
}

private fun startVertical(runtime: PlayerGestureRuntime, actions: PlayerGestureActions) {
    runtime.dragging = true
    if (runtime.downX < runtime.width / 2f) {
        val startVal = if (runtime.startBrightness < 0f) 0.5f else runtime.startBrightness
        runtime.axis = DragAxis.VERTICAL_BRIGHTNESS
        actions.onGestureStart(GestureType.BRIGHTNESS, startVal)
    } else {
        val startVal = if (runtime.startVolume < 0f) 0.5f else runtime.startVolume
        runtime.axis = DragAxis.VERTICAL_VOLUME
        actions.onGestureStart(GestureType.VOLUME, startVal)
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
    if (up.isConsumed) {
        cancelActiveGesture(runtime, actions)
        return
    }

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
        runtime.axis = DragAxis.NONE
        return
    }

    if (isLocked) {
        if (!runtime.moved) {
            actions.onLockedTouchAlert()
        }
        return
    }

    if (runtime.moved) return

    val now = up.uptimeMillis
    val prevPos = lastTapPositions[runtime]
    val doubleTapSlopPx = lastTapDensities[runtime]?.let { with(it) { DOUBLE_TAP_SLOP_DP.dp.toPx() } }
        ?: Float.MAX_VALUE
    val isWithinDoubleTapDistance = prevPos != null &&
        hypot(up.position.x - prevPos.x, up.position.y - prevPos.y) <= doubleTapSlopPx

    if (runtime.lastTapUpTime > 0L &&
        now - runtime.lastTapUpTime <= doubleTapTimeout &&
        isWithinDoubleTapDistance
    ) {
        runtime.singleTapJob?.cancel()
        runtime.lastTapUpTime = 0L
        lastTapPositions.remove(runtime)
        val width = runtime.width.coerceAtLeast(1f)
        val x = up.position.x
        val edge = width * DOUBLE_TAP_EDGE_FRACTION
        when {
            x < edge -> actions.onDoubleTapSeek(-DOUBLE_TAP_SEEK_MS)
            x > width - edge -> actions.onDoubleTapSeek(DOUBLE_TAP_SEEK_MS)
            else -> actions.onTogglePlay()
        }
        return
    }

    runtime.singleTapJob?.cancel()
    runtime.lastTapUpTime = now
    lastTapPositions[runtime] = up.position
    runtime.singleTapJob = scope.launch {
        delay(doubleTapTimeout)
        if (runtime.lastTapUpTime == now) {
            runtime.lastTapUpTime = 0L
            lastTapPositions.remove(runtime)
            actions.onSingleTap()
        }
    }
}

internal fun cancelActiveGesture(runtime: PlayerGestureRuntime, actions: PlayerGestureActions) {
    runtime.longPressJob?.cancel()
    runtime.singleTapJob?.cancel()
    if (runtime.boostActive) {
        runtime.boostActive = false
        actions.onSpeedBoost(false)
    }
    if (runtime.dragging) {
        runtime.dragging = false
        runtime.axis = DragAxis.NONE
        actions.onGestureEnd()
    }
}

private const val BOOST_STEP_DP = 38f
private const val VERTICAL_DEADZONE_DP = 24f
private const val EDGE_DEADZONE_HORIZONTAL_DP = 24f
private const val EDGE_DEADZONE_VERTICAL_DP = 32f
private const val VERTICAL_SLOPE_MIN = 2.0f
private const val HORIZONTAL_SLOPE_MIN = 1.5f
private const val DOUBLE_TAP_SLOP_DP = 100f
private const val VERTICAL_SENSITIVITY = 1.15f
private const val SEEK_RANGE_FRACTION = 0.25f
private const val MIN_SEEK_RANGE_MS = 30_000f
private const val MAX_SEEK_RANGE_MS = 180_000f
private const val DOUBLE_TAP_EDGE_FRACTION = 0.30f
private const val DOUBLE_TAP_SEEK_MS = 10_000L
