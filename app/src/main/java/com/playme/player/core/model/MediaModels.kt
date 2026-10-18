package com.playme.player.core.model

import android.net.Uri

enum class BadgeVariant {
    GOLD, BLUE, GRAY, GREEN
}

data class VideoBadge(
    val label: String,
    val variant: BadgeVariant = BadgeVariant.GRAY
)

data class VideoItem(
    val id: String,
    val title: String,
    val uri: Uri = Uri.EMPTY,
    val path: String = "",
    val durationMs: Long = 0L,
    val durationFormatted: String = "00:00",
    val fileSizeFormatted: String = "",
    val format: String = "MP4",
    val year: String = "",
    val thumbnailUrl: String? = null,
    val isNew: Boolean = false,
    val watchProgressPercent: Float = 0f,
    val badges: List<VideoBadge> = emptyList()
)

data class FolderItem(
    val id: String,
    val name: String,
    val path: String,
    val videoCount: Int,
    val totalSizeFormatted: String
)
