package com.playme.player.feature.player.vm

import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.PlayerState

object PlayerQueueDelegate {

    fun cycleRepeat(current: String): String = when (current) {
        "off" -> "all"
        "all" -> "one"
        else -> "off"
    }

    fun removeTrack(playlist: List<VideoItem>, videoId: String): List<VideoItem> =
        playlist.filter { it.id != videoId }

    /**
     * Removes [videoId] from both the active playlist and any saved pre-shuffle order,
     * keeping [PlayerState.currentIndex] aligned with the currently playing item.
     */
    fun removeTrackFromState(state: PlayerState, videoId: String): PlayerState {
        val newPlaylist = removeTrack(state.playlist, videoId)
        val newPreShuffle = if (state.preShufflePlaylist.isNotEmpty()) {
            removeTrack(state.preShufflePlaylist, videoId)
        } else {
            emptyList()
        }
        val currentId = state.currentVideo?.id
        val newIndex = when {
            newPlaylist.isEmpty() -> 0
            currentId != null && currentId != videoId ->
                newPlaylist.indexOfFirst { it.id == currentId }
                    .takeIf { it >= 0 }
                    ?: state.currentIndex.coerceIn(0, newPlaylist.lastIndex)
            else -> state.currentIndex.coerceIn(0, newPlaylist.lastIndex)
        }
        return state.copy(
            playlist = newPlaylist,
            preShufflePlaylist = newPreShuffle,
            currentIndex = newIndex
        )
    }

    fun shuffleQueue(playlist: List<VideoItem>, currentVideo: VideoItem?): List<VideoItem> {
        if (playlist.size <= 1) return playlist
        val rest = playlist.filter { it.id != currentVideo?.id }.shuffled()
        return if (currentVideo != null) listOf(currentVideo) + rest else rest
    }

    fun restoreUnshuffledQueue(currentPlaylist: List<VideoItem>, preShufflePlaylist: List<VideoItem>): List<VideoItem> {
        if (preShufflePlaylist.isEmpty()) return currentPlaylist
        val survivingIds = currentPlaylist.mapTo(HashSet()) { it.id }
        val base = preShufflePlaylist.filter { it.id in survivingIds }
        val baseIds = base.mapTo(HashSet()) { it.id }
        val extras = currentPlaylist.filter { it.id !in baseIds }
        return (base + extras).ifEmpty { currentPlaylist }
    }
}
