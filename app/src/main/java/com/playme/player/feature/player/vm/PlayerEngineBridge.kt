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
        onPlaybackEnded: () -> Unit,
        isCurrentEngine: () -> Boolean = { true }
    ): PlayerEngineListener {
        return object : PlayerEngineListener {
            override fun onPositionChanged(positionMs: Long, durationMs: Long, bufferMs: Long) {
                if (!isCurrentEngine()) return
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
                if (!isCurrentEngine()) return
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
                if (!isCurrentEngine()) return
                updateState { it.copy(currentSubtitle = cueText) }
            }

            override fun onTracksChanged(audioTracks: List<MediaTrackInfo>, subtitleTracks: List<MediaTrackInfo>) {
                if (!isCurrentEngine()) return
                updateState {
                    val activeAudio = audioTracks.firstOrNull { t -> t.isSelected }?.id
                        ?: if (audioTracks.isEmpty()) "default" else it.selectedAudioTrack
                    val activeSubtitle = subtitleTracks.firstOrNull { t -> t.isSelected }?.id
                        ?: "none"
                    it.copy(
                        availableAudioTracks = audioTracks,
                        availableSubtitleTracks = subtitleTracks,
                        selectedAudioTrack = activeAudio,
                        selectedSubtitleTrack = activeSubtitle
                    )
                }
            }

            override fun onPlaybackEnded() {
                if (!isCurrentEngine()) return
                onPlaybackEnded()
            }

            override fun onError(errorMessage: String) {
                if (!isCurrentEngine()) return
                onShowToast(errorMessage)
            }

            override fun onEngineSwitched(engineType: EngineType) {
                if (!isCurrentEngine()) return
                // Engine choice is an implementation detail; never surface it.
                updateState { it.copy(activeEngineType = engineType) }
            }
        }
    }
}
