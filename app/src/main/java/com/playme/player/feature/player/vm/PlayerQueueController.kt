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
    private val onPlaybackCompleted: () -> Unit = {},
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
            else -> {
                onPlaybackCompleted()
                state.update { it.copy(isPlaying = false) }
            }
        }
    }

    fun playNext() {
        val list = state.value.playlist
        if (list.isEmpty()) return
        val nextIndex = PlayerPlaybackCoordinator.getNextTrackIndex(list, state.value.currentIndex)
        list.getOrNull(nextIndex)?.let { playVideo(it, list) }
    }

    fun playPrevious() {
        val list = state.value.playlist
        if (list.isEmpty()) return
        val previousIndex = PlayerPlaybackCoordinator.getPreviousTrackIndex(list, state.value.currentIndex)
        list.getOrNull(previousIndex)?.let { playVideo(it, list) }
    }

    fun toggleShuffle() {
        val snapshot = state.value
        val next = !snapshot.isShuffle
        if (next) {
            val shuffled = PlayerQueueDelegate.shuffleQueue(snapshot.playlist, snapshot.currentVideo)
            val newIndex = snapshot.currentVideo?.let { cur ->
                shuffled.indexOfFirst { it.id == cur.id }.coerceAtLeast(0)
            } ?: 0
            state.update {
                it.copy(
                    isShuffle = true,
                    preShufflePlaylist = snapshot.playlist,
                    playlist = shuffled,
                    currentIndex = newIndex
                )
            }
        } else {
            val restored = PlayerQueueDelegate.restoreUnshuffledQueue(
                currentPlaylist = snapshot.playlist,
                preShufflePlaylist = snapshot.preShufflePlaylist
            )
            val newIndex = snapshot.currentVideo?.let { cur ->
                restored.indexOfFirst { it.id == cur.id }.coerceAtLeast(0)
            } ?: 0
            state.update {
                it.copy(
                    isShuffle = false,
                    preShufflePlaylist = emptyList(),
                    playlist = restored,
                    currentIndex = newIndex
                )
            }
        }
        effects.showToast(if (next) "Shuffle ON" else "Shuffle OFF")
    }

    fun cycleRepeatMode() {
        val next = PlayerQueueDelegate.cycleRepeat(state.value.repeatMode)
        state.update { it.copy(repeatMode = next) }
        effects.showToast("Repeat: $next")
    }

    fun removeFromQueue(videoId: String) {
        val before = state.value
        val updated = PlayerQueueDelegate.removeTrackFromState(before, videoId)
        state.value = updated
        if (before.currentVideo?.id == videoId) {
            val replacement = updated.playlist.getOrNull(updated.currentIndex)
            if (replacement != null) {
                playVideo(replacement, updated.playlist)
            } else {
                transport.pause()
            }
        }
    }

    fun clearQueue() {
        val current = state.value.currentVideo
        val remaining = if (current != null) listOf(current) else emptyList()
        state.update {
            it.copy(
                playlist = remaining,
                preShufflePlaylist = if (it.isShuffle) remaining else emptyList(),
                currentIndex = 0
            )
        }
        effects.showToast("Queue Cleared")
    }
}
