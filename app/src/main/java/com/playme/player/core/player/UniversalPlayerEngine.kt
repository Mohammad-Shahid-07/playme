package com.playme.player.core.player

import android.content.Context
import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.DecoderMode

class UniversalPlayerEngine(
    private val context: Context
) : PlayerEngineContract {

    private val exoEngine: PlayMePlayerEngine by lazy { PlayMePlayerEngine(context) }
    private val vlcEngine: VlcPlayerEngine by lazy { VlcPlayerEngine(context) }

    private var activeEngine: PlayerEngineContract = exoEngine
    private var engineListener: PlayerEngineListener? = null
    private var currentVideo: VideoItem? = null
    private var lastPositionMs: Long = 0L
    private var attachedSurfaceView: SurfaceView? = null
    private var currentDecoderMode: DecoderMode = DecoderMode.HW

    override val player: ExoPlayer?
        get() = if (activeEngine.engineType == EngineType.EXO) activeEngine.player else null

    override val engineType: EngineType
        get() = activeEngine.engineType

    init {
        setupListeners()
    }

    private fun setupListeners() {
        val listenerProxy = object : PlayerEngineListener {
            override fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long) {
                lastPositionMs = positionMs
                engineListener?.onPositionChanged(positionMs, durationMs, bufferMs)
            }

            override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
                engineListener?.onPlaybackStateChanged(isPlaying, isBuffering)
            }

            override fun onSubtitleCue(cueText: String?) {
                engineListener?.onSubtitleCue(cueText)
            }

            override fun onError(errorMessage: String) {
                // If ExoPlayer fails on an unsupported codec or container, hot-swap to LibVLC!
                if (activeEngine.engineType == EngineType.EXO) {
                    switchToVlcFallback()
                } else {
                    engineListener?.onError(errorMessage)
                }
            }

            override fun onEngineSwitched(engineType: EngineType) {
                engineListener?.onEngineSwitched(engineType)
            }
        }

        exoEngine.setListener(listenerProxy)
        vlcEngine.setListener(listenerProxy)
    }

    private fun switchToVlcFallback() {
        val video = currentVideo ?: return
        exoEngine.pause()
        activeEngine = vlcEngine

        attachedSurfaceView?.let { vlcEngine.attachSurface(it) }
        vlcEngine.playVideo(video)
        if (lastPositionMs > 1000L) {
            vlcEngine.seekTo(lastPositionMs)
        }
        engineListener?.onEngineSwitched(EngineType.VLC)
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
    }

    override fun attachSurface(surfaceView: SurfaceView) {
        this.attachedSurfaceView = surfaceView
        activeEngine.attachSurface(surfaceView)
    }

    override fun detachSurface() {
        activeEngine.detachSurface()
        this.attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) {
        this.currentVideo = video
        this.lastPositionMs = 0L

        // Route to VLC if user explicitly set SW decoder mode, otherwise use ExoPlayer HW
        activeEngine = if (currentDecoderMode == DecoderMode.SW) vlcEngine else exoEngine

        attachedSurfaceView?.let { activeEngine.attachSurface(it) }
        activeEngine.playVideo(video)
        engineListener?.onEngineSwitched(activeEngine.engineType)
    }

    override fun play() {
        activeEngine.play()
    }

    override fun pause() {
        activeEngine.pause()
    }

    override fun seekTo(positionMs: Long) {
        lastPositionMs = positionMs
        activeEngine.seekTo(positionMs)
    }

    override fun setSpeed(speed: Float) {
        activeEngine.setSpeed(speed)
    }

    override fun setVolume(volume: Float) {
        activeEngine.setVolume(volume)
    }

    override fun setMuted(muted: Boolean) {
        activeEngine.setMuted(muted)
    }

    override fun setDecoderMode(mode: DecoderMode) {
        this.currentDecoderMode = mode
        val targetEngine = if (mode == DecoderMode.SW) vlcEngine else exoEngine

        if (activeEngine !== targetEngine && currentVideo != null) {
            val video = currentVideo ?: return
            activeEngine.pause()
            activeEngine.detachSurface()

            activeEngine = targetEngine
            attachedSurfaceView?.let { activeEngine.attachSurface(it) }
            activeEngine.playVideo(video)
            if (lastPositionMs > 1000L) {
                activeEngine.seekTo(lastPositionMs)
            }
            engineListener?.onEngineSwitched(activeEngine.engineType)
        }
    }

    override fun release() {
        detachSurface()
        exoEngine.release()
        vlcEngine.release()
        currentVideo = null
    }
}
