package com.playme.player.feature.player.vm

import com.playme.player.core.model.VideoItem
import com.playme.player.core.player.EngineType
import com.playme.player.feature.player.model.DecoderMode
import com.playme.player.feature.player.model.GestureType
import com.playme.player.feature.player.model.PlayerState

/**
 * Builds the fresh playback state for a newly selected video or switched queue track.
 * Resets transient loop, boost, delay, track, and error states so a prior video
 * cannot trap or pollute the next one.
 */
object PlayerVideoSelection {

    fun apply(
        current: PlayerState,
        video: VideoItem,
        playlist: List<VideoItem>,
        resetQueueMode: Boolean = false
    ): PlayerState {
        val index = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
        val initialEngine = if (current.decoderMode == DecoderMode.SW || video.requiresVlc()) {
            EngineType.VLC
        } else {
            EngineType.EXO
        }
        return current.copy(
            currentVideo = video,
            playlist = playlist,
            preShufflePlaylist = if (resetQueueMode) emptyList() else current.preShufflePlaylist,
            currentIndex = index,
            durationMs = video.durationMs.coerceAtLeast(10_000L),
            currentPositionMs = 0L,
            bufferedPositionMs = 0L,
            isBuffering = true,
            isPlaying = true,
            isLocked = false,
            isShuffle = if (resetQueueMode) false else current.isShuffle,
            volumePercent = if (resetQueueMode) -1f else current.volumePercent,
            brightnessPercent = if (resetQueueMode) -1f else current.brightnessPercent,
            orientationOverride = if (resetQueueMode) null else current.orientationOverride,
            activeEngineType = initialEngine,
            isControlsVisible = true,
            isAbLoopActive = false,
            abLoopStartMs = null,
            abLoopEndMs = null,
            activeGesture = GestureType.NONE,
            gestureValue = 0f,
            seekPreviewPositionMs = 0L,
            isSpeedBoostActive = false,
            isQueueOpen = false,
            isSettingsOpen = false,
            isSpeedPickerOpen = false,
            audioDelayMs = 0L,
            subtitleDelayMs = 0L,
            currentSubtitle = null,
            toastMessage = null,
            availableAudioTracks = emptyList(),
            availableSubtitleTracks = emptyList(),
            selectedAudioTrack = "default",
            selectedSubtitleTrack = "none"
        )
    }

    private fun VideoItem.requiresVlc(): Boolean =
        format.equals("TS", ignoreCase = true) ||
            format.equals("M2TS", ignoreCase = true) ||
            path.endsWith(".ts", ignoreCase = true) ||
            path.endsWith(".m2ts", ignoreCase = true)
}
