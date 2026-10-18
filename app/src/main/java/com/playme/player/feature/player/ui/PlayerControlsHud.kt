package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.model.PlayerState
import com.playme.player.feature.player.model.SettingsTab

@Composable
fun PlayerControlsHud(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.areControlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(PlayMeColors.ObsidianDark.copy(alpha = 0.9f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAction(PlayerAction.ClosePlayer) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = PlayMeColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = state.currentVideo?.title ?: "Playing Video",
                    color = PlayMeColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Quick Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onAction(PlayerAction.ToggleControlsLock) }) {
                        Icon(
                            imageVector = if (state.isControlsLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = "Lock Controls",
                            tint = if (state.isControlsLocked) PlayMeColors.AmberBoost else PlayMeColors.TextSecondary
                        )
                    }
                    IconButton(onClick = { onAction(PlayerAction.CycleScaleMode) }) {
                        Icon(
                            imageVector = Icons.Rounded.AspectRatio,
                            contentDescription = "Scale Mode",
                            tint = PlayMeColors.TextSecondary
                        )
                    }
                    IconButton(onClick = {
                        onAction(PlayerAction.SelectSettingsTab(SettingsTab.SUBTITLES))
                        onAction(PlayerAction.ToggleSettings(true))
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Subtitles,
                            contentDescription = "Subtitles",
                            tint = PlayMeColors.TextSecondary
                        )
                    }
                    IconButton(onClick = {
                        onAction(PlayerAction.SelectSettingsTab(SettingsTab.AUDIO))
                        onAction(PlayerAction.ToggleSettings(true))
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Audio Track",
                            tint = PlayMeColors.TextSecondary
                        )
                    }
                    IconButton(onClick = { onAction(PlayerAction.ToggleSettings(null)) }) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "More Settings",
                            tint = PlayMeColors.TextSecondary
                        )
                    }
                }
            }

            // Center Play / Pause Button
            if (!state.isControlsLocked) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(PlayMeColors.GlassElevated)
                        .border(1.5.dp, PlayMeColors.GlassBorderHover, CircleShape)
                        .clickable { onAction(PlayerAction.TogglePlayPause) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = PlayMeColors.TextPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Bottom Timeline & Controls Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, PlayMeColors.ObsidianDark.copy(alpha = 0.95f))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (!state.isControlsLocked) {
                    // Timeline Slider
                    val currentSec = (state.currentPositionMs / 1000f)
                    val totalSec = (state.durationMs / 1000f).coerceAtLeast(1f)

                    Slider(
                        value = currentSec.coerceIn(0f, totalSec),
                        onValueChange = { onAction(PlayerAction.SeekTo((it * 1000L).toLong())) },
                        valueRange = 0f..totalSec,
                        colors = SliderDefaults.colors(
                            thumbColor = PlayMeColors.LavenderThumb,
                            activeTrackColor = PlayMeColors.ElectricViolet,
                            inactiveTrackColor = PlayMeColors.GlassBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bottom Row: Times + Controls + Speed + Queue
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Current & Total Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = formatDuration(state.currentPositionMs),
                                color = PlayMeColors.TextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "/",
                                color = PlayMeColors.TextTertiary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = formatDuration(state.durationMs),
                                color = PlayMeColors.TextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Replay 10, Play/Pause, Forward 10
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = { onAction(PlayerAction.SeekRelative(-10)) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Replay10,
                                    contentDescription = "Rewind 10s",
                                    tint = PlayMeColors.TextPrimary
                                )
                            }
                            IconButton(onClick = { onAction(PlayerAction.TogglePlayPause) }) {
                                Icon(
                                    imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = PlayMeColors.ElectricViolet,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(onClick = { onAction(PlayerAction.SeekRelative(10)) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Forward10,
                                    contentDescription = "Forward 10s",
                                    tint = PlayMeColors.TextPrimary
                                )
                            }
                        }

                        // Speed Pill & Queue Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Speed Pill
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PlayMeColors.GlassSurface)
                                    .border(1.dp, PlayMeColors.GlassBorder, CircleShape)
                                    .clickable { onAction(PlayerAction.ToggleSpeedMenu(null)) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "%.2f×".format(state.playbackSpeed),
                                    color = PlayMeColors.LavenderText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Queue Button
                            IconButton(onClick = { onAction(PlayerAction.ToggleQueueDrawer(null)) }) {
                                Icon(
                                    imageVector = Icons.Rounded.QueueMusic,
                                    contentDescription = "Queue",
                                    tint = PlayMeColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
