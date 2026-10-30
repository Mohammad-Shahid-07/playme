package com.playme.player.feature.player.vm

import com.playme.player.core.model.MediaTrackInfo
import com.playme.player.core.player.EngineType
import com.playme.player.core.player.PlayerEngineListener
import com.playme.player.feature.player.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

object PlayerEngineBridge {

    fun createEngineListener(
        state: StateFlow<PlayerState>,
        updateState: ((PlayerState) -> PlayerState) -> Unit,
        onSeekTo: (Long) -> Unit,
        onShowToast: (String) -> Unit,
        onPlaybackEnded: () -> Unit
    ): PlayerEngineListener {
        return object : PlayerEngineListener {
            override fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long) {
                if (PlayerPlaybackCoordinator.isAbLoopBoundaryReached(state.value, positionMs)) {
                    val start = state.value.abLoopStartMs ?: 0L
                    onSeekTo(start)
                    return
                }

                updateState {
                    it.copy(
                        currentPositionMs = positionMs,
                        durationMs = if (durationMs > 0) durationMs else it.durationMs,
                        bufferedPositionMs = bufferMs
                    )
                }
            }

            override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
                updateState {
                    // A seek makes the engine report "not playing" for a moment
                    // while it buffers. Keep the user's play/pause intent so the
                    // transport button does not flicker.
                    it.copy(
                        isPlaying = if (isBuffering) it.isPlaying else isPlaying,
                        isBuffering = isBuffering
                    )
                }
            }

            override fun onSubtitleCue(cueText: String?) {
                updateState { it.copy(currentSubtitle = cueText) }
            }

            override fun onTracksChanged(audioTracks: List<MediaTrackInfo>, subtitleTracks: List<MediaTrackInfo>) {
                updateState {
                    it.copy(
                        availableAudioTracks = audioTracks,
                        availableSubtitleTracks = subtitleTracks,
                        selectedAudioTrack = audioTracks.firstOrNull { t -> t.isSelected }?.name ?: it.selectedAudioTrack,
                        selectedSubtitleTrack = subtitleTracks.firstOrNull { t -> t.isSelected }?.name ?: it.selectedSubtitleTrack
                    )
                }
            }

            override fun onPlaybackEnded() {
                onPlaybackEnded()
            }

            override fun onError(errorMessage: String) {
                onShowToast(errorMessage)
            }

            override fun onEngineSwitched(engineType: EngineType) {
                updateState { it.copy(activeEngineType = engineType) }
                val label = if (engineType == EngineType.VLC) {
                    "Universal Codec Engine (LibVLC)"
                } else {
                    "Hardware Engine (Media3)"
                }
                onShowToast("Using $label")
            }
        }
    }
}
