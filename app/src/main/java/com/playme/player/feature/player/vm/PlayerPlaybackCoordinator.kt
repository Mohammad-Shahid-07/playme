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
        val safe = currentIndex.coerceIn(0, list.lastIndex)
        return (safe + 1) % list.size
    }

    fun getPreviousTrackIndex(list: List<VideoItem>, currentIndex: Int): Int {
        if (list.isEmpty()) return 0
        val safe = currentIndex.coerceIn(0, list.lastIndex)
        return if (safe == 0) list.lastIndex else safe - 1
    }

    fun getNextPlaybackVideo(state: PlayerState): Pair<VideoItem?, Boolean> {
        val list = state.playlist
        return when (state.repeatMode) {
            "one" -> null to true
            "all" -> {
                if (list.isEmpty()) null to false else list[getNextTrackIndex(list, state.currentIndex)] to false
            }
            else -> {
                if (list.isEmpty()) return null to false
                val idx = state.currentIndex.coerceIn(0, list.lastIndex)
                if (state.isAutoplay && idx < list.lastIndex) {
                    list[getNextTrackIndex(list, idx)] to false
                } else {
                    null to false
                }
            }
        }
    }
}
