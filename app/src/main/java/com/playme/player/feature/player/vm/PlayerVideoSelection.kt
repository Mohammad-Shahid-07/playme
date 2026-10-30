package com.playme.player.feature.player.vm

import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.GestureType
import com.playme.player.feature.player.model.PlayerState

/**
 * Builds the fresh playback state for a newly selected video. Kept pure so the
 * first-frame poster state can be published before an engine is attached.
 */
object PlayerVideoSelection {

    fun apply(current: PlayerState, video: VideoItem, playlist: List<VideoItem>): PlayerState {
        val index = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
        return current.copy(
            currentVideo = video,
            playlist = playlist,
            currentIndex = index,
            durationMs = video.durationMs.coerceAtLeast(10_000L),
            currentPositionMs = 0L,
            bufferedPositionMs = 0L,
            isBuffering = true,
            isPlaying = true,
            isControlsVisible = true,
            activeGesture = GestureType.NONE,
            gestureValue = 0f,
            isSpeedBoostActive = false,
            currentSubtitle = null,
            toastMessage = null
        )
    }
}
