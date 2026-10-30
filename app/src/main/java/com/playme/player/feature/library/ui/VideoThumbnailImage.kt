package com.playme.player.feature.library.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.playme.player.core.model.VideoItem
import com.playme.player.core.scanner.ThumbnailManager

@Composable
fun VideoThumbnailImage(
    video: VideoItem,
    targetWidth: Int = 360,
    targetHeight: Int = 240,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(video.id) {
        mutableStateOf<Bitmap?>(ThumbnailManager.getFromMemory(video.id))
    }

    LaunchedEffect(video.id) {
        if (bitmap == null) {
            bitmap = ThumbnailManager.loadThumbnail(
                context = context,
                videoId = video.id,
                uri = video.uri,
                filePath = video.path,
                targetWidth = targetWidth,
                targetHeight = targetHeight
            )
        }
    }

    Crossfade(
        targetState = bitmap,
        animationSpec = tween(120),
        modifier = modifier.fillMaxSize(),
        label = "thumb_crossfade"
    ) { currentBitmap ->
        if (currentBitmap != null) {
            VideoThumbnailContent(
                bitmap = currentBitmap,
                contentDescription = video.title,
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF11131E))
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
            val backdrop = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier
                    .fillMaxSize()
                    .scale(1.2f)
                    .blur(18.dp)
                    .alpha(0.55f)
            } else {
                Modifier
                    .fillMaxSize()
                    .alpha(0.32f)
            }
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
