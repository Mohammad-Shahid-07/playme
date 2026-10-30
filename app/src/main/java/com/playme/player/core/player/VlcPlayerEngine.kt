package com.playme.player.core.player

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class VlcPlayerEngine(
    private val context: Context
) : PlayerEngineContract {

    companion object {
        private const val TAG = "VlcPlayerEngine"
    }

    private var libVlc: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var engineListener: PlayerEngineListener? = null
    private var savedVolume: Float = 1.0f
    private var attachedSurfaceView: SurfaceView? = null
    private var vlcVideoLayout: org.videolan.libvlc.util.VLCVideoLayout? = null
    private var pfd: android.os.ParcelFileDescriptor? = null
    private var decoderMode: DecoderMode = DecoderMode.HW

    // A seek requested before the media is parsed is ignored by LibVLC.
    // Hold it until the Playing event, then apply it.
    private var pendingSeekMs: Long = 0L

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
            Log.d(TAG, "LibVLC initialized successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "LibVLC initialization failed", e)
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
                Log.d(TAG, "VLC event: Playing")
                if (pendingSeekMs > 0L) {
                    val target = pendingSeekMs
                    pendingSeekMs = 0L
                    mp.time = target
                }
                engineListener?.onPlaybackStateChanged(isPlaying = true, isBuffering = false)
                dispatchTracks()
            }
            MediaPlayer.Event.Paused -> {
                Log.d(TAG, "VLC event: Paused")
                engineListener?.onPlaybackStateChanged(isPlaying = false, isBuffering = false)
            }
            MediaPlayer.Event.Buffering -> {
                val isBuffering = event.buffering < 100f
                engineListener?.onPlaybackStateChanged(isPlaying = mp.isPlaying, isBuffering = isBuffering)
            }
            MediaPlayer.Event.EndReached -> {
                Log.d(TAG, "VLC event: EndReached")
                engineListener?.onPlaybackEnded()
            }
            MediaPlayer.Event.ESAdded -> dispatchTracks()
            MediaPlayer.Event.EncounteredError -> {
                Log.e(TAG, "VLC event: EncounteredError")
                engineListener?.onError("VLC playback error encountered")
            }
        }
    }

    private fun dispatchTracks() {
        val (audio, spu) = VlcTrackHelper.extractTracks(mediaPlayer)
        engineListener?.onTracksChanged(audio, spu)
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
    }

    fun attachVlcLayout(layout: org.videolan.libvlc.util.VLCVideoLayout) {
        val previous = vlcVideoLayout
        this.vlcVideoLayout = layout
        mediaPlayer?.let { mp ->
            try {
                if (previous === layout && mp.vlcVout.areViewsAttached()) {
                    return
                }
                if (mp.vlcVout.areViewsAttached()) {
                    mp.detachViews()
                }
                mp.attachViews(layout, null, true, true)
                Log.d(TAG, "Attached VLCVideoLayout with useTextureView=true")
            } catch (e: Throwable) {
                Log.e(TAG, "Failed attaching VLC views", e)
            }
        }
    }

    fun detachVlcLayout() {
        try {
            mediaPlayer?.detachViews()
        } catch (_: Throwable) {}
        vlcVideoLayout = null
    }

    override fun attachSurface(surfaceView: SurfaceView) {
        this.attachedSurfaceView = surfaceView
        mediaPlayer?.vlcVout?.apply {
            setVideoView(surfaceView)
            attachViews()
        }
    }

    override fun detachSurface() {
        detachVlcLayout()
        mediaPlayer?.vlcVout?.detachViews()
        attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) = playVideoAt(video, 0L)

    /**
     * Starts a clip and seeks once LibVLC reports Playing. A seek issued
     * immediately after play() is ignored while the media is still opening.
     */
    fun playVideoAt(video: VideoItem, positionMs: Long) {
        val vlc = libVlc ?: return
        val mp = mediaPlayer ?: return
        val uri = MediaUriResolver.resolve(context, video)
        Log.d(TAG, "playVideo in VLC: uri=$uri, path=${video.path}, seek=$positionMs")
        pendingSeekMs = positionMs.coerceAtLeast(0L)

        try {
            pfd?.close()
            pfd = null
        } catch (_: Throwable) {}

        try {
            val media = if (uri.scheme == "content") {
                try {
                    val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
                    pfd = descriptor
                    if (descriptor != null) {
                        Media(vlc, descriptor.fileDescriptor)
                    } else {
                        Media(vlc, uri)
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "openFileDescriptor fallback to uri: ${e.message}")
                    Media(vlc, uri)
                }
            } else if (uri.scheme == "file") {
                Media(vlc, uri.path ?: video.path)
            } else {
                Media(vlc, uri)
            }

            media.setHWDecoderEnabled(decoderMode == DecoderMode.HW, false)
            media.addOption(":no-sub-autodetect-file")
            mp.media = media
            media.release()

            vlcVideoLayout?.let { attachVlcLayout(it) }
            attachedSurfaceView?.let { attachSurface(it) }
            mp.play()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load video in VLC", e)
            engineListener?.onError("Failed to load video in VLC: ${e.message}")
        }
    }

    override fun play() { mediaPlayer?.play() }
    override fun pause() { mediaPlayer?.pause() }
    override fun seekTo(positionMs: Long) {
        val mp = mediaPlayer ?: return
        if (mp.length <= 0L && !mp.isPlaying) {
            pendingSeekMs = positionMs
        } else {
            mp.time = positionMs
        }
    }
    override fun setSpeed(speed: Float) { mediaPlayer?.rate = speed }
    override fun setVolume(volume: Float) {
        savedVolume = volume
        mediaPlayer?.volume = (volume * 100).toInt().coerceIn(0, 100)
    }
    override fun setMuted(muted: Boolean) {
        mediaPlayer?.volume = if (muted) 0 else (savedVolume * 100).toInt().coerceIn(0, 100)
    }
    override fun setDecoderMode(mode: DecoderMode) {
        decoderMode = mode
    }

    override fun setAspectRatio(mode: AspectRatioMode) {
        val mp = mediaPlayer ?: return
        when (mode) {
            AspectRatioMode.BEST_FIT -> { mp.aspectRatio = null; mp.scale = 0f }
            AspectRatioMode.FIT_SCREEN -> { mp.aspectRatio = null; mp.scale = 1.25f }
            AspectRatioMode.FILL -> { mp.aspectRatio = null; mp.scale = 1.0f }
            AspectRatioMode.SIXTEEN_NINE -> { mp.aspectRatio = "16:9"; mp.scale = 0f }
            AspectRatioMode.FOUR_THREE -> { mp.aspectRatio = "4:3"; mp.scale = 0f }
            AspectRatioMode.ORIGINAL -> { mp.aspectRatio = null; mp.scale = 1.0f }
        }
    }

    override fun setAudioDelay(delayMs: Long) { mediaPlayer?.setAudioDelay(delayMs * 1000L) }
    override fun setSubtitleDelay(delayMs: Long) { mediaPlayer?.setSpuDelay(delayMs * 1000L) }

    override fun selectAudioTrack(trackId: String) {
        trackId.toIntOrNull()?.let { id -> mediaPlayer?.audioTrack = id }
    }

    override fun selectSubtitleTrack(trackId: String) {
        trackId.toIntOrNull()?.let { id -> mediaPlayer?.spuTrack = id }
    }

    override fun setVolumeBoost(percent: Int) {
        val target = ((savedVolume * percent / 100f) * 100).toInt().coerceIn(0, 200)
        mediaPlayer?.volume = target
    }

    override fun release() {
        detachSurface()
        try {
            pfd?.close()
            pfd = null
        } catch (_: Throwable) {}
        mediaPlayer?.setEventListener(null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        libVlc?.release()
        libVlc = null
    }
}
