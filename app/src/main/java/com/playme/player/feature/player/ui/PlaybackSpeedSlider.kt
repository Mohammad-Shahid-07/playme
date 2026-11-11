package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

internal val SPEED_ANCHORS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f)
private val NUM_SEGMENTS = SPEED_ANCHORS.size - 1

internal fun speedToFraction(speed: Float): Float {
    if (speed <= SPEED_ANCHORS.first()) return 0f
    if (speed >= SPEED_ANCHORS.last()) return 1f
    for (i in 0 until NUM_SEGMENTS) {
        val a = SPEED_ANCHORS[i]
        val b = SPEED_ANCHORS[i + 1]
        if (speed in a..b) {
            val segFraction = (speed - a) / (b - a)
            return (i + segFraction) / NUM_SEGMENTS
        }
    }
    return 0f
}

internal fun fractionToSpeed(fraction: Float): Float {
    val clamped = fraction.coerceIn(0f, 1f)
    val t = clamped * NUM_SEGMENTS
    val i = floor(t).toInt().coerceIn(0, NUM_SEGMENTS - 1)
    val segFraction = t - i
    val a = SPEED_ANCHORS[i]
    val b = SPEED_ANCHORS[i + 1]
    val rawSpeed = a + segFraction * (b - a)

    for (anchor in SPEED_ANCHORS) {
        if (abs(rawSpeed - anchor) < 0.035f) return anchor
    }
    return round(rawSpeed * 20f) / 20f
}

@Composable
internal fun PlaybackSpeedRuler(
    displayedSpeed: Float,
    onSelectSpeed: (Float) -> Unit
) {
    val latestSelect by rememberUpdatedState(onSelectSpeed)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(24.dp)
    ) {
        SPEED_ANCHORS.forEachIndexed { idx, anchor ->
            val anchorFraction = idx.toFloat() / NUM_SEGMENTS
            val isSelected = abs(displayedSpeed - anchor) < 0.035f
            val anchorLabel = if (anchor % 1f == 0f) "${anchor.toInt()}×" else "$anchor×"

            Box(
                modifier = Modifier
                    .offset(x = (maxWidth * anchorFraction) - 16.dp)
                    .width(32.dp)
                    .clickable { latestSelect(anchor) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = anchorLabel,
                        color = if (isSelected) Color(0xFFC7D2FE) else Color(0x8CFFFFFF),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
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
}

@Composable
internal fun PlaybackSpeedTrackSlider(
    currentFraction: Float,
    onDragStart: (Float) -> Unit,
    onDragChange: (Float) -> Unit,
    onDragCommit: () -> Unit,
    onDragCancel: () -> Unit,
    onTapCommit: (Float) -> Unit
) {
    val latestOnDragStart by rememberUpdatedState(onDragStart)
    val latestOnDragChange by rememberUpdatedState(onDragChange)
    val latestOnDragCommit by rememberUpdatedState(onDragCommit)
    val latestOnDragCancel by rememberUpdatedState(onDragCancel)
    val latestOnTapCommit by rememberUpdatedState(onTapCommit)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width.coerceAtLeast(1).toFloat()
                    val frac = (offset.x / width).coerceIn(0f, 1f)
                    latestOnTapCommit(fractionToSpeed(frac))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val width = size.width.coerceAtLeast(1).toFloat()
                        val frac = (offset.x / width).coerceIn(0f, 1f)
                        latestOnDragStart(fractionToSpeed(frac))
                    },
                    onDragEnd = { latestOnDragCommit() },
                    onDragCancel = { latestOnDragCancel() },
                    onDrag = { change, _ ->
                        change.consume()
                        val width = size.width.coerceAtLeast(1).toFloat()
                        val frac = (change.position.x / width).coerceIn(0f, 1f)
                        latestOnDragChange(fractionToSpeed(frac))
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(Color(0x24FFFFFF))
        )

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

        SPEED_ANCHORS.forEachIndexed { idx, _ ->
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
