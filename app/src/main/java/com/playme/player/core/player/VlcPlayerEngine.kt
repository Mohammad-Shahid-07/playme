package com.playme.player.core.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class VlcPlayerEngine(
    private val context: Context
) : PlayerEngineContract {

    companion object {
        private const val TAG = "VlcPlayerEngine"
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var libVlc: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var engineListener: PlayerEngineListener? = null
    private var savedVolume: Float = 1.0f
    private var savedVolumeBoost: Int = 100
    private var savedMuted: Boolean = false
    private var attachedSurfaceView: SurfaceView? = null
    private var vlcVideoLayout: VLCVideoLayout? = null
    private var attachedLayout: VLCVideoLayout? = null
    private var pendingAttachLayout: VLCVideoLayout? = null
    private var pfd: ParcelFileDescriptor? = null
    private var decoderMode: DecoderMode = DecoderMode.HW
    private var pendingSeekMs: Long = 0L
    private var pendingError: String? = null
    @Volatile private var shouldPlay: Boolean = true
    @Volatile private var hasEnded: Boolean = false
    @Volatile private var isReleased: Boolean = false

    override val player: ExoPlayer? = null
    override val engineType: EngineType = EngineType.VLC

    // Declared before `init` so MediaPlayer receives an initialized listener.
    private val vlcEventListener = MediaPlayer.EventListener { event ->
        if (Looper.myLooper() == Looper.getMainLooper()) handleVlcEvent(event)
        else mainHandler.post { handleVlcEvent(event) }
    }

    init {
        try {
            val options = arrayListOf("--no-sub-autodetect-file", "--swscale-mode=0", "--network-caching=1500")
            libVlc = LibVLC(context.applicationContext, options)
            mediaPlayer = MediaPlayer(libVlc).apply { setEventListener(vlcEventListener) }
        } catch (e: Throwable) {
            Log.e(TAG, "LibVLC initialization failed", e)
            pendingError = "LibVLC initialization failed: ${e.message}"
        }
    }

    private fun activeMediaPlayer(): MediaPlayer? =
        mediaPlayer?.takeIf { !isReleased && !it.isReleased }

    private fun handleVlcEvent(event: MediaPlayer.Event) {
        val mp = activeMediaPlayer() ?: return
        when (event.type) {
            MediaPlayer.Event.Opening ->
                engineListener?.onPlaybackStateChanged(isPlaying = shouldPlay, isBuffering = true)
            MediaPlayer.Event.TimeChanged ->
                engineListener?.onPositionChanged(mp.time.coerceAtLeast(0L), mp.length.coerceAtLeast(0L), 0L)
            MediaPlayer.Event.Playing -> {
                hasEnded = false
                if (!shouldPlay) {
                    if (mp.isPlaying) mp.pause()
                    engineListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                } else {
                    if (pendingSeekMs > 0L) {
                        mp.time = pendingSeekMs
                        pendingSeekMs = 0L
                    }
                    engineListener?.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                }
                dispatchTracks()
            }
            MediaPlayer.Event.Paused ->
                engineListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
            MediaPlayer.Event.Buffering ->
                engineListener?.onPlaybackStateChanged(isPlaying = mp.isPlaying || shouldPlay, isBuffering = event.buffering < 100f)
            MediaPlayer.Event.EndReached -> {
                hasEnded = true
                pendingSeekMs = 0L
                engineListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
                engineListener?.onPlaybackEnded()
            }
            MediaPlayer.Event.ESAdded -> dispatchTracks()
            MediaPlayer.Event.EncounteredError ->
                engineListener?.onError("VLC playback error encountered")
        }
    }

    private fun dispatchTracks() {
        val (audio, spu) = VlcTrackHelper.extractTracks(mediaPlayer)
        engineListener?.onTracksChanged(audio, spu)
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
        pendingError?.let { msg -> pendingError = null; listener.onError(msg) }
    }

    fun attachVlcLayout(layout: VLCVideoLayout) {
        if (isReleased) return
        this.vlcVideoLayout = layout
        if (attachedLayout === layout && mediaPlayer?.vlcVout?.areViewsAttached() == true) return
        if (layout.isAttachedToWindow) {
            attachVlcViews(layout)
            return
        }
        if (pendingAttachLayout === layout) return
        pendingAttachLayout = layout
        layout.post {
            if (pendingAttachLayout === layout) pendingAttachLayout = null
            attachVlcViews(layout)
        }
    }

    private fun attachVlcViews(layout: VLCVideoLayout) {
        val mp = activeMediaPlayer() ?: return
        if (vlcVideoLayout !== layout || !layout.isAttachedToWindow) return
        if (attachedLayout === layout && mp.vlcVout.areViewsAttached()) return
        try {
            if (mp.vlcVout.areViewsAttached()) mp.detachViews()
            // enableSubtitles = true, useTextureView = false: SurfaceView stays behind Compose HUD.
            mp.attachViews(layout, null, true, false)
            attachedLayout = layout
        } catch (e: Throwable) {
            Log.e(TAG, "Failed attaching VLC views", e)
        }
    }

    fun detachVlcLayout() {
        pendingAttachLayout = null
        runCatching {
            val mp = mediaPlayer
            if (mp != null && !mp.isReleased && mp.vlcVout.areViewsAttached()) mp.detachViews()
        }
        vlcVideoLayout = null
        attachedLayout = null
    }

    override fun attachSurface(surfaceView: SurfaceView) {
        val mp = activeMediaPlayer() ?: return
        if (attachedSurfaceView === surfaceView && mp.vlcVout.areViewsAttached()) return
        this.attachedSurfaceView = surfaceView
        mp.vlcVout.apply {
            if (areViewsAttached()) detachViews()
            setVideoView(surfaceView)
            attachViews()
        }
    }

    override fun detachSurface() {
        detachVlcLayout()
        attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) = playVideoAt(video, 0L, true)

    fun playVideoAt(video: VideoItem, positionMs: Long, autoPlay: Boolean = true) {
        if (isReleased) return
        val vlc = libVlc ?: run {
            engineListener?.onError(pendingError ?: "LibVLC is not initialized")
            return
        }
        val mp = activeMediaPlayer() ?: return

        val uri = MediaUriResolver.resolve(context, video)
        shouldPlay = autoPlay
        hasEnded = false
        pendingSeekMs = positionMs.coerceAtLeast(0L)
        closePfd()

        try {
            if (mp.hasMedia()) mp.stop()
            val media = openMedia(vlc, uri, video.path)
            media.setHWDecoderEnabled(decoderMode == DecoderMode.HW, false)
            media.addOption(":no-sub-autodetect-file")
            mp.media = media
            media.release()

            vlcVideoLayout?.let { attachVlcLayout(it) } ?: attachedSurfaceView?.let { attachSurface(it) }
            mp.play()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load video in VLC", e)
            engineListener?.onError("Failed to load video in VLC: ${e.message}")
        }
    }

    private fun openMedia(vlc: LibVLC, uri: Uri, fallbackPath: String): Media {
        if (uri.scheme == "content") {
            runCatching { context.contentResolver.openFileDescriptor(uri, "r") }
                .getOrNull()
                ?.let { descriptor ->
                    pfd = descriptor
                    return Media(vlc, descriptor.fileDescriptor)
                }
        } else if (uri.scheme == "file" || (uri == Uri.EMPTY && fallbackPath.isNotBlank())) {
            return Media(vlc, uri.path ?: fallbackPath)
        }
        return Media(vlc, uri)
    }

    private fun closePfd() {
        runCatching { pfd?.close() }
        pfd = null
    }

    override fun play() {
        shouldPlay = true
        val mp = activeMediaPlayer()?.takeIf { it.hasMedia() } ?: return
        if (hasEnded) {
            hasEnded = false
            mp.stop()
            mp.play()
        } else if (!mp.isPlaying) {
            mp.play()
        }
    }

    override fun pause() {
        shouldPlay = false
        val mp = activeMediaPlayer()?.takeIf { it.hasMedia() } ?: return
        // LibVLC's pause() toggles state on some builds; only pause when playing.
        if (mp.isPlaying) mp.pause()
    }

    override fun seekTo(positionMs: Long) {
        val mp = activeMediaPlayer() ?: return
        val clamped = positionMs.coerceAtLeast(0L)
        if (hasEnded || (mp.length <= 0L && !mp.isPlaying)) pendingSeekMs = clamped
        else mp.time = clamped
    }

    override fun setSpeed(speed: Float) { mediaPlayer?.rate = speed.coerceAtLeast(0.1f) }
    override fun setVolume(volume: Float) { savedVolume = volume; applyVlcVolume() }
    override fun setMuted(muted: Boolean) { savedMuted = muted; applyVlcVolume() }
    override fun setVolumeBoost(percent: Int) { savedVolumeBoost = percent; applyVlcVolume() }
    override fun setDecoderMode(mode: DecoderMode) { decoderMode = mode }
    override fun setAudioDelay(delayMs: Long) { mediaPlayer?.setAudioDelay(delayMs * 1000L) }
    override fun setSubtitleDelay(delayMs: Long) { mediaPlayer?.setSpuDelay(delayMs * 1000L) }

    override fun setAspectRatio(mode: AspectRatioMode) {
        mediaPlayer?.videoScale = when (mode) {
            AspectRatioMode.BEST_FIT -> MediaPlayer.ScaleType.SURFACE_BEST_FIT
            AspectRatioMode.FIT_SCREEN -> MediaPlayer.ScaleType.SURFACE_FIT_SCREEN
            AspectRatioMode.FILL -> MediaPlayer.ScaleType.SURFACE_FILL
            AspectRatioMode.SIXTEEN_NINE -> MediaPlayer.ScaleType.SURFACE_16_9
            AspectRatioMode.FOUR_THREE -> MediaPlayer.ScaleType.SURFACE_4_3
            AspectRatioMode.ORIGINAL -> MediaPlayer.ScaleType.SURFACE_ORIGINAL
        }
    }

    override fun selectAudioTrack(trackId: String) {
        val id = if (trackId == "default") mediaPlayer?.audioTracks?.firstOrNull { it.id != -1 }?.id else trackId.toIntOrNull()
        id?.let { mediaPlayer?.audioTrack = it }
    }

    override fun selectSubtitleTrack(trackId: String) {
        mediaPlayer?.spuTrack = if (trackId == "none") -1 else trackId.toIntOrNull() ?: return
    }

    private fun applyVlcVolume() {
        mediaPlayer?.volume = if (savedMuted) 0 else ((savedVolume * savedVolumeBoost / 100f) * 100).toInt().coerceIn(0, 200)
    }

    override fun release() {
        isReleased = true
        engineListener = null
        pendingAttachLayout = null
        detachSurface()
        closePfd()
        mediaPlayer?.let { mp ->
            mp.setEventListener(null)
            runCatching { mp.stop() }
            runCatching { mp.release() }
        }
        mediaPlayer = null
        runCatching { libVlc?.release() }
        libVlc = null
    }
}
