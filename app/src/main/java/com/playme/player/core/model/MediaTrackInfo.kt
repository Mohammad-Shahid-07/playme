package com.playme.player.core.model

enum class TrackType {
    AUDIO, SUBTITLE
}

data class MediaTrackInfo(
    val id: String,
    val name: String,
    val language: String? = null,
    val type: TrackType,
    val isSelected: Boolean = false
)
