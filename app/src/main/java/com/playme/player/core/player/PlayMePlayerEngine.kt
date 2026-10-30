package com.playme.player.core.player

import android.content.Context
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.DecoderMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayMePlayerEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : PlayerEngineContract {

    companion object {
        private const val TAG = "PlayMePlayerEngine"
    }

    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var engineListener: PlayerEngineListener? = null
    private var progressPollJob: Job? = null
    private var savedVolume: Float = 1.0f
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var pendingError: String? = null

    override val player: ExoPlayer? get() = exoPlayer
    override val engineType: EngineType = EngineType.EXO

    private fun buildPlayer() {
        try {
            val renderersFactory = DefaultRenderersFactory(context).apply {
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                setEnableDecoderFallback(true)
            }

            val selector = DefaultTrackSelector(context)
            trackSelector = selector

            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

            exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(selector)
                .setAudioAttributes(audioAttributes, true)
                .setSeekBackIncrementMs(10_000L)
                .setSeekForwardIncrementMs(10_000L)
                .build().apply {
                    addListener(playerListener)
                }
            Log.d(TAG, "ExoPlayer initialized successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize ExoPlayer with custom factory: ${e.message}", e)
            try {
                exoPlayer = ExoPlayer.Builder(context).build().apply { addListener(playerListener) }
            } catch (e2: Throwable) {
                Log.e(TAG, "Fallback ExoPlayer builder also failed", e2)
                pendingError = "Player initialization failed: ${e2.message}"
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN"
            }
            Log.d(TAG, "onPlaybackStateChanged: $stateName, isPlaying=${exoPlayer?.isPlaying}")
            val isBuffering = playbackState == Player.STATE_BUFFERING
            val isPlaying = exoPlayer?.isPlaying == true
            engineListener?.onPlaybackStateChanged(isPlaying, isBuffering)
            updateProgress()
            if (playbackState == Player.STATE_ENDED) {
                engineListener?.onPlaybackEnded()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged: $isPlaying")
            // isPlaying briefly turns false while ExoPlayer buffers a seek.
            // Report that as buffering so the UI keeps its play/pause intent.
            val isBuffering = exoPlayer?.playbackState == Player.STATE_BUFFERING
            engineListener?.onPlaybackStateChanged(isPlaying, isBuffering)
            if (isPlaying) startProgressPolling() else stopProgressPolling()
        }

        override fun onTracksChanged(tracks: Tracks) {
            val (audioList, subList) = ExoPlayerTrackHelper.extractTracks(tracks)
            engineListener?.onTracksChanged(audioList, subList)
        }

        override fun onCues(cueGroup: CueGroup) {
            val text = cueGroup.cues.firstOrNull()?.text?.toString()
            engineListener?.onSubtitleCue(text)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "onPlayerError: code=${error.errorCode}, message=${error.message}", error)
            engineListener?.onError(error.message ?: "Playback error encountered")
        }
    }

    // NOTE: buildPlayer() must run only after playerListener is initialized,
    // otherwise addListener() would receive null during construction.
    init {
        buildPlayer()
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
        pendingError?.let { message ->
            pendingError = null
            listener.onError(message)
        }
    }

    private var attachedSurfaceView: android.view.SurfaceView? = null

    override fun attachSurface(surfaceView: android.view.SurfaceView) {
        this.attachedSurfaceView = surfaceView
        exoPlayer?.setVideoSurfaceView(surfaceView)
    }

    override fun detachSurface() {
        attachedSurfaceView?.let { exoPlayer?.clearVideoSurfaceView(it) }
        attachedSurfaceView = null
    }

    override fun playVideo(video: VideoItem) {
        val player = exoPlayer
        if (player == null) {
            Log.e(TAG, "playVideo called but ExoPlayer is not initialized")
            engineListener?.onError("Media3 player is not available")
            return
        }

        val resolvedUri = MediaUriResolver.resolve(context, video)
        Log.d(TAG, "playVideo: id=${video.id}, title=${video.title}, resolvedUri=$resolvedUri")
        val mediaItem = MediaItem.fromUri(resolvedUri)

        player.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            attachedSurfaceView?.let { setVideoSurfaceView(it) }
            prepare()
            playWhenReady = true
        }
    }

    override fun play() { exoPlayer?.play() }
    override fun pause() { exoPlayer?.pause() }
    override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        updateProgress()
    }
    override fun setSpeed(speed: Float) { exoPlayer?.playbackParameters = PlaybackParameters(speed) }
    override fun setVolume(volume: Float) {
        savedVolume = volume
        exoPlayer?.volume = volume
    }
    override fun setMuted(muted: Boolean) { exoPlayer?.volume = if (muted) 0f else savedVolume }
    override fun setDecoderMode(mode: DecoderMode) {}

    override fun selectAudioTrack(trackId: String) {
        ExoPlayerTrackHelper.selectAudioTrack(trackSelector, exoPlayer, trackId)
    }

    override fun selectSubtitleTrack(trackId: String) {
        ExoPlayerTrackHelper.selectSubtitleTrack(trackSelector, exoPlayer, trackId)
    }

    override fun setVolumeBoost(percent: Int) {
        val sessionId = exoPlayer?.audioSessionId ?: return
        if (sessionId == C.AUDIO_SESSION_ID_UNSET) return
        try {
            if (loudnessEnhancer == null) {
                loudnessEnhancer = LoudnessEnhancer(sessionId)
            }
            val gainMb = ((percent - 100) * 80).coerceIn(0, 8000)
            loudnessEnhancer?.setTargetGain(gainMb)
            loudnessEnhancer?.enabled = percent > 100
        } catch (_: Throwable) {}
    }

    private fun startProgressPolling() {
        progressPollJob?.cancel()
        progressPollJob = scope.launch {
            while (isActive) {
                updateProgress()
                delay(250L)
            }
        }
    }

    private fun stopProgressPolling() {
        progressPollJob?.cancel()
        updateProgress()
    }

    private fun updateProgress() {
        val p = exoPlayer ?: return
        val current = p.currentPosition.coerceAtLeast(0L)
        val duration = p.duration.coerceAtLeast(0L)
        val buffered = p.bufferedPosition.coerceAtLeast(0L)
        engineListener?.onPositionChanged(current, duration, buffered)
    }

    override fun release() {
        stopProgressPolling()
        detachSurface()
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (_: Throwable) {}
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }
}
