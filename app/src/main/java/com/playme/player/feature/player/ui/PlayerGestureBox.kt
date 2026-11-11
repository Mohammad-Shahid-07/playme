package com.playme.player.feature.player.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.playme.player.feature.player.model.GestureType

/**
 * Transparent gesture surface that sits directly above the native video view.
 *
 * Keeping it above the surface guarantees every touch reaches Compose instead of
 * being consumed by the ExoPlayer / VLC view, and keeping its pointer input
 * keyed on stable values stops playback ticks from cancelling an active swipe.
 */
@Composable
fun PlayerGestureBox(
    isLocked: Boolean,
    currentVolume: Float,
    currentBrightness: Float,
    currentPositionMs: Long,
    durationMs: Long,
    boostSpeed: Float,
    onSingleTap: () -> Unit,
    onDoubleTapSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onSpeedBoost: (Boolean) -> Unit,
    onBoostSpeedChange: (Float) -> Unit,
    onGestureStart: (GestureType, Float) -> Unit,
    onGestureUpdate: (GestureType, Float) -> Unit,
    onGestureEnd: () -> Unit,
    onSeekCommit: (Long) -> Unit,
    onLockedTouchAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val values = rememberUpdatedState(
        PlayerGestureValues(
            isLocked = isLocked,
            volume = currentVolume,
            brightness = currentBrightness,
            positionMs = currentPositionMs,
            durationMs = durationMs,
            boostSpeed = boostSpeed
        )
    )
    val actions = rememberUpdatedState(
        PlayerGestureActions(
            onSingleTap = onSingleTap,
            onDoubleTapSeek = { delta ->
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDoubleTapSeek(delta)
            },
            onTogglePlay = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onTogglePlay()
            },
            onSpeedBoost = { enabled ->
                if (enabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSpeedBoost(enabled)
            },
            onBoostSpeedChange = { speed ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onBoostSpeedChange(speed)
            },
            onGestureStart = onGestureStart,
            onGestureUpdate = onGestureUpdate,
            onGestureEnd = onGestureEnd,
            onSeekCommit = onSeekCommit,
            onLockedTouchAlert = onLockedTouchAlert
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .playerGestureInput(scope, values, actions)
    )
}

internal class PlayerGestureValues(
    val isLocked: Boolean,
    val volume: Float,
    val brightness: Float,
    val positionMs: Long,
    val durationMs: Long,
    val boostSpeed: Float
)

internal class PlayerGestureActions(
    val onSingleTap: () -> Unit,
    val onDoubleTapSeek: (Long) -> Unit,
    val onTogglePlay: () -> Unit,
    val onSpeedBoost: (Boolean) -> Unit,
    val onBoostSpeedChange: (Float) -> Unit,
    val onGestureStart: (GestureType, Float) -> Unit,
    val onGestureUpdate: (GestureType, Float) -> Unit,
    val onGestureEnd: () -> Unit,
    val onSeekCommit: (Long) -> Unit,
    val onLockedTouchAlert: () -> Unit
)
