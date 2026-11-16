package com.playme.player.feature.player.ui

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.playme.player.R
import com.playme.player.core.player.EngineType
import com.playme.player.core.player.NonConsumingVlcLayout
import com.playme.player.core.player.PlayerEngineContract
import com.playme.player.core.player.UniversalPlayerEngine
import com.playme.player.core.player.VlcPlayerEngine
import com.playme.player.feature.player.model.AspectRatioMode
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun VideoSurfaceView(
    engine: PlayerEngineContract?,
    aspectRatioMode: AspectRatioMode,
    activeEngineType: EngineType = EngineType.EXO,
    modifier: Modifier = Modifier
) {
    val currentEngine by rememberUpdatedState(engine)

    if (activeEngineType == EngineType.VLC || engine?.engineType == EngineType.VLC) {
        AndroidView(
            factory = { context ->
                NonConsumingVlcLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    tag = engine
                    attachVlc(engine, this)
                }
            },
            update = { layout ->
                layout.tag = engine
                attachVlc(engine, layout)
            },
            onReset = { layout ->
                detachVlc(currentEngine ?: (layout.tag as? PlayerEngineContract))
                layout.tag = null
            },
            onRelease = { layout ->
                detachVlc(currentEngine ?: (layout.tag as? PlayerEngineContract))
                layout.tag = null
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
                    // VideoStageCanvas renders ExoPlayer cues in PlayMe's styled pill;
                    // hide Media3's built-in SubtitleView to prevent duplicate subtitles.
                    subtitleView?.visibility = android.view.View.GONE
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    // No built-in spinner: fast seeks would flash it. The
                    // Compose overlay only appears after 1.5s of buffering.
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    val holder = ExoViewHolder(this, aspectRatioMode)
                    tag = holder
                    holder.bind(engine?.player, aspectRatioMode)
                }
            },
            update = { playerView ->
                val holder = (playerView.tag as? ExoViewHolder)
                    ?: ExoViewHolder(playerView, aspectRatioMode).also { playerView.tag = it }
                holder.bind(engine?.player, aspectRatioMode)
            },
            onReset = { playerView ->
                (playerView.tag as? ExoViewHolder)?.unbind()
            },
            onRelease = { playerView ->
                (playerView.tag as? ExoViewHolder)?.unbind()
                playerView.tag = null
            },
            modifier = modifier.fillMaxSize()
        )
    }
}

private fun attachVlc(engine: PlayerEngineContract?, layout: NonConsumingVlcLayout) {
    (engine as? UniversalPlayerEngine)?.attachVlcLayout(layout)
        ?: (engine as? VlcPlayerEngine)?.attachVlcLayout(layout)
}

private fun detachVlc(engine: PlayerEngineContract?) {
    (engine as? UniversalPlayerEngine)?.detachVlcLayout()
        ?: (engine as? VlcPlayerEngine)?.detachVlcLayout()
}

@OptIn(UnstableApi::class)
private class ExoViewHolder(
    private val playerView: PlayerView,
    private var mode: AspectRatioMode,
    private var boundPlayer: Player? = null
) {
    private val listener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            playerView.post {
                if (playerView.tag === this@ExoViewHolder && boundPlayer != null) {
                    applyExoAspectRatio(playerView, boundPlayer, mode)
                }
            }
        }
    }

    fun bind(player: Player?, nextMode: AspectRatioMode) {
        mode = nextMode
        if (boundPlayer !== player) {
            boundPlayer?.removeListener(listener)
            boundPlayer = player
            player?.addListener(listener)
            playerView.player = player
        }
        applyExoAspectRatio(playerView, player, nextMode)
    }

    fun unbind() {
        boundPlayer?.removeListener(listener)
        boundPlayer = null
        playerView.player = null
    }
}

@OptIn(UnstableApi::class)
private fun applyExoAspectRatio(
    playerView: PlayerView,
    player: Player?,
    mode: AspectRatioMode
) {
    playerView.resizeMode = when (mode) {
        AspectRatioMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        AspectRatioMode.BEST_FIT,
        AspectRatioMode.SIXTEEN_NINE,
        AspectRatioMode.FOUR_THREE,
        AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    val contentFrame = playerView.findViewById<AspectRatioFrameLayout>(
        androidx.media3.ui.R.id.exo_content_frame
    ) ?: return

    val videoSize = player?.videoSize
    val hasValidSize = videoSize != null && videoSize.width > 0 && videoSize.height > 0
    val naturalRatio = if (hasValidSize) {
        (videoSize!!.width * videoSize.pixelWidthHeightRatio) / videoSize.height.toFloat()
    } else {
        0f
    }

    if (mode == AspectRatioMode.ORIGINAL && hasValidSize) {
        val nativeW = (videoSize!!.width * videoSize.pixelWidthHeightRatio).roundToInt().coerceAtLeast(1)
        val nativeH = videoSize.height.coerceAtLeast(1)
        updateContentFrameLayout(contentFrame, nativeW, nativeH)
        contentFrame.setAspectRatio(naturalRatio)
        return
    }

    updateContentFrameLayout(
        contentFrame,
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    when (mode) {
        AspectRatioMode.SIXTEEN_NINE -> contentFrame.setAspectRatio(16f / 9f)
        AspectRatioMode.FOUR_THREE -> contentFrame.setAspectRatio(4f / 3f)
        else -> if (naturalRatio > 0f) contentFrame.setAspectRatio(naturalRatio)
    }
}

private fun updateContentFrameLayout(
    contentFrame: ViewGroup,
    targetWidth: Int,
    targetHeight: Int
) {
    val lp = contentFrame.layoutParams as? FrameLayout.LayoutParams
    if (lp == null || lp.width != targetWidth || lp.height != targetHeight || lp.gravity != Gravity.CENTER) {
        contentFrame.layoutParams = FrameLayout.LayoutParams(targetWidth, targetHeight, Gravity.CENTER)
    }
}
