package com.playme.player.feature.player

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.playme.player.core.designsystem.PlayMeDesign
import com.playme.player.core.designsystem.darkGlass
import com.playme.player.core.model.NerdStatsInfo
import com.playme.player.core.player.DecoderMode
import com.playme.player.core.player.PlayMePlayerController
import com.playme.player.core.player.PlayerUiState
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PlayerControlsHud(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onToggleLock: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onPipClick: () -> Unit,
    onFloatingWindowClick: () -> Unit,
    onToggleBackgroundPlay: () -> Unit,
    onToggleDecoder: () -> Unit,
    onScreenshotClick: () -> Unit = {},
    onRotateClick: () -> Unit = {},
    onNextUpClick: () -> Unit = {},
    onBackgroundClick: () -> Unit = {}
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }
    var isSpeedPopoverVisible by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val isAnySettingActive = uiState.abRepeat.isEnabled ||
        uiState.isNerdStatsVisible ||
        (uiState.sleepTimerConfig != null) ||
        uiState.isBackgroundPlayEnabled ||
        uiState.isMirrorMode ||
        uiState.isNightMode ||
        uiState.isQuickAudioBoostActive ||
        (uiState.audioDelayMs != 0L) ||
        (uiState.subtitleDelayMs != 0L)

    // When locked, only render the floating Unlock pill
    if (uiState.isScreenLocked) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180)),
            modifier = modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 24.dp)
                        .darkGlass(RoundedCornerShape(50))
                        .clickable { onToggleLock() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LockOpen,
                        contentDescription = "Unlock",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tap to unlock", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        return
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // 1. TOP SECTION: Screenshot 4 Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .animateEnterExit(
                        enter = slideInVertically(tween(260)) { -it } + fadeIn(tween(200)),
                        exit = slideOutVertically(tween(200)) { -it } + fadeOut(tween(180))
                    )
                    .background(PlayMeDesign.TopBarVignette)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Absorb clicks on top bar */ }
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back circle & Title with offline specs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        // Circular Back Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .darkGlass(CircleShape)
                                .clickable { onBackClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = uiState.currentVideo?.displayName ?: "Media Playback",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Subtitle: 1080p · HW+ · 485 MB (Screenshot 4)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentVideo?.simpleResolution ?: "1080p",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(" · ", color = Color(0xFF52525B), fontSize = 11.sp)
                                val hwLabel = if (uiState.decoderMode == DecoderMode.HW) "HW+" else "SW"
                                Text(
                                    text = hwLabel,
                                    color = Color(0xFF10B981),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.clickable { onToggleDecoder() }
                                )
                                Text(" · ", color = Color(0xFF52525B), fontSize = 11.sp)
                                Text(
                                    text = uiState.currentVideo?.formattedSize ?: "485 MB",
                                    color = Color(0xFFD4D4D8),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Cohesive 4-Icon Action Pill Capsule (Screenshot 4)
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .darkGlass(RoundedCornerShape(50))
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // 1. Queue list icon -> Opens Queue Drawer
                        IconButton(onClick = onNextUpClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "Queue",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 2. CC Subtitles
                        IconButton(onClick = { controller.openSidebar("subtitles") }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Subtitles,
                                contentDescription = "Subtitles",
                                tint = if (uiState.currentSubtitle != null) PlayMeDesign.ElectricViolet else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 3. Headphones (Background playback)
                        IconButton(onClick = onToggleBackgroundPlay, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Headphones,
                                contentDescription = "Headphones",
                                tint = if (uiState.isBackgroundPlayEnabled) PlayMeDesign.ElectricViolet else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // 4. Pro Tools Drawer
                        Box {
                            IconButton(onClick = { controller.openSidebar("main") }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Pro Tools",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (isAnySettingActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PlayMeDesign.ElectricViolet)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-3).dp, y = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. RIGHT-SIDE FLOATING PILL CAPSULE (Only shown in Landscape mode to prevent portrait clutter)
            if (!isPortrait) {
                Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .animateEnterExit(
                        enter = slideInHorizontally(tween(260)) { it } + fadeIn(tween(200)),
                        exit = slideOutHorizontally(tween(200)) { it } + fadeOut(tween(180))
                    )
                    .padding(end = 16.dp, bottom = 44.dp)
                    .width(44.dp)
                    .darkGlass(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Absorb clicks */ }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Mute Toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isMuted) Color.White.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable { controller.toggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (uiState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // 2. Screen Lock Toggle
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Divider Line
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                // 3. Screenshot
                IconButton(
                    onClick = onScreenshotClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCut,
                        contentDescription = "Screenshot",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // 4. Rotate
                IconButton(
                    onClick = onRotateClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ScreenRotation,
                        contentDescription = "Rotate",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // 3. BOTTOM DECK (SEEKBAR + 3 BALANCED CAPSULES)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(tween(260)) { it } + fadeIn(tween(200)),
                        exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(180))
                    )
                    .background(PlayMeDesign.BottomBarVignette)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Absorb clicks */ }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val currentFraction = if (uiState.durationMs > 0) {
                    (uiState.currentPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
                } else 0f
                val bufferedFraction = if (uiState.durationMs > 0) {
                    (uiState.bufferedPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
                } else 0f

                val displayedPos = if (isScrubbing) {
                    (scrubProgress * uiState.durationMs).toLong()
                } else {
                    uiState.currentPositionMs
                }
                val remainingMs = (uiState.durationMs - displayedPos).coerceAtLeast(0L)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(displayedPos),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Custom Electric Violet Seekbar with A-B Looping Range and Smooth Drag Scrubbing
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .pointerInput(uiState.durationMs) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        isScrubbing = true
                                        scrubProgress = (down.position.x / size.width).coerceIn(0f, 1f)
                                        val pointerId = down.id
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                            if (!change.pressed) {
                                                onSeekTo((scrubProgress * uiState.durationMs).toLong())
                                                isScrubbing = false
                                                break
                                            }
                                            change.consume()
                                            scrubProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val activeFrac = if (isScrubbing) scrubProgress else currentFraction

                        Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                            val w = size.width
                            val h = size.height

                            // Background track
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.2f),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
                            )
                            // Buffer line
                            if (bufferedFraction > 0f) {
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.35f),
                                    size = androidx.compose.ui.geometry.Size(w * bufferedFraction, h),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
                                )
                            }
                            // A-B Loop highlight range
                            val ab = uiState.abRepeat
                            if (ab.isEnabled && uiState.durationMs > 0) {
                                val ptA = ab.pointA
                                val ptB = ab.pointB
                                if (ptA != null && ptB != null) {
                                    val fA = (ptA.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
                                    val fB = (ptB.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
                                    drawRoundRect(
                                        color = Color(0xFFA855F7).copy(alpha = 0.65f),
                                        topLeft = Offset(w * fA, 0f),
                                        size = androidx.compose.ui.geometry.Size(w * (fB - fA), h),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
                                    )
                                }
                            }
                            // Active playback progress track
                            drawRoundRect(
                                brush = PlayMeDesign.ElectricGradient,
                                size = androidx.compose.ui.geometry.Size(w * activeFrac, h),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2)
                            )
                            // Thumb dot
                            drawCircle(
                                color = PlayMeDesign.LavenderGlow,
                                radius = 7.dp.toPx(),
                                center = Offset(w * activeFrac, h / 2)
                            )
                            drawCircle(
                                color = PlayMeDesign.LavenderThumb,
                                radius = 4.5.dp.toPx(),
                                center = Offset(w * activeFrac, h / 2)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "-${formatTime(remainingMs)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Next Up metadata
                val nextVideo = uiState.playlist.getOrNull(uiState.currentPlaylistIndex + 1)
                val queueCount = uiState.playlist.size
                val nextSubtitle = nextVideo?.displayName ?: if (queueCount > 1) "Queue ($queueCount items)" else "Queue"
                val queueFraction = "${uiState.currentPlaylistIndex + 1}/$queueCount"

                // Responsive Bottom HUD: Streamlined & clean in Portrait, 3-capsule in Landscape
                if (isPortrait) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Upper Row: Screenshot 2 Up Next Pill on Left, Aspect Ratio / Fullscreen on Right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            UpNextCapsule(
                                nextVideo = nextVideo,
                                nextSubtitle = nextSubtitle,
                                queueFraction = queueFraction,
                                onNextUpClick = onNextUpClick,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Quick PIP & Aspect Ratio Pill
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .darkGlass(RoundedCornerShape(50))
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onPipClick, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PIP", tint = Color.White, modifier = Modifier.size(17.dp))
                                }
                                IconButton(onClick = onCycleAspectRatio, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = "Aspect Ratio", tint = Color.White, modifier = Modifier.size(19.dp))
                                }
                            }
                        }

                        // Lower Row: Single Clean Cohesive Transport Dock (No clutter!)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .darkGlass(RoundedCornerShape(50))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 1. Rewind 10s
                            IconButton(onClick = onSkipBackward, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(24.dp))
                            }

                            // 2. Protruding Center Play/Pause Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PlayMeDesign.PlayButtonRingBrush)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(PlayMeDesign.PlayButtonInnerBrush)
                                    .clickable { onPlayPauseClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isBuffering) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // 3. Forward 10s
                            IconButton(onClick = onSkipForward, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(24.dp))
                            }

                            // 4. Playback Speed Button -> Toggles Screenshot 3 Popover
                            TextButton(
                                onClick = { isSpeedPopoverVisible = !isSpeedPopoverVisible },
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    "${String.format(Locale.US, "%.1f", uiState.playbackSpeed)}x",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                } else {
                    // Landscape Mode: Spacious 3-capsule horizontal row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UpNextCapsule(
                            nextVideo = nextVideo,
                            nextSubtitle = nextSubtitle,
                            queueFraction = queueFraction,
                            onNextUpClick = onNextUpClick
                        )

                        PlaybackDockCapsule(
                            uiState = uiState,
                            onSkipBackward = onSkipBackward,
                            onPlayPauseClick = onPlayPauseClick,
                            onSkipForward = onSkipForward
                        )

                        ToolsCapsule(
                            uiState = uiState,
                            onSpeedClick = { isSpeedPopoverVisible = !isSpeedPopoverVisible },
                            onPipClick = onPipClick,
                            onCycleAspectRatio = onCycleAspectRatio
                        )
                    }
                }
            }

            // Floating Playback Speed Popover (Screenshot 3)
            if (isSpeedPopoverVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 120.dp, end = 20.dp)
                ) {
                    SpeedPopoverCard(
                        currentSpeed = uiState.playbackSpeed,
                        onSpeedSelected = { speed ->
                            controller.setPlaybackSpeed(speed)
                        }
                    )
                }
            }

            // 4. Video Nerd Stats Overlay
            if (uiState.isNerdStatsVisible && uiState.nerdStats != null) {
                NerdStatsOverlay(
                    stats = uiState.nerdStats,
                    onClose = { controller.toggleNerdStats() }
                )
            }
        }
    }
}

