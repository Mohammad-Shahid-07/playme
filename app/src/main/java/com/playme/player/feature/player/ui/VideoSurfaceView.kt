package com.playme.player.feature.player.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.playme.player.R
import com.playme.player.core.player.EngineType
import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.core.player.UniversalPlayerEngine
import com.playme.player.core.player.VlcPlayerEngine
import com.playme.player.feature.player.model.AspectRatioMode
import org.videolan.libvlc.util.VLCVideoLayout

@OptIn(UnstableApi::class)
@Composable
fun VideoSurfaceView(
    engine: PlayerEngineContract?,
    aspectRatioMode: AspectRatioMode,
    activeEngineType: EngineType = EngineType.EXO,
    modifier: Modifier = Modifier
) {
    val resizeMode = when (aspectRatioMode) {
        AspectRatioMode.BEST_FIT, AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        AspectRatioMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        AspectRatioMode.SIXTEEN_NINE, AspectRatioMode.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    if (activeEngineType == EngineType.VLC || engine?.engineType == EngineType.VLC) {
        AndroidView(
            factory = { context ->
                VLCVideoLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    (engine as? UniversalPlayerEngine)?.attachVlcLayout(this)
                        ?: (engine as? VlcPlayerEngine)?.attachVlcLayout(this)
                }
            },
            update = { layout ->
                (engine as? UniversalPlayerEngine)?.attachVlcLayout(layout)
                    ?: (engine as? VlcPlayerEngine)?.attachVlcLayout(layout)
            },
            onReset = {
                (engine as? UniversalPlayerEngine)?.detachVlcLayout()
                    ?: (engine as? VlcPlayerEngine)?.detachVlcLayout()
            },
            modifier = modifier.fillMaxSize()
        )
    } else {
        AndroidView(
            factory = { context ->
                val playerView = LayoutInflater.from(context)
                    .inflate(R.layout.playme_texture_player_view, null) as PlayerView
                playerView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    this.resizeMode = resizeMode
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    // No built-in spinner: fast seeks would flash it. The
                    // Compose overlay only appears after 1.5s of buffering.
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    this.player = engine?.player
                }
            },
            update = { playerView ->
                if (playerView.player != engine?.player) {
                    playerView.player = engine?.player
                }
                playerView.resizeMode = resizeMode
            },
            onReset = { playerView ->
                playerView.player = null
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
