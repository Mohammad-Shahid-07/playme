package com.playme.player.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
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

    private var exoPlayer: ExoPlayer? = null
    private var engineListener: PlayerEngineListener? = null
    private var progressPollJob: Job? = null
    private var savedVolume: Float = 1.0f

    override val player: ExoPlayer?
        get() = exoPlayer

    init {
        buildPlayer(DecoderMode.HW)
    }

    private fun buildPlayer(decoderMode: DecoderMode) {
        try {
            val renderersFactory = DefaultRenderersFactory(context).apply {
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                setEnableDecoderFallback(true)
            }

            val trackSelector = DefaultTrackSelector(context)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

            exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector)
                .setAudioAttributes(audioAttributes, true)
                .setSeekBackIncrementMs(10_000L)
                .setSeekForwardIncrementMs(10_000L)
                .build().apply {
                    addListener(playerListener)
                }
        } catch (_: Throwable) {
            try {
                exoPlayer = ExoPlayer.Builder(context)
                    .setSeekBackIncrementMs(10_000L)
                    .setSeekForwardIncrementMs(10_000L)
                    .build().apply {
                        addListener(playerListener)
                    }
            } catch (e2: Throwable) {
                engineListener?.onError("Player initialization failed: ${e2.message}")
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val isBuffering = playbackState == Player.STATE_BUFFERING
            val isPlaying = exoPlayer?.isPlaying == true
            engineListener?.onPlaybackStateChanged(isPlaying, isBuffering)
            updateProgress()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            engineListener?.onPlaybackStateChanged(isPlaying, isBuffering = false)
            if (isPlaying) startProgressPolling() else stopProgressPolling()
        }

        override fun onCues(cueGroup: CueGroup) {
            val text = cueGroup.cues.firstOrNull()?.text?.toString()
            engineListener?.onSubtitleCue(text)
        }

        override fun onPlayerError(error: PlaybackException) {
            engineListener?.onError(error.message ?: "Playback error encountered")
        }
    }

    override fun setListener(listener: PlayerEngineListener) {
        this.engineListener = listener
    }

    override fun playVideo(video: VideoItem) {
        val mediaItem = if (video.uri != android.net.Uri.EMPTY) {
            MediaItem.fromUri(video.uri)
        } else {
            MediaItem.fromUri(android.net.Uri.parse("file://${video.path}"))
        }

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    override fun play() {
        exoPlayer?.play()
    }

    override fun pause() {
        exoPlayer?.pause()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        updateProgress()
    }

    override fun setSpeed(speed: Float) {
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    override fun setVolume(volume: Float) {
        savedVolume = volume
        exoPlayer?.volume = volume
    }

    override fun setMuted(muted: Boolean) {
        exoPlayer?.volume = if (muted) 0f else savedVolume
    }

    override fun setDecoderMode(mode: DecoderMode) {
        // HW with automatic fallback is active by default; mode toggle preserved for UI sync
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
        val player = exoPlayer ?: return
        val current = player.currentPosition.coerceAtLeast(0L)
        val duration = player.duration.coerceAtLeast(0L)
        val buffered = player.bufferedPosition.coerceAtLeast(0L)
        engineListener?.onPositionChanged(current, duration, buffered)
    }

    override fun release() {
        stopProgressPolling()
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }
}
