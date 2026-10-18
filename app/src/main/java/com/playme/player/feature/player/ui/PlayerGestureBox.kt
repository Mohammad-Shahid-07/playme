package com.playme.player.feature.player.ui

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.playme.player.feature.player.model.GestureType
import kotlin.math.abs

private enum class DragAxis { NONE, VERTICAL_BRIGHTNESS, VERTICAL_VOLUME, HORIZONTAL_SEEK }

@Composable
fun PlayerGestureBox(
    isLocked: Boolean,
    currentVolume: Float,
    currentBrightness: Float,
    currentPositionMs: Long,
    durationMs: Long,
    onSingleTap: () -> Unit,
    onDoubleTapSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onSpeedBoost: (Boolean) -> Unit,
    onGestureStart: (GestureType, Float) -> Unit,
    onGestureUpdate: (Float) -> Unit,
    onGestureEnd: () -> Unit,
    onSeekCommit: (Long) -> Unit,
    onLockedTouchAlert: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var dragAxis by remember { mutableStateOf(DragAxis.NONE) }
    var startPoint by remember { mutableStateOf(Offset.Zero) }
    var startVolume by remember { mutableFloatStateOf(currentVolume) }
    var startBrightness by remember { mutableFloatStateOf(currentBrightness) }
    var startPosMs by remember { mutableLongStateOf(currentPositionMs) }
    var seekTargetMs by remember { mutableLongStateOf(currentPositionMs) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewSize = it }
            .pointerInput(isLocked) {
                detectTapGestures(
                    onTap = {
                        if (isLocked) {
                            onLockedTouchAlert()
                        } else {
                            onSingleTap()
                        }
                    },
                    onDoubleTap = { offset ->
                        if (isLocked) {
                            onLockedTouchAlert()
                            return@detectTapGestures
                        }
                        val width = viewSize.width.toFloat().coerceAtLeast(1f)
                        when {
                            offset.x < width * 0.35f -> onDoubleTapSeek(-10_000L)
                            offset.x > width * 0.65f -> onDoubleTapSeek(10_000L)
                            else -> onTogglePlay()
                        }
                    },
                    onLongPress = {
                        if (!isLocked) {
                            onSpeedBoost(true)
                        }
                    }
                )
            }
            .pointerInput(isLocked, currentVolume, currentBrightness, currentPositionMs) {
                if (isLocked) return@pointerInput

                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        startPoint = down.position
                        startVolume = currentVolume
                        startBrightness = currentBrightness
                        startPosMs = currentPositionMs
                        seekTargetMs = currentPositionMs
                        dragAxis = DragAxis.NONE

                        var pointerId = down.id
                        var isDragging = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                            if (change.pressed) {
                                val totalDeltaX = change.position.x - startPoint.x
                                val totalDeltaY = change.position.y - startPoint.y
                                val dist = kotlin.math.hypot(totalDeltaX, totalDeltaY)

                                if (!isDragging && dist > 24f) {
                                    isDragging = true
                                    val isVertical = abs(totalDeltaY) > abs(totalDeltaX) * 1.5f
                                    val width = viewSize.width.toFloat().coerceAtLeast(1f)

                                    dragAxis = if (isVertical) {
                                        if (startPoint.x < width / 2f) {
                                            onGestureStart(GestureType.BRIGHTNESS, startBrightness)
                                            DragAxis.VERTICAL_BRIGHTNESS
                                        } else {
                                            onGestureStart(GestureType.VOLUME, startVolume)
                                            DragAxis.VERTICAL_VOLUME
                                        }
                                    } else {
                                        onGestureStart(GestureType.SEEK, 0f)
                                        DragAxis.HORIZONTAL_SEEK
                                    }
                                }

                                if (isDragging) {
                                    val height = viewSize.height.toFloat().coerceAtLeast(1f)
                                    val width = viewSize.width.toFloat().coerceAtLeast(1f)

                                    when (dragAxis) {
                                        DragAxis.VERTICAL_BRIGHTNESS -> {
                                            val deltaB = -totalDeltaY / height
                                            val b = (startBrightness + deltaB).coerceIn(0.05f, 1f)
                                            onGestureUpdate(b)
                                        }
                                        DragAxis.VERTICAL_VOLUME -> {
                                            val deltaV = -totalDeltaY / height
                                            val v = (startVolume + deltaV).coerceIn(0f, 1f)
                                            onGestureUpdate(v)
                                        }
                                        DragAxis.HORIZONTAL_SEEK -> {
                                            val seekWindowMs = (durationMs * 0.25f).coerceIn(30_000f, 180_000f)
                                            val deltaMs = ((totalDeltaX / width) * seekWindowMs).toLong()
                                            seekTargetMs = (startPosMs + deltaMs).coerceIn(0L, durationMs)
                                            onGestureUpdate(deltaMs.toFloat())
                                        }
                                        DragAxis.NONE -> Unit
                                    }
                                    change.consume()
                                }
                            } else {
                                // Pointer released (ACTION_UP)
                                onSpeedBoost(false)
                                if (isDragging) {
                                    if (dragAxis == DragAxis.HORIZONTAL_SEEK) {
                                        onSeekCommit(seekTargetMs)
                                    }
                                    onGestureEnd()
                                }
                                break
                            }
                        }
                    }
                }
            }
    ) {
        content()
    }
}
