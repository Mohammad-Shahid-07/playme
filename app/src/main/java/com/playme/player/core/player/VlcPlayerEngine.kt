package com.playme.player.core.player

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.DecoderMode
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class VlcPlayerEngine(
    private val context: Context
) : PlayerEngineContract {

    private var libVlc: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var engineListener: PlayerEngineListener? = null
    private var savedVolume: Float = 1.0f
    private var attachedSurfaceView: SurfaceView? = null

    override val player: ExoPlayer? = null
    override val engineType: EngineType = EngineType.VLC

    init {
        initLibVlc()
    }

    private fun initLibVlc() {
        try {
            val options = arrayListOf(
                "--no-sub-autodetect-file",
                "--swscale-mode=0",
                "--network-caching=1500"
            )
            libVlc = LibVLC(context, options)
            mediaPlayer = MediaPlayer(libVlc).apply {
                setEventListener(vlcEventListener)
            }
        } catch (e: Throwable) {
            engineListener?.onError("LibVLC initialization failed: ${e.message}")
        }
    }

    private val vlcEventListener = MediaPlayer.EventListener { event ->
        val mp = mediaPlayer ?: return@EventListener
        when (event.type) {
            MediaPlayer.Event.TimeChanged -> {
                val current = mp.time.coerceAtLeast(0L)
                val duration = mp.length.coerceAtLeast(0L)
                engineListener?.onPositionChanged(current, duration, 0L)
            }
            MediaPlayer.Event.Playing -> {
                engineListener?.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
            }
            MediaPlayer.Event.Paused -> {
                engineListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
            }
            MediaPlayer.Event.Buffering -> {
                val isBuffering = event.buffering < 100f
                engineListener?.onPlaybackStateChanged(isPlaying = mp.isPlaying, isBuffering = isBuffering)
            }
            MediaPlayer.Event.EncounteredError -> {
                engineListener?.onError("VLC playback error encountered")
            }
        }
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
    }

    override fun attachSurface(surfaceView: SurfaceView) {
        this.attachedSurfaceView = surfaceView
        mediaPlayer?.vlcVout?.apply {
            setVideoView(surfaceView)
            attachViews()
        }
    }

    override fun detachSurface() {
        mediaPlayer?.vlcVout?.detachViews()
        attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) {
        val vlc = libVlc ?: return
        val mp = mediaPlayer ?: return

        try {
            val media = if (video.uri != Uri.EMPTY) {
                Media(vlc, video.uri)
            } else {
                Media(vlc, video.path)
            }
            // Hardware acceleration enabled with automatic FFmpeg software fallback
            media.setHWDecoderEnabled(true, false)
            mp.media = media
            media.release()

            attachedSurfaceView?.let { attachSurface(it) }
            mp.play()
        } catch (e: Throwable) {
            engineListener?.onError("Failed to load video in VLC: ${e.message}")
        }
    }

    override fun play() {
        mediaPlayer?.play()
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun seekTo(positionMs: Long) {
        mediaPlayer?.time = positionMs
    }

    override fun setSpeed(speed: Float) {
        mediaPlayer?.rate = speed
    }

    override fun setVolume(volume: Float) {
        savedVolume = volume
        mediaPlayer?.volume = (volume * 100).toInt().coerceIn(0, 100)
    }

    override fun setMuted(muted: Boolean) {
        mediaPlayer?.volume = if (muted) 0 else (savedVolume * 100).toInt().coerceIn(0, 100)
    }

    override fun setDecoderMode(mode: DecoderMode) {
        // HW acceleration with automatic fallback is already handled by LibVLC's setHWDecoderEnabled
    }

    override fun release() {
        detachSurface()
        mediaPlayer?.setEventListener(null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        libVlc?.release()
        libVlc = null
    }
}
