package com.playme.player.feature.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeDesign
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

enum class GestureType {
    NONE,
    BRIGHTNESS,
    VOLUME,
    SEEK
}

@Composable
fun GestureOverlay(
    modifier: Modifier = Modifier,
    isLocked: Boolean,
    isSidebarOpen: Boolean = false,
    isHudVisible: Boolean = false,
    currentPositionMs: Long,
    durationMs: Long,
    isFastPlaying: Boolean = false,
    fastPlaySpeed: Float = 2.0f,
    onSingleTap: () -> Unit,
    onDoubleTapSeek: (Long) -> Unit = {},
    onDoubleTapLeft: () -> Unit = {},
    onDoubleTapRight: () -> Unit = {},
    onSeekConfirm: (Long) -> Unit,
    onVolumeBoostChange: (Int) -> Unit,
    onFastPlayStart: () -> Unit = {},
    onFastPlaySpeedChange: (Float) -> Unit = {},
    onFastPlayEnd: () -> Unit = {},
    onPinchZoom: (Boolean) -> Unit = {},
    onScreenshot: () -> Unit = {},
    onUserInteraction: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var activeGesture by remember { mutableStateOf(GestureType.NONE) }
    var brightnessPercent by remember { mutableFloatStateOf(50f) }
    var volumePercent by remember { mutableIntStateOf(50) }
    var seekTargetMs by remember { mutableLongStateOf(0L) }
    var seekDeltaSeconds by remember { mutableIntStateOf(0) }

    // Multi-tap seek accumulation state
    var doubleTapFeedbackLeft by remember { mutableStateOf(false) }
    var doubleTapFeedbackRight by remember { mutableStateOf(false) }
    var accumulatedSeekSeconds by remember { mutableIntStateOf(0) }
    var pendingSingleTapJob by remember { mutableStateOf<Job?>(null) }
    var resetAccumulationJob by remember { mutableStateOf<Job?>(null) }

    // Tap tracking for rock-solid double-tap skip detection
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var lastTapX by remember { mutableFloatStateOf(0f) }
    var lastTapY by remember { mutableFloatStateOf(0f) }
    var lastTapWasRight by remember { mutableStateOf(false) }

    // Hold-to-speed reliable timer job
    var holdSpeedJob by remember { mutableStateOf<Job?>(null) }

    // Aspect ratio pinch feedback
    var pinchFeedbackText by remember { mutableStateOf<String?>(null) }

    // Safe dynamic state references that never abort pointerInput loop
    val currentLocked by rememberUpdatedState(isLocked)
    val currentSidebarOpen by rememberUpdatedState(isSidebarOpen)
    val currentHudVisible by rememberUpdatedState(isHudVisible)
    val currentPosMs by rememberUpdatedState(currentPositionMs)
    val currentDurMs by rememberUpdatedState(durationMs)
    val currentFastPlaySpeed by rememberUpdatedState(fastPlaySpeed)

    // Read current system brightness on init
    LaunchedEffect(Unit) {
        val windowBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
        brightnessPercent = if (windowBrightness >= 0f) windowBrightness * 100f else 50f
    }

    // Auto-hide feedback cards after gestures
    LaunchedEffect(activeGesture) {
        if (activeGesture != GestureType.NONE) {
            delay(1000)
            activeGesture = GestureType.NONE
        }
    }

    LaunchedEffect(pinchFeedbackText) {
        if (pinchFeedbackText != null) {
            delay(1200)
            pinchFeedbackText = null
        }
    }

    val touchSlopPx = with(density) { 16.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var dragMode = GestureType.NONE
                var initialDragX = 0f
                var initialDragY = 0f
                var startPos = 0L
                var downTime = 0L
                var isHoldingSpeed = false
                var hasMovedPastSlop = false
                var initialHoldSpeed = 2.0f

                // Pinch detection state
                var initialPinchSpan = 0f
                var pinchActionTriggered = false

                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes

                        // If settings sidebar is open, ignore gestures
                        if (currentSidebarOpen) {
                            continue
                        }

                        // If screen is locked, only handle single tap to show unlock pill
                        if (currentLocked) {
                            val change = changes.firstOrNull() ?: continue
                            if (!change.pressed) {
                                onSingleTap()
                            }
                            continue
                        }

                        // 3-Finger Screenshot Gesture
                        if (changes.size >= 3) {
                            onUserInteraction()
                            onScreenshot()
                            dragMode = GestureType.NONE
                            holdSpeedJob?.cancel()
                            continue
                        }

                        // Pinch-to-Zoom (2 Fingers) for Aspect Ratio Zoom / Fill
                        if (changes.size == 2) {
                            holdSpeedJob?.cancel()
                            val p0 = changes[0].position
                            val p1 = changes[1].position
                            val currentSpan = sqrt((p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y))

                            if (initialPinchSpan == 0f) {
                                initialPinchSpan = currentSpan
                                pinchActionTriggered = false
                            } else if (!pinchActionTriggered && initialPinchSpan > 0f) {
                                val ratio = currentSpan / initialPinchSpan
                                if (ratio > 1.30f) {
                                    pinchActionTriggered = true
                                    pinchFeedbackText = "Crop to Fill"
                                    onPinchZoom(true)
                                } else if (ratio < 0.72f) {
                                    pinchActionTriggered = true
                                    pinchFeedbackText = "Best Fit (Original)"
                                    onPinchZoom(false)
                                }
                            }
                            continue
                        } else {
                            initialPinchSpan = 0f
                        }

                        val change = changes.firstOrNull() ?: continue

                        if (change.pressed) {
                            onUserInteraction()

                            if (downTime == 0L) {
                                downTime = System.currentTimeMillis()
                                initialDragX = change.position.x
                                initialDragY = change.position.y
                                startPos = currentPosMs
                                hasMovedPastSlop = false
                                isHoldingSpeed = false
                                dragMode = GestureType.NONE
                                initialHoldSpeed = currentFastPlaySpeed

                                // Sync volume with current device level
                                val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                volumePercent = if (maxVolume > 0) ((curVol.toFloat() / maxVolume) * 100).toInt() else 50

                                // Sync brightness with current level
                                val curBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
                                if (curBrightness >= 0f) {
                                    brightnessPercent = curBrightness * 100f
                                } else {
                                    val sysBrightness = try {
                                        android.provider.Settings.System.getInt(context.contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
                                    } catch (e: Exception) { 128 }
                                    brightnessPercent = (sysBrightness / 255f) * 100f
                                }

                                // 100% RELIABLE HOLD-TO-SPEED COROUTINE TIMER:
                                // Generous hold tolerance (32dp) so micro-movement of finger does not cancel hold
                                holdSpeedJob?.cancel()
                                holdSpeedJob = coroutineScope.launch {
                                    delay(240)
                                    if (!hasMovedPastSlop && dragMode == GestureType.NONE) {
                                        isHoldingSpeed = true
                                        onFastPlayStart()
                                    }
                                }
                            } else {
                                val dx = change.position.x - initialDragX
                                val dy = change.position.y - initialDragY
                                val absDx = abs(dx)
                                val absDy = abs(dy)

                                if (isHoldingSpeed) {
                                    val deltaSpeed = (dx / with(density) { 36.dp.toPx() }) * 0.25f
                                    val targetSpeed = (initialHoldSpeed + deltaSpeed).coerceIn(0.25f, 4.0f)
                                    onFastPlaySpeedChange(targetSpeed)
                                } else {
                                    if (absDx > with(density) { 32.dp.toPx() } || absDy > with(density) { 32.dp.toPx() }) {
                                        hasMovedPastSlop = true
                                        holdSpeedJob?.cancel()
                                    }

                                    // Slope arbitration & 3-zone partitioning with center neutral dead zone
                                    if (dragMode == GestureType.NONE && hasMovedPastSlop) {
                                        val width = size.width
                                        val isLeftZone = initialDragX < width * 0.40f
                                        val isRightZone = initialDragX > width * 0.60f

                                        if (absDx > 0) {
                                            val slope = absDy / absDx
                                            if (slope < 0.60f) {
                                                // Predominantly horizontal -> SEEK
                                                dragMode = GestureType.SEEK
                                            } else if (slope > 1.60f) {
                                                // Predominantly vertical
                                                if (isLeftZone) {
                                                    dragMode = GestureType.BRIGHTNESS
                                                } else if (isRightZone) {
                                                    dragMode = GestureType.VOLUME
                                                } else {
                                                    // Center dead zone: ignore vertical swipes!
                                                    dragMode = GestureType.NONE
                                                }
                                            }
                                        }
                                    }

                                    when (dragMode) {
                                        GestureType.BRIGHTNESS -> {
                                            activeGesture = GestureType.BRIGHTNESS
                                            val deltaPercent = -((dy / size.height) * 100f)
                                            brightnessPercent = (brightnessPercent + deltaPercent).coerceIn(0f, 100f)
                                            activity?.window?.attributes = activity?.window?.attributes?.apply {
                                                screenBrightness = (brightnessPercent / 100f).coerceIn(0.01f, 1.0f)
                                            }
                                            initialDragY = change.position.y
                                        }
                                        GestureType.VOLUME -> {
                                            activeGesture = GestureType.VOLUME
                                            val deltaPercent = -((dy / size.height) * 100f).toInt()
                                            val newVol = (volumePercent + deltaPercent).coerceIn(0, 200)
                                            volumePercent = newVol

                                            if (newVol <= 100) {
                                                val sysVol = (newVol / 100f * maxVolume).toInt()
                                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sysVol, 0)
                                                onVolumeBoostChange(0)
                                            } else {
                                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                                                val boost = newVol - 100
                                                onVolumeBoostChange(boost)
                                            }
                                            initialDragY = change.position.y
                                        }
                                        GestureType.SEEK -> {
                                            activeGesture = GestureType.SEEK
                                            val deltaFraction = dx / size.width
                                            val seekWindowMs = 120_000L // 2 min scrub window
                                            val offsetMs = (deltaFraction * seekWindowMs).toLong()
                                            seekTargetMs = (startPos + offsetMs).coerceIn(0L, currentDurMs.coerceAtLeast(0L))
                                            seekDeltaSeconds = ((seekTargetMs - startPos) / 1000).toInt()
                                        }
                                        GestureType.NONE -> {}
                                    }
                                }
                            }
                        } else {
                            // Pointer Released (ACTION_UP)
                            holdSpeedJob?.cancel()

                            val now = System.currentTimeMillis()
                            val tapDuration = now - downTime

                            if (isHoldingSpeed) {
                                isHoldingSpeed = false
                                onFastPlayEnd()
                            } else if (dragMode == GestureType.SEEK) {
                                onSeekConfirm(seekTargetMs)
                            } else if (dragMode == GestureType.NONE && !hasMovedPastSlop && tapDuration < 280) {
                                val tapX = change.position.x
                                val tapY = change.position.y
                                val width = size.width

                                val isRightSide = tapX > width * 0.60f
                                val isLeftSide = tapX < width * 0.40f
                                val timeSinceLast = now - lastTapTime
                                val dist = hypot(tapX - lastTapX, tapY - lastTapY)
                                val isDoubleTapCandidate = (timeSinceLast in 40L..340L || resetAccumulationJob?.isActive == true) &&
                                        dist < with(density) { 90.dp.toPx() }
                                val isSameDirection = (isRightSide && lastTapWasRight) || (isLeftSide && !lastTapWasRight)

                                if ((isRightSide || isLeftSide) && isDoubleTapCandidate && isSameDirection) {
                                    // ACCUMULATE DOUBLE-TAP / MULTI-TAP SKIP (+10s, +20s, +30s...)
                                    pendingSingleTapJob?.cancel()
                                    resetAccumulationJob?.cancel()

                                    accumulatedSeekSeconds += 10

                                    if (isRightSide) {
                                        doubleTapFeedbackRight = true
                                        doubleTapFeedbackLeft = false
                                        onDoubleTapRight()
                                    } else {
                                        doubleTapFeedbackLeft = true
                                        doubleTapFeedbackRight = false
                                        onDoubleTapLeft()
                                    }

                                    // If HUD was toggled by the first tap, dismiss it so double-tap seek is unobstructed
                                    if (currentHudVisible) {
                                        onSingleTap()
                                    }

                                    lastTapTime = now
                                    lastTapX = tapX
                                    lastTapY = tapY
                                    lastTapWasRight = isRightSide

                                    // Auto-reset accumulation after 650ms of inactivity
                                    resetAccumulationJob = coroutineScope.launch {
                                        delay(650)
                                        accumulatedSeekSeconds = 0
                                        doubleTapFeedbackLeft = false
                                        doubleTapFeedbackRight = false
                                        lastTapTime = 0L
                                    }
                                } else {
                                    // SINGLE TAP: Instantly toggle HUD with 0ms latency!
                                    lastTapTime = now
                                    lastTapX = tapX
                                    lastTapY = tapY
                                    lastTapWasRight = isRightSide

                                    onSingleTap()
                                }
                            }

                            dragMode = GestureType.NONE
                            downTime = 0L
                            hasMovedPastSlop = false
                            isHoldingSpeed = false
                            initialPinchSpan = 0f
                        }
                    }
                }
            }
    ) {
        content()

        // 1. Hold-to-Speed Horizontal Slider HUD
        AnimatedVisibility(
            visible = isFastPlaying,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp)
        ) {
            HoldSpeedSliderHud(speed = fastPlaySpeed)
        }

        // 2. Vertical Brightness Capsule Slider (Left Edge)
        AnimatedVisibility(
            visible = activeGesture == GestureType.BRIGHTNESS,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            VerticalCapsuleSlider(
                icon = {
                    Icon(
                        Icons.Default.BrightnessHigh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                valueText = "${brightnessPercent.toInt()}%",
                fraction = brightnessPercent / 100f
            )
        }

        // 3. Vertical Volume Capsule Slider (Symmetrical with left Brightness capsule)
        AnimatedVisibility(
            visible = activeGesture == GestureType.VOLUME,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
        ) {
            val isBoosted = volumePercent > 100
            VerticalCapsuleSlider(
                icon = {
                    Icon(
                        if (volumePercent == 0) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = if (isBoosted) Color(0xFFFF5252) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                valueText = if (isBoosted) "BOOST +${volumePercent - 100}%" else "${volumePercent}%",
                fraction = (volumePercent / 200f),
                isBoosted = isBoosted
            )
        }

        // 4. Seek Scrub HUD (Center)
        AnimatedVisibility(
            visible = activeGesture == GestureType.SEEK,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val sign = if (seekDeltaSeconds >= 0) "+" else ""
            val targetFormatted = formatTime(seekTargetMs)
            val totalFormatted = formatTime(durationMs)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0C0E18).copy(alpha = 0.90f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 28.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$sign${seekDeltaSeconds}s",
                        color = PlayMeDesign.ElectricViolet,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$targetFormatted / $totalFormatted",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 5. Double Tap Accumulating Seek Visual Feedback
        if (doubleTapFeedbackLeft) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 48.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0C0E18).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "-${accumulatedSeekSeconds}s",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (doubleTapFeedbackRight) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0C0E18).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "+${accumulatedSeekSeconds}s",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 6. Pinch-to-Zoom Aspect Ratio Toast Indicator
        AnimatedVisibility(
            visible = pinchFeedbackText != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0C0E18).copy(alpha = 0.90f))
                    .border(1.dp, PlayMeDesign.ElectricViolet.copy(alpha = 0.40f), RoundedCornerShape(50))
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AspectRatio,
                        contentDescription = null,
                        tint = PlayMeDesign.ElectricViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pinchFeedbackText ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// =========================================================================
// VERTICAL BRIGHTNESS & VOLUME CAPSULE SLIDER
// =========================================================================

@Composable
private fun VerticalCapsuleSlider(
    icon: @Composable () -> Unit,
    valueText: String,
    fraction: Float,
    isBoosted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(62.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF0C0E18).copy(alpha = 0.92f))
            .border(
                1.dp,
                if (isBoosted) Color(0xFFFF5252).copy(alpha = 0.40f) else Color.White.copy(alpha = 0.14f),
                RoundedCornerShape(32.dp)
            )
            .padding(vertical = 14.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Top icon (Sun / Speaker)
            icon()

            // Vertical Track Canvas
            val clampedFraction = fraction.coerceIn(0f, 1f)
            Canvas(
                modifier = Modifier
                    .width(6.dp)
                    .height(116.dp)
            ) {
                val w = size.width
                val h = size.height
                val corner = CornerRadius(w / 2)

                // Background track
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    size = size,
                    cornerRadius = corner
                )

                // Active filled track from bottom up
                val filledHeight = h * clampedFraction
                val topY = h - filledHeight

                val trackBrush = if (isBoosted) {
                    Brush.verticalGradient(listOf(Color(0xFFFF5252), Color(0xFFFF7A00)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1)))
                }

                drawRoundRect(
                    brush = trackBrush,
                    topLeft = Offset(0f, topY),
                    size = Size(w, filledHeight),
                    cornerRadius = corner
                )

                // Glow ring
                drawCircle(
                    color = if (isBoosted) Color(0x66FF5252) else Color(0x66757BF8),
                    radius = 11.dp.toPx(),
                    center = Offset(w / 2, topY)
                )
                // Solid thumb dot
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(w / 2, topY)
                )
            }

            // Bottom value pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isBoosted) Color(0xFFFF5252).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.55f))
                    .border(
                        0.5.dp,
                        if (isBoosted) Color(0xFFFF5252).copy(alpha = 0.60f) else Color.Transparent,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = valueText,
                    color = if (isBoosted) Color(0xFFFF5252) else Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}

