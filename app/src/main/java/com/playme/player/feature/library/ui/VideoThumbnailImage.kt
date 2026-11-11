package com.playme.player.feature.library.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.playme.player.core.model.VideoItem
import com.playme.player.core.scanner.ThumbnailManager
import kotlinx.coroutines.delay

/** Small debounce so a fling does not queue a decode for every row. */
private const val THUMBNAIL_SETTLE_MS = 120L

@Composable
fun VideoThumbnailImage(
    video: VideoItem,
    targetWidth: Int = 360,
    targetHeight: Int = 240,
    contentScale: ContentScale = ContentScale.Crop,
    showPlaceholderGlyph: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isScrolling = LocalListScrolling.current
    var bitmap by remember(video.id) {
        mutableStateOf<Bitmap?>(ThumbnailManager.getFromMemory(video.id))
    }

    LaunchedEffect(video.id, isScrolling) {
        if (bitmap != null || isScrolling) return@LaunchedEffect
        // During a fling the row is disposed before this fires, so fast
        // scrolling never pays for thumbnails the user scrolled past.
        delay(THUMBNAIL_SETTLE_MS)
        bitmap = ThumbnailManager.loadThumbnail(
            context = context,
            videoId = video.id,
            uri = video.uri,
            filePath = video.path,
            durationMs = video.durationMs,
            targetWidth = targetWidth,
            targetHeight = targetHeight
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        val current = bitmap
        if (current != null) {
            VideoThumbnailContent(
                bitmap = current,
                contentDescription = video.title,
                contentScale = contentScale
            )
        } else {
            ThumbnailPlaceholder(showGlyph = showPlaceholderGlyph)
        }
    }
}

/**
 * Some containers (10-bit MKV on certain decoders) cannot produce a frame
 * quickly, or at all. A dim film glyph reads as intentional; an empty
 * rectangle reads as broken.
 */
@Composable
private fun ThumbnailPlaceholder(showGlyph: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF151827), Color(0xFF0D0F1A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showGlyph) {
            Icon(
                imageVector = Icons.Rounded.Movie,
                contentDescription = null,
                tint = Color(0x26FFFFFF),
                modifier = Modifier.fillMaxSize(0.26f)
            )
        }
    }
}

/**
 * Draws a thumbnail without ever stretching it.
 *
 * Landscape sources fill the frame (center crop). Portrait sources (9:16
 * reels, status saves, etc.) are shown full-height with a dimmed, cropped
 * copy behind them so the empty sides still feel intentional.
 */
@Composable
fun VideoThumbnailContent(
    bitmap: Bitmap,
    contentDescription: String?,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier
) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    val isPortrait = bitmap.height > bitmap.width

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0A0B12))) {
        if (isPortrait) {
            // A blurred backdrop is a RenderEffect per item, which visibly
            // stutters while scrolling; a scaled copy reads the same.
            val backdrop = Modifier
                .fillMaxSize()
                .scale(1.2f)
                .alpha(0.35f)
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = backdrop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x40000000), Color(0x80080A12))
                        )
                    )
            )
            Image(
                bitmap = image,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                bitmap = image,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
