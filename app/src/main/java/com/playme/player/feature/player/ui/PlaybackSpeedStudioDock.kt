package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

private val ANCHORS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f)
private val NUM_SEGMENTS = ANCHORS.size - 1

private fun speedToFraction(speed: Float): Float {
    if (speed <= ANCHORS.first()) return 0f
    if (speed >= ANCHORS.last()) return 1f
    for (i in 0 until NUM_SEGMENTS) {
        val a = ANCHORS[i]
        val b = ANCHORS[i + 1]
        if (speed in a..b) {
            val segFraction = (speed - a) / (b - a)
            return (i + segFraction) / NUM_SEGMENTS
        }
    }
    return 0f
}

private fun fractionToSpeed(fraction: Float): Float {
    val clamped = fraction.coerceIn(0f, 1f)
    val t = clamped * NUM_SEGMENTS
    val i = floor(t).toInt().coerceIn(0, NUM_SEGMENTS - 1)
    val segFraction = t - i
    val a = ANCHORS[i]
    val b = ANCHORS[i + 1]
    val rawSpeed = a + segFraction * (b - a)

    for (anchor in ANCHORS) {
        if (abs(rawSpeed - anchor) < 0.035f) return anchor
    }
    return round(rawSpeed * 20f) / 20f
}

@Composable
fun PlaybackSpeedStudioDock(
    isOpen: Boolean,
    onClose: () -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val currentFraction = speedToFraction(speed)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClose),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.96f),
            exit = fadeOut() + scaleOut(targetScale = 0.96f),
            modifier = Modifier.padding(bottom = 74.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(368.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xDF0A0D18))
                    .border(1.dp, Color(0x386366F1), RoundedCornerShape(20.dp))
                    .shadow(32.dp, RoundedCornerShape(20.dp), spotColor = Color(0xCC000000))
                    .clickable(enabled = false) {}
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Header: Micro-Label + Prominent Hero Readout + Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "PLAYBACK SPEED",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (abs(speed - 1.0f) > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x14FFFFFF))
                                        .border(1.dp, Color(0x24FFFFFF), CircleShape)
                                        .clickable { onSpeedChange(1.0f) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Reset 1.0×",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = String.format(Locale.US, "%.2f×", speed),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 2. Geometrically Synchronized Benchmark Ruler Labels
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {
                        val trackWidthPx = constraints.maxWidth.toFloat()
                        ANCHORS.forEachIndexed { idx, anchor ->
                            val anchorFraction = idx.toFloat() / NUM_SEGMENTS
                            val isSelected = abs(speed - anchor) < 0.035f
                            val anchorLabel = if (anchor % 1f == 0f) "${anchor.toInt()}×" else "$anchor×"

                            Box(
                                modifier = Modifier
                                    .offset(x = (maxWidth * anchorFraction) - 16.dp)
                                    .width(32.dp)
                                    .clickable { onSpeedChange(anchor) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = anchorLabel,
                                        color = if (isSelected) Color(0xFFC7D2FE) else Color(0x8CFFFFFF),
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(3.5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFA5B4FC))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Tactile Precision Slider Zone
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onSpeedChange(fractionToSpeed(frac))
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val frac = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    onSpeedChange(fractionToSpeed(frac))
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Groove Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(Color(0x24FFFFFF))
                        )

                        // Illuminated Gradient Fill
                        Box(
                            modifier = Modifier
                                .width(maxWidth * currentFraction)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFC084FC))
                                    )
                                )
                        )

                        // Benchmark Tick Notches
                        ANCHORS.forEachIndexed { idx, _ ->
                            val tickFraction = idx.toFloat() / NUM_SEGMENTS
                            val isPassed = currentFraction >= tickFraction - 0.005f
                            Box(
                                modifier = Modifier
                                    .offset(x = (maxWidth * tickFraction) - 1.dp)
                                    .width(2.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(if (isPassed) Color(0xB3FFFFFF) else Color(0x40FFFFFF))
                            )
                        }

                        // Glowing Tactile Thumb
                        Box(
                            modifier = Modifier
                                .offset(x = (maxWidth * currentFraction) - 9.dp)
                                .size(18.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color(0xFF6366F1))
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6366F1))
                            )
                        }
                    }
                }
            }
        }
    }
}
