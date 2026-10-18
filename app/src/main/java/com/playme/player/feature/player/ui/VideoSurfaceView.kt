package com.playme.player.feature.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.playme.player.feature.player.model.AspectRatioMode

@OptIn(UnstableApi::class)
@Composable
fun VideoSurfaceView(
    player: ExoPlayer?,
    aspectRatioMode: AspectRatioMode,
    modifier: Modifier = Modifier
) {
    val resizeMode = when (aspectRatioMode) {
        AspectRatioMode.BEST_FIT, AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        AspectRatioMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        AspectRatioMode.SIXTEEN_NINE, AspectRatioMode.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                this.resizeMode = resizeMode
                this.player = player
            }
        },
        update = { playerView ->
            if (playerView.player != player) {
                playerView.player = player
            }
            playerView.resizeMode = resizeMode
        },
        modifier = modifier.fillMaxSize()
    )
}