// =========================================================================
// HORIZONTAL HOLD-TO-SPEED SLIDER HUD
// =========================================================================

@Composable
private fun HoldSpeedSliderHud(
    speed: Float,
    modifier: Modifier = Modifier
) {
    val minSpeed = 0.25f
    val maxSpeed = 4.0f
    val speedFraction = ((speed - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .width(320.dp)
            .height(86.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0C0E18).copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper row: 0.25x -- slider with thumb bubble -- 4.0x
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "0.25x",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )

                // Center Canvas with snap dots & thumb with tooltip bubble
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .height(38.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(38.dp)) {
                        val w = size.width
                        val trackY = 24.dp.toPx()
                        val trackH = 4.dp.toPx()
                        val thumbX = (w * speedFraction).coerceIn(0f, w)

                        // Unfilled track
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.20f),
                            topLeft = Offset(0f, trackY - trackH / 2),
                            size = Size(w, trackH),
                            cornerRadius = CornerRadius(trackH / 2)
                        )

                        // Filled active track
                        if (thumbX > 0f) {
                            drawRoundRect(
                                brush = PlayMeDesign.ElectricGradient,
                                topLeft = Offset(0f, trackY - trackH / 2),
                                size = Size(thumbX, trackH),
                                cornerRadius = CornerRadius(trackH / 2)
                            )
                        }

                        // Discrete snap dots along the track
                        val dotSpeeds = listOf(0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f)
                        dotSpeeds.forEach { s ->
                            val dotFrac = (s - minSpeed) / (maxSpeed - minSpeed)
                            val dotX = w * dotFrac
                            drawCircle(
                                color = if (dotX <= thumbX) Color(0xFF757BF8) else Color.White.copy(alpha = 0.35f),
                                radius = 2.5.dp.toPx(),
                                center = Offset(dotX, trackY)
                            )
                        }

                        // Glow around thumb
                        drawCircle(
                            color = Color(0x66757BF8),
                            radius = 12.dp.toPx(),
                            center = Offset(thumbX, trackY)
                        )
                        // Thumb circle
                        drawCircle(
                            color = Color.White,
                            radius = 7.dp.toPx(),
                            center = Offset(thumbX, trackY)
                        )
                    }

                    // Tooltip bubble dynamically positioned directly above the thumb
                    val bubbleWidth = 44.dp
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (180.dp * speedFraction - bubbleWidth / 2).coerceIn(0.dp, 180.dp - bubbleWidth),
                                y = (-4).dp
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2138))
                            .border(1.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${String.format(Locale.US, "%.2f", speed)}x",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    "4.0x",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bottom row: ⏩ speed playing indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${String.format(Locale.US, "%.2f", speed)}x speed playing",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
