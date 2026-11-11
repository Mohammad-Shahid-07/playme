package com.playme.player.feature.player.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.scanner.SeekPreviewFrames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PREVIEW_WIDTH = 140.dp
private val PREVIEW_HEIGHT = 79.dp

/**
 * Frame preview that follows the thumb while the timeline is scrubbed.
 *
 * Frames are decoded per timeline bucket and cached, so only the first visit to
 * a bucket pays the decode cost. The previous frame stays on screen while the
 * next one loads, which keeps the bubble from flickering.
 */
@Composable
fun SeekPreviewBubble(
    videoId: String?,
    filePath: String?,
    durationMs: Long,
    positionMs: Long,
    modifier: Modifier = Modifier
) {
    if (videoId == null || filePath == null || durationMs <= 0L) return

    val context = LocalContext.current
    val bucket = remember(durationMs, positionMs) {
        SeekPreviewFrames.bucketFor(durationMs, positionMs)
    }
    var frame by remember(videoId) { mutableStateOf<Bitmap?>(null) }

    // Keyed on the bucket, not the pixel position: scrubbing across the bar
    // must not queue a decode per pixel.
    LaunchedEffect(videoId, bucket) {
        frame = withContext(Dispatchers.IO) {
            SeekPreviewFrames.frameAt(context, videoId, filePath, durationMs, positionMs)
        } ?: frame
    }

    Column(
        modifier = modifier
            .width(PREVIEW_WIDTH)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xF20B0D17)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = PREVIEW_WIDTH, height = PREVIEW_HEIGHT)
                .background(Color(0xFF11131E)),
            contentAlignment = Alignment.Center
        ) {
            val current = frame
            if (current != null) {
                Image(
                    bitmap = current.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = PREVIEW_WIDTH, height = PREVIEW_HEIGHT)
                )
            } else {
                Text(
                    text = "Loading…",
                    color = Color(0xFF6B7280),
                    fontSize = 10.sp
                )
            }
        }

        Text(
            text = formatPreviewTime(positionMs),
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 3.dp)
        )
    }
}

private fun formatPreviewTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
