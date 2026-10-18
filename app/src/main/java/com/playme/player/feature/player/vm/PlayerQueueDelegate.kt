package com.playme.player.feature.player.vm

import com.playme.player.core.model.VideoItem

object PlayerQueueDelegate {

    fun cycleRepeat(current: String): String = when (current) {
        "off" -> "all"
        "all" -> "one"
        else -> "off"
    }

    fun removeTrack(playlist: List<VideoItem>, videoId: String): List<VideoItem> =
        playlist.filter { it.id != videoId }

    fun shuffleQueue(playlist: List<VideoItem>, currentVideo: VideoItem?): List<VideoItem> {
        if (playlist.size <= 1) return playlist
        val rest = playlist.filter { it.id != currentVideo?.id }.shuffled()
        return if (currentVideo != null) listOf(currentVideo) + rest else rest
    }
}
