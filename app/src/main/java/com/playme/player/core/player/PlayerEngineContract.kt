package com.playme.player.core.player

import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.DecoderMode

interface PlayerEngineListener {
    fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long)
    fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean)
    fun onSubtitleCue(cueText: String?)
    fun onError(errorMessage: String)
    fun onEngineSwitched(engineType: EngineType) {}
}

interface PlayerEngineContract {
    val player: ExoPlayer?
    val engineType: EngineType
        get() = EngineType.EXO
    fun setListener(listener: PlayerEngineListener)
    fun playVideo(video: VideoItem)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setSpeed(speed: Float)
    fun setVolume(volume: Float)
    fun setMuted(muted: Boolean)
    fun setDecoderMode(mode: DecoderMode)
    fun attachSurface(surfaceView: android.view.SurfaceView) {}
    fun detachSurface() {}
    fun release()
}
