package com.playme.player.feature.library.ui

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = video.title,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
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
