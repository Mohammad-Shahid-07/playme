package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerBottomBar(
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    isPlaying: Boolean,
    speed: Float,
    onSeekTo: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onCycleSpeed: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    onFullscreenToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val bufferProgress = if (durationMs > 0) (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x99000000),
                        Color(0xF2000000)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Timeline Scrubber Row
            TimelineScrubberRow(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                progress = progress,
                bufferProgress = bufferProgress,
                onSeek = { frac -> onSeekTo((frac * durationMs).toLong()) }
            )

            // 2. Full-Width Balanced Controls Deck
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Playback Speed Badge Pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x2E6366F1))
                        .border(1.dp, Color(0x596366F1), CircleShape)
                        .clickable(onClick = onCycleSpeed)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${speed}x",
                        color = Color(0xFFA5B4FC),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Center: Hero Transport Controls (Previous, Glowing Hero Play, Next)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TransportCircleButton(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Track",
                        onClick = onPlayPrevious
                    )

                    // Hero Glowing Play / Pause Button (52dp outer ring, 46dp inner dome)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color(0x996366F1))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                                )
                            )
                            .clickable(onClick = onTogglePlay)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F111E))
                                .border(1.dp, Color(0x1AFFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(26.dp)
                                    .padding(start = if (isPlaying) 0.dp else 2.dp)
                            )
                        }
                    }

                    TransportCircleButton(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        onClick = onPlayNext
                    )
                }

                // Right: Fullscreen Button
                TransportCircleButton(
                    icon = Icons.Rounded.Fullscreen,
                    contentDescription = "Fullscreen",
                    onClick = onFullscreenToggle
                )
            }
        }
    }
}

@Composable
private fun TimelineScrubberRow(
    currentPositionMs: Long,
    durationMs: Long,
    progress: Float,
    bufferProgress: Float,
    onSeek: (Float) -> Unit
) {
    var widthPx by remember { mutableFloatStateOf(1f) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = formatTime(currentPositionMs),
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )

        // Custom Liquid Violet Seekbar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onSeek((offset.x / widthPx).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onSeek((change.position.x / widthPx).coerceIn(0f, 1f))
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Track Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(Color(0x26FFFFFF))
            )

            // Buffer Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(bufferProgress)
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(Color(0x40FFFFFF))
            )

            // Active Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                        )
                    )
            )
        }

        val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)
        Text(
            text = "-${formatTime(remainingMs)}",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TransportCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xE0FFFFFF),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000L
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return "%02d:%02d".format(min, sec)
}