// ==========================================
// DOCK & CAPSULE HELPER COMPONENTS
// ==========================================

/**
 * Screenshot 2: Up Next Preview Pill
 */
@Composable
private fun UpNextCapsule(
    nextVideo: com.playme.player.core.model.VideoItem?,
    nextSubtitle: String,
    queueFraction: String,
    onNextUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF141622).copy(alpha = 0.90f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .clickable { onNextUpClick() }
            .padding(start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Circular Thumbnail with border (Exact match to screenshot)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF18181B))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (nextVideo != null) {
                AsyncImage(
                    model = nextVideo.uri,
                    contentDescription = nextVideo.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Up Next Label & Video Title
        Column(
            modifier = Modifier
                .widthIn(min = 140.dp, max = 220.dp)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "UP NEXT",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp
                )
                Text(
                    text = queueFraction,
                    color = Color(0xFFA5B4FC),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = nextSubtitle,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open Queue",
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Screenshot 3: Obsidian Violet Liquid Glass Speed Controller Popover
 */
@Composable
private fun SpeedPopoverCard(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(235.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xF00D0E1A))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF6366F1))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playback Speed",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", currentSpeed)}x",
                    color = Color(0xFFA5B4FC),
                    fontSize = 12.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Continuous Speed Slider
            androidx.compose.material3.Slider(
                value = currentSpeed,
                onValueChange = { onSpeedSelected((it * 20).toInt() / 20f) },
                valueRange = 0.25f..2.5f,
                steps = 44,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF6366F1),
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                ),
                modifier = Modifier.height(24.dp)
            )

            // 5 Quick Preset Pills (0.5x, 1.0x, 1.25, 1.5, 2.0x)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
                presets.forEach { preset ->
                    val isSelected = Math.abs(currentSpeed - preset) < 0.05f
                    val bg = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f)
                    val border = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.65f) else Color.Transparent
                    val textColor = if (isSelected) Color.White else Color(0xFFD4D4D8)
                    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(0.8.dp, border, RoundedCornerShape(8.dp))
                            .clickable { onSpeedSelected(preset) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (preset == 1.25f || preset == 1.5f) {
                            String.format(Locale.US, "%.2f", preset).trimEnd('0')
                        } else {
                            "${String.format(Locale.US, "%.1f", preset)}x"
                        }
                        Text(
                            text = label,
                            color = textColor,
                            fontSize = 9.5.sp,
                            fontWeight = fontWeight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackDockCapsule(
    uiState: PlayerUiState,
    onSkipBackward: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSkipForward: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        // Backing Track Capsule
        Row(
            modifier = Modifier
                .height(44.dp)
                .darkGlass(RoundedCornerShape(50))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSkipBackward, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(56.dp))
            IconButton(onClick = onSkipForward, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        // Protruding Center Play/Pause Button
        Box(
            modifier = Modifier
                .size(54.dp)
                .offset(y = (-2).dp)
                .shadow(16.dp, CircleShape, spotColor = PlayMeDesign.ElectricViolet)
                .clip(CircleShape)
                .background(PlayMeDesign.PlayButtonRingBrush)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(PlayMeDesign.PlayButtonInnerBrush)
                .clickable { onPlayPauseClick() },
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isBuffering) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(
                    if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolsCapsule(
    uiState: PlayerUiState,
    onSpeedClick: () -> Unit,
    onPipClick: () -> Unit,
    onCycleAspectRatio: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(44.dp)
            .darkGlass(RoundedCornerShape(50))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextButton(
            onClick = onSpeedClick,
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                "${String.format(Locale.US, "%.1f", uiState.playbackSpeed)}x",
                color = Color(0xFFA5B4FC),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        IconButton(onClick = onPipClick, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PIP", tint = Color.White, modifier = Modifier.size(18.dp))
        }

        IconButton(onClick = onCycleAspectRatio, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

// Diagnostics Nerd Stats Floating Overlay
@Composable
private fun NerdStatsOverlay(
    stats: NerdStatsInfo,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(top = 70.dp, start = 24.dp)
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C0E18).copy(alpha = 0.90f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NERD STATS", color = PlayMeDesign.BadgeIndigoText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onClose() }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            NerdStatRow("Resolution", stats.resolution)
            NerdStatRow("Video Codec", stats.videoCodec)
            NerdStatRow("Decoder", stats.decoderName)
            NerdStatRow("Bitrate", "${stats.bitrateKbps} kbps")
            NerdStatRow("Frame Rate", "${String.format(Locale.US, "%.1f", stats.frameRate)} fps")
            NerdStatRow("Audio Codec", "${stats.audioCodec} (${stats.audioChannels}ch, ${stats.sampleRateHz}Hz)")
            NerdStatRow("Buffer", "${stats.bufferDurationMs} ms")
        }
    }
}

@Composable
private fun NerdStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFA1A1AA), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
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
