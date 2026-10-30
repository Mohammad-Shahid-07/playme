package com.playme.player.feature.player.vm

import com.playme.player.core.model.VideoItem
import com.playme.player.feature.player.model.PlayerState

object PlayerPlaybackCoordinator {

    val speedPresets = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.5f)

    fun getNextSpeed(current: Float): Float {
        val idx = (speedPresets.indexOf(current) + 1) % speedPresets.size
        return speedPresets[idx]
    }

    fun isAbLoopBoundaryReached(state: PlayerState, currentPosMs: Long): Boolean {
        if (!state.isAbLoopActive) return false
        val end = state.abLoopEndMs ?: state.durationMs
        return currentPosMs >= end
    }

    fun getNextSleepTimer(currentMinutes: Int, requestedMinutes: Int): Int {
        if (requestedMinutes != -1) return requestedMinutes
        return when (currentMinutes) {
            0 -> 15
            15 -> 30
            30 -> 60
            else -> 0
        }
    }

    fun getNextTrackIndex(list: List<VideoItem>, currentIndex: Int): Int {
        if (list.isEmpty()) return 0
        return (currentIndex + 1) % list.size
    }

    fun getPreviousTrackIndex(list: List<VideoItem>, currentIndex: Int): Int {
        if (list.isEmpty()) return 0
        return if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
    }

    fun getNextPlaybackVideo(state: PlayerState): Pair<VideoItem?, Boolean> {
        return when (state.repeatMode) {
            "one" -> null to true
            "all" -> {
                val list = state.playlist
                if (list.isEmpty()) null to false else list[getNextTrackIndex(list, state.currentIndex)] to false
            }
            else -> {
                val list = state.playlist
                val idx = state.currentIndex
                if (state.isAutoplay && idx < list.size - 1) {
                    list[getNextTrackIndex(list, idx)] to false
                } else {
                    null to false
                }
            }
        }
    }
}
