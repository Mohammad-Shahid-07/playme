package com.playme.player.core.player

import android.content.Context
import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import org.videolan.libvlc.util.VLCVideoLayout

class UniversalPlayerEngine(
    private val context: Context
) : PlayerEngineContract {

    private val exoEngine: PlayMePlayerEngine by lazy { PlayMePlayerEngine(context) }
    private val vlcDelegate = lazy {
        VlcPlayerEngine(context).also { it.setListener(createProxyListener(EngineType.VLC)) }
    }
    private val vlcEngine: VlcPlayerEngine get() = vlcDelegate.value

    private var activeEngine: PlayerEngineContract = exoEngine
    private var engineListener: PlayerEngineListener? = null
    private var currentVideo: VideoItem? = null
    private var lastPositionMs: Long = 0L
    private var positionOriginMs: Long = 0L
    private var originCaptured: Boolean = false
    private var expectedStartMs: Long = 0L
    private var attachedSurfaceView: SurfaceView? = null
    private var currentDecoderMode: DecoderMode = DecoderMode.HW
    private var vlcLayout: VLCVideoLayout? = null
    private var pendingVlcVideo: VideoItem? = null
    private var pendingVlcSeekMs: Long = 0L
    private var pendingLayoutPosted: VLCVideoLayout? = null
    private var vlcPlayGeneration: Long = 0L
    private var shouldPlay: Boolean = true
    @Volatile private var isReleased: Boolean = false

    private var savedSpeed: Float = 1.0f
    private var savedVolume: Float = 1.0f
    private var savedMuted: Boolean = false
    private var savedAspectRatio: AspectRatioMode = AspectRatioMode.BEST_FIT
    private var savedVolumeBoost: Int = 100
    private var savedAudioDelayMs: Long = 0L
    private var savedSubtitleDelayMs: Long = 0L

    override val player: ExoPlayer?
        get() = if (activeEngine.engineType == EngineType.EXO) activeEngine.player else null

    override val engineType: EngineType get() = activeEngine.engineType

    private fun createProxyListener(source: EngineType): PlayerEngineListener = object : PlayerEngineListener {
        private fun isActive(): Boolean = !isReleased && activeEngine.engineType == source

        override fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long) {
            if (!isActive()) return
            // MPEG-TS captures can open at a large non-zero PTS (e.g. 275:44).
            if (!originCaptured && positionMs > 0L) {
                originCaptured = true
                if (expectedStartMs == 0L && positionMs > 5_000L) positionOriginMs = positionMs
            }
            val relative = (positionMs - positionOriginMs).coerceAtLeast(0L)
            val relativeDuration = if (durationMs > positionOriginMs) durationMs - positionOriginMs else durationMs
            lastPositionMs = relative
            engineListener?.onPositionChanged(relative, relativeDuration, bufferMs)
        }

        override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
            if (isActive()) engineListener?.onPlaybackStateChanged(isPlaying, isBuffering)
        }

        override fun onSubtitleCue(cueText: String?) {
            if (isActive()) engineListener?.onSubtitleCue(cueText)
        }

        override fun onTracksChanged(audioTracks: List<MediaTrackInfo>, subtitleTracks: List<MediaTrackInfo>) {
            if (isActive()) engineListener?.onTracksChanged(audioTracks, subtitleTracks)
        }

        override fun onPlaybackEnded() {
            if (isActive()) engineListener?.onPlaybackEnded()
        }

        override fun onError(errorMessage: String) {
            if (!isActive()) return
            if (source == EngineType.EXO && currentVideo != null) switchToVlcFallback()
            else engineListener?.onError(errorMessage)
        }

        override fun onEngineSwitched(engineType: EngineType) {
            if (isActive()) engineListener?.onEngineSwitched(engineType)
        }
    }

    init {
        exoEngine.setListener(createProxyListener(EngineType.EXO))
    }

    fun attachVlcLayout(layout: VLCVideoLayout) {
        if (isReleased) return
        this.vlcLayout = layout
        vlcEngine.attachVlcLayout(layout)
        val queued = pendingVlcVideo ?: return
        if (activeEngine === vlcEngine) launchVlcOnLayout(layout, queued, pendingVlcSeekMs)
    }

    fun detachVlcLayout() {
        pendingLayoutPosted = null
        if (vlcDelegate.isInitialized()) vlcEngine.detachVlcLayout()
        this.vlcLayout = null
    }

    private fun startVlcOnLayout(layout: VLCVideoLayout, video: VideoItem, seekMs: Long) {
        pendingVlcVideo = null
        pendingVlcSeekMs = 0L
        pendingLayoutPosted = null
        vlcEngine.attachVlcLayout(layout)
        applySavedSettings(vlcEngine)
        vlcEngine.playVideoAt(video, seekMs, shouldPlay)
    }

    private fun launchVlcOnLayout(layout: VLCVideoLayout, video: VideoItem, seekMs: Long) {
        if (layout.isAttachedToWindow) {
            startVlcOnLayout(layout, video, seekMs)
            return
        }
        pendingVlcVideo = video
        pendingVlcSeekMs = seekMs
        if (pendingLayoutPosted === layout) return
        pendingLayoutPosted = layout
        val gen = vlcPlayGeneration
        layout.post {
            if (pendingLayoutPosted === layout) pendingLayoutPosted = null
            if (isReleased || activeEngine !== vlcEngine || vlcLayout !== layout || vlcPlayGeneration != gen) return@post
            val queued = pendingVlcVideo ?: return@post
            startVlcOnLayout(layout, queued, pendingVlcSeekMs)
        }
    }

    private fun clearPendingVlc() {
        vlcPlayGeneration++
        pendingVlcVideo = null
        pendingVlcSeekMs = 0L
        pendingLayoutPosted = null
    }

    private fun startVlcPlayback(video: VideoItem, seekMs: Long) {
        clearPendingVlc()
        positionOriginMs = 0L
        expectedStartMs = seekMs
        originCaptured = seekMs > 0L
        activeEngine = vlcEngine
        vlcEngine.setDecoderMode(currentDecoderMode)

        val layout = vlcLayout
        if (layout == null) {
            pendingVlcVideo = video
            pendingVlcSeekMs = seekMs
        } else {
            launchVlcOnLayout(layout, video, seekMs)
        }
        engineListener?.onEngineSwitched(EngineType.VLC)
    }

    private fun startExoPlayback(video: VideoItem, resumeMs: Long) {
        clearPendingVlc()
        if (vlcDelegate.isInitialized()) vlcEngine.pause()
        detachVlcLayout()
        activeEngine = exoEngine
        attachedSurfaceView?.let { exoEngine.attachSurface(it) }
        expectedStartMs = resumeMs
        originCaptured = resumeMs > 0L
        exoEngine.playVideo(video)
        applySavedSettings(exoEngine)
        if (resumeMs > 0L) exoEngine.seekTo(resumeMs)
        engineListener?.onEngineSwitched(EngineType.EXO)
    }

    private fun pauseExoForVlc() {
        exoEngine.pause()
        exoEngine.detachSurface()
    }

    private fun switchToVlcFallback() {
        val video = currentVideo ?: return
        pauseExoForVlc()
        startVlcPlayback(video, lastPositionMs)
        engineListener?.onPlaybackStateChanged(isPlaying = shouldPlay, isBuffering = true)
    }

    private fun applySavedSettings(target: PlayerEngineContract) {
        target.setSpeed(savedSpeed)
        target.setVolume(savedVolume)
        target.setMuted(savedMuted)
        target.setAspectRatio(savedAspectRatio)
        target.setVolumeBoost(savedVolumeBoost)
        target.setAudioDelay(savedAudioDelayMs)
        target.setSubtitleDelay(savedSubtitleDelayMs)
    }

    override fun setListener(listener: PlayerEngineListener) { this.engineListener = listener }

    override fun attachSurface(surfaceView: SurfaceView) {
        if (isReleased) return
        this.attachedSurfaceView = surfaceView
        activeEngine.attachSurface(surfaceView)
    }

    override fun detachSurface() {
        detachVlcLayout()
        activeEngine.detachSurface()
        this.attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) {
        if (isReleased) return
        clearPendingVlc()
        this.currentVideo = video
        this.lastPositionMs = 0L
        this.positionOriginMs = 0L
        this.expectedStartMs = 0L
        this.originCaptured = false
        this.shouldPlay = true

        if (currentDecoderMode == DecoderMode.SW || video.needsVlcEngine()) {
            if (activeEngine === exoEngine) pauseExoForVlc()
            startVlcPlayback(video, 0L)
        } else {
            startExoPlayback(video, 0L)
        }
    }

    override fun play() { shouldPlay = true; if (pendingVlcVideo == null) activeEngine.play() }
    override fun pause() { shouldPlay = false; if (pendingVlcVideo == null) activeEngine.pause() }

    override fun seekTo(positionMs: Long) {
        lastPositionMs = positionMs
        originCaptured = true
        if (pendingVlcVideo != null) pendingVlcSeekMs = positionMs
        else activeEngine.seekTo(positionMs + positionOriginMs)
    }

    override fun setSpeed(speed: Float) { savedSpeed = speed; activeEngine.setSpeed(speed) }
    override fun setVolume(volume: Float) { savedVolume = volume; activeEngine.setVolume(volume) }
    override fun setMuted(muted: Boolean) { savedMuted = muted; activeEngine.setMuted(muted) }

    override fun setDecoderMode(mode: DecoderMode) {
        val modeChanged = this.currentDecoderMode != mode
        this.currentDecoderMode = mode
        activeEngine.setDecoderMode(mode)
        val video = currentVideo ?: return
        val useVlc = mode == DecoderMode.SW || video.needsVlcEngine()

        if (useVlc) {
            if (activeEngine !== vlcEngine) pauseExoForVlc()
            if (activeEngine !== vlcEngine || modeChanged) startVlcPlayback(video, lastPositionMs)
        } else if (activeEngine !== exoEngine) {
            startExoPlayback(video, lastPositionMs)
        }
    }

    override fun setAspectRatio(mode: AspectRatioMode) { savedAspectRatio = mode; activeEngine.setAspectRatio(mode) }
    override fun setAudioDelay(delayMs: Long) { savedAudioDelayMs = delayMs; activeEngine.setAudioDelay(delayMs) }
    override fun setSubtitleDelay(delayMs: Long) { savedSubtitleDelayMs = delayMs; activeEngine.setSubtitleDelay(delayMs) }
    override fun selectAudioTrack(trackId: String) = activeEngine.selectAudioTrack(trackId)
    override fun selectSubtitleTrack(trackId: String) = activeEngine.selectSubtitleTrack(trackId)
    override fun setVolumeBoost(percent: Int) { savedVolumeBoost = percent; activeEngine.setVolumeBoost(percent) }

    override fun release() {
        isReleased = true
        clearPendingVlc()
        engineListener = null
        detachSurface()
        exoEngine.release()
        if (vlcDelegate.isInitialized()) vlcEngine.release()
    }
}

private fun VideoItem.needsVlcEngine(): Boolean =
    format.equals("TS", ignoreCase = true) ||
        format.equals("M2TS", ignoreCase = true) ||
        path.endsWith(".ts", ignoreCase = true) ||
        path.endsWith(".m2ts", ignoreCase = true)
