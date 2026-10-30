package com.playme.player.core.player

import android.view.SurfaceView
import androidx.media3.exoplayer.ExoPlayer
import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode

interface PlayerEngineListener {
    fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long)
    fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean)
    fun onSubtitleCue(cueText: String?)
    fun onError(errorMessage: String)
    fun onEngineSwitched(engineType: EngineType) {}
    fun onPlaybackEnded() {}
    fun onTracksChanged(audioTracks: List<MediaTrackInfo>, subtitleTracks: List<MediaTrackInfo>) {}
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
    fun setAspectRatio(mode: AspectRatioMode) {}
    fun setAudioDelay(delayMs: Long) {}
    fun setSubtitleDelay(delayMs: Long) {}
    fun selectAudioTrack(trackId: String) {}
    fun selectSubtitleTrack(trackId: String) {}
    fun setVolumeBoost(percent: Int) {}
    fun attachSurface(surfaceView: SurfaceView) {}
    fun detachSurface() {}
    fun release()
}
