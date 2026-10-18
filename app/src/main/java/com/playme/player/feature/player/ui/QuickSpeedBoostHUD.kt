package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

val BOOST_PRESETS = listOf(0.25f, 0.5f, 0.75f, 1.5f, 2.0f, 3.0f, 4.0f)

private fun formatBoostSpeed(s: Float): String {
    return if (s % 1f == 0f) "${s.toInt()}.0x" else "${s}x"
}

@Composable
fun QuickSpeedBoostHUD(
    isActive: Boolean,
    boostSpeed: Float,
    onSelectBoostSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val activeIndex = BOOST_PRESETS.indices.minByOrNull {
        abs(BOOST_PRESETS[it] - boostSpeed)
    } ?: 4
    val activeFraction = activeIndex.toFloat() / (BOOST_PRESETS.size - 1)

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(390.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xDF0A0D18))
                .border(1.dp, Color(0x333B82F6), RoundedCornerShape(20.dp))
                .shadow(28.dp, RoundedCornerShape(20.dp), spotColor = Color(0x88000000))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Header: Lightning Bolt + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Playback Speed",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                }

                // 2. Presets Row
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    BOOST_PRESETS.forEachIndexed { idx, preset ->
                        val isSelected = idx == activeIndex
                        val leftFrac = idx.toFloat() / (BOOST_PRESETS.size - 1)

                        Box(
                            modifier = Modifier
                                .offset(x = (maxWidth * leftFrac) - 22.dp)
                                .width(44.dp)
                                .clickable { onSelectBoostSpeed(preset) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                                    .then(
                                        if (isSelected) Modifier.shadow(12.dp, CircleShape, spotColor = Color(0xFF2563EB))
                                        else Modifier
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formatBoostSpeed(preset),
                                    color = if (isSelected) Color.White else Color(0xD1FFFFFF),
                                    fontSize = if (isSelected) 12.5.sp else 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // 3. Slider Track Zone with Dots & Glowing Thumb
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val targetIdx = (frac * (BOOST_PRESETS.size - 1)).roundToInt()
                                onSelectBoostSpeed(BOOST_PRESETS[targetIdx.coerceIn(0, BOOST_PRESETS.size - 1)])
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Track Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(CircleShape)
                            .background(Color(0x29FFFFFF))
                    )

                    // Active Blue Fill
                    Box(
                        modifier = Modifier
                            .width(maxWidth * activeFraction)
                            .height(3.5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                    )

                    // Dots
                    BOOST_PRESETS.forEachIndexed { idx, _ ->
                        val posFrac = idx.toFloat() / (BOOST_PRESETS.size - 1)
                        val isPassed = idx <= activeIndex
                        Box(
                            modifier = Modifier
                                .offset(x = (maxWidth * posFrac) - 3.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPassed) Color(0xFF3B82F6) else Color(0x3DFFFFFF))
                        )
                    }

                    // Active Glowing Blue Thumb
                    Box(
                        modifier = Modifier
                            .offset(x = (maxWidth * activeFraction) - 7.dp)
                            .size(14.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color(0xFF38BDF8))
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }

                // 4. Footer: Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${formatBoostSpeed(boostSpeed)} speed playing",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
