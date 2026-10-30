package com.playme.player.feature.player.vm

import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Queue navigation and playback-end handling. The controller asks the
 * ViewModel to load the next item through [playVideo] so engine ownership
 * stays in one place.
 */
class PlayerQueueController(
    private val state: MutableStateFlow<PlayerState>,
    private val effects: PlayerSessionEffects,
    private val transport: PlayerTransportController,
    private val playVideo: (VideoItem, List<VideoItem>) -> Unit
) {

    fun onPlaybackEnded() {
        val (nextVideo, repeatSelf) = PlayerPlaybackCoordinator.getNextPlaybackVideo(state.value)
        when {
            repeatSelf -> {
                transport.seekTo(0L)
                transport.play()
            }
            nextVideo != null -> playVideo(nextVideo, state.value.playlist)
            else -> state.update { it.copy(isPlaying = false) }
        }
    }

    fun playNext() {
        val list = state.value.playlist
        if (list.isEmpty()) return
        val nextIndex = PlayerPlaybackCoordinator.getNextTrackIndex(list, state.value.currentIndex)
        playVideo(list[nextIndex], list)
    }

    fun playPrevious() {
        val list = state.value.playlist
        if (list.isEmpty()) return
        val previousIndex = PlayerPlaybackCoordinator.getPreviousTrackIndex(list, state.value.currentIndex)
        playVideo(list[previousIndex], list)
    }

    fun toggleShuffle() {
        val next = !state.value.isShuffle
        val updated = if (next) {
            PlayerQueueDelegate.shuffleQueue(state.value.playlist, state.value.currentVideo)
        } else {
            state.value.playlist
        }
        state.update { it.copy(isShuffle = next, playlist = updated) }
        effects.showToast(if (next) "Shuffle ON" else "Shuffle OFF")
    }

    fun cycleRepeatMode() {
        val next = PlayerQueueDelegate.cycleRepeat(state.value.repeatMode)
        state.update { it.copy(repeatMode = next) }
        effects.showToast("Repeat: $next")
    }

    fun clearQueue() {
        val current = state.value.currentVideo
        state.update { it.copy(playlist = if (current != null) listOf(current) else emptyList()) }
        effects.showToast("Queue Cleared")
    }
}
