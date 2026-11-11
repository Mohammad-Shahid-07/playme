package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val PREVIEW_WIDTH = 140.dp
private val PREVIEW_HEIGHT = 100.dp

/**
 * Timeline scrubber with elapsed/remaining labels and a frame preview.
 *
 * While dragging, the thumb follows the finger locally and the seek is only
 * committed on release — seeking on every pixel makes playback stutter.
 */
@Composable
fun TimelineScrubberRow(
    currentPositionMs: Long,
    durationMs: Long,
    progress: Float,
    videoId: String?,
    filePath: String?,
    onSeek: (Float) -> Unit
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    var scrubProgress by remember { mutableStateOf<Float?>(null) }
    val displayProgress = scrubProgress ?: progress
    val displayPositionMs = if (scrubProgress != null && durationMs > 0L) {
        (displayProgress * durationMs).toLong()
    } else {
        currentPositionMs
    }

    val density = LocalDensity.current
    val previewWidthPx = with(density) { PREVIEW_WIDTH.toPx() }
    val previewHeightPx = with(density) { PREVIEW_HEIGHT.toPx() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = formatScrubberTime(displayPositionMs),
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )

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
                    detectDragGestures(
                        onDragStart = { offset ->
                            scrubProgress = (offset.x / widthPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            scrubProgress?.let { onSeek(it) }
                            scrubProgress = null
                        },
                        onDragCancel = { scrubProgress = null }
                    ) { change, _ ->
                        change.consume()
                        scrubProgress = (change.position.x / widthPx).coerceIn(0f, 1f)
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

            // Active Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayProgress)
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                        )
                    )
            )

            // Frame preview follows the thumb while scrubbing.
            if (scrubProgress != null) {
                val maxOffset = (widthPx - previewWidthPx).coerceAtLeast(0f)
                val x = (displayProgress * widthPx - previewWidthPx / 2f).coerceIn(0f, maxOffset)
                SeekPreviewBubble(
                    videoId = videoId,
                    filePath = filePath,
                    durationMs = durationMs,
                    positionMs = displayPositionMs,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                x.roundToInt(),
                                -(previewHeightPx.toInt() + 8)
                            )
                        }
                )
            }
        }

        val remainingMs = (durationMs - displayPositionMs).coerceAtLeast(0L)
        Text(
            text = "-${formatScrubberTime(remainingMs)}",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatScrubberTime(ms: Long): String {
    val totalSec = ms / 1000L
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return "%02d:%02d".format(min, sec)
}
