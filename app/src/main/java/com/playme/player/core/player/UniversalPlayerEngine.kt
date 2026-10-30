package com.playme.player.core.player

import android.content.Context
import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode

class UniversalPlayerEngine(
    private val context: Context
) : PlayerEngineContract {

    private val exoEngine: PlayMePlayerEngine by lazy { PlayMePlayerEngine(context) }
    private val vlcEngine: VlcPlayerEngine by lazy {
        VlcPlayerEngine(context).also { it.setListener(listenerProxy) }
    }

    private var activeEngine: PlayerEngineContract = exoEngine
    private var engineListener: PlayerEngineListener? = null
    private var currentVideo: VideoItem? = null
    private var lastPositionMs: Long = 0L
    private var attachedSurfaceView: SurfaceView? = null
    private var currentDecoderMode: DecoderMode = DecoderMode.HW
    private var vlcLayout: org.videolan.libvlc.util.VLCVideoLayout? = null

    // LibVLC cannot create a video output without an attached VLCVideoLayout.
    // When playback is requested before the layout exists (decoder switch or
    // ExoPlayer fallback), remember the request and start once it is attached.
    private var pendingVlcVideo: VideoItem? = null
    private var pendingVlcSeekMs: Long = 0L

    override val player: ExoPlayer?
        get() = if (activeEngine.engineType == EngineType.EXO) activeEngine.player else null

    override val engineType: EngineType get() = activeEngine.engineType

    private val listenerProxy = object : PlayerEngineListener {
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

        override fun onTracksChanged(audioTracks: List<MediaTrackInfo>, subtitleTracks: List<MediaTrackInfo>) {
            engineListener?.onTracksChanged(audioTracks, subtitleTracks)
        }

        override fun onPlaybackEnded() {
            engineListener?.onPlaybackEnded()
        }

        override fun onError(errorMessage: String) {
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

    init {
        // LibVLC is only created on demand (SW decoder / Exo fallback),
        // so the common Media3 path does not pay the native load cost.
        exoEngine.setListener(listenerProxy)
    }

    fun attachVlcLayout(layout: org.videolan.libvlc.util.VLCVideoLayout) {
        this.vlcLayout = layout
        vlcEngine.attachVlcLayout(layout)

        pendingVlcVideo?.let { video ->
            // Wait for the view to be attached to the window before asking
            // LibVLC to create its video output.
            layout.post {
                val queued = pendingVlcVideo ?: return@post
                val seekMs = pendingVlcSeekMs
                pendingVlcVideo = null
                pendingVlcSeekMs = 0L
                vlcEngine.playVideoAt(queued, seekMs)
            }
        }
    }

    fun detachVlcLayout() {
        vlcEngine.detachVlcLayout()
        this.vlcLayout = null
    }

    private fun startVlcPlayback(video: VideoItem, seekMs: Long) {
        activeEngine = vlcEngine
        vlcEngine.setDecoderMode(currentDecoderMode)
        if (vlcLayout == null) {
            pendingVlcVideo = video
            pendingVlcSeekMs = seekMs
            engineListener?.onEngineSwitched(EngineType.VLC)
            return
        }

        attachedSurfaceView?.let { vlcEngine.attachSurface(it) }
        vlcEngine.playVideoAt(video, seekMs)
        engineListener?.onEngineSwitched(EngineType.VLC)
    }

    private fun switchToVlcFallback() {
        val video = currentVideo ?: return
        exoEngine.pause()
        exoEngine.detachSurface()
        startVlcPlayback(video, lastPositionMs)
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
    }

    override fun attachSurface(surfaceView: SurfaceView) {
        this.attachedSurfaceView = surfaceView
        activeEngine.attachSurface(surfaceView)
    }

    override fun detachSurface() {
        detachVlcLayout()
        activeEngine.detachSurface()
        this.attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) {
        this.currentVideo = video
        this.lastPositionMs = 0L
        this.pendingVlcVideo = null
        this.pendingVlcSeekMs = 0L

        if (currentDecoderMode == DecoderMode.SW) {
            startVlcPlayback(video, 0L)
            return
        }

        if (activeEngine.engineType == EngineType.VLC) {
            vlcEngine.detachVlcLayout()
            vlcLayout = null
        }
        activeEngine = exoEngine
        attachedSurfaceView?.let { activeEngine.attachSurface(it) }
        activeEngine.playVideo(video)
        engineListener?.onEngineSwitched(activeEngine.engineType)
    }

    override fun play() = activeEngine.play()
    override fun pause() = activeEngine.pause()
    override fun seekTo(positionMs: Long) {
        lastPositionMs = positionMs
        activeEngine.seekTo(positionMs)
    }
    override fun setSpeed(speed: Float) = activeEngine.setSpeed(speed)
    override fun setVolume(volume: Float) = activeEngine.setVolume(volume)
    override fun setMuted(muted: Boolean) = activeEngine.setMuted(muted)

    override fun setDecoderMode(mode: DecoderMode) {
        this.currentDecoderMode = mode
        activeEngine.setDecoderMode(mode)
        val targetEngine = if (mode == DecoderMode.SW) vlcEngine else exoEngine

        if (activeEngine !== targetEngine && currentVideo != null) {
            val video = currentVideo ?: return
            activeEngine.pause()
            activeEngine.detachSurface()

            if (targetEngine === vlcEngine) {
                startVlcPlayback(video, lastPositionMs)
            } else {
                pendingVlcVideo = null
                pendingVlcSeekMs = 0L
                vlcLayout = null
                activeEngine = exoEngine
                attachedSurfaceView?.let { exoEngine.attachSurface(it) }
                // Capture the resume point first: prepare() synchronously
                // reports position 0 and would otherwise clobber it.
                val resumeMs = lastPositionMs
                exoEngine.playVideo(video)
                if (resumeMs > 0L) {
                    exoEngine.seekTo(resumeMs)
                }
                engineListener?.onEngineSwitched(EngineType.EXO)
            }
        }
    }

    override fun setAspectRatio(mode: AspectRatioMode) = activeEngine.setAspectRatio(mode)
    override fun setAudioDelay(delayMs: Long) = activeEngine.setAudioDelay(delayMs)
    override fun setSubtitleDelay(delayMs: Long) = activeEngine.setSubtitleDelay(delayMs)
    override fun selectAudioTrack(trackId: String) = activeEngine.selectAudioTrack(trackId)
    override fun selectSubtitleTrack(trackId: String) = activeEngine.selectSubtitleTrack(trackId)
    override fun setVolumeBoost(percent: Int) = activeEngine.setVolumeBoost(percent)

    override fun release() {
        detachSurface()
        exoEngine.release()
        vlcEngine.release()
    }
}
