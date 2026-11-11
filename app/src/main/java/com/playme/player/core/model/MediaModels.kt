package com.playme.player.core.model

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class BadgeVariant {
    GOLD, BLUE, GRAY, GREEN
}

@Immutable
data class VideoBadge(
    val label: String,
    val variant: BadgeVariant = BadgeVariant.GRAY
)

enum class VideoSubTab {
    VIDEOS, FOLDERS, SERIES
}

enum class SortOption {
    NEWEST, NAME, SIZE, DURATION
}

enum class ViewMode {
    LIST, GRID
}

/**
 * `@Immutable` matters for scrolling: `Uri` is an Android class Compose cannot
 * prove stable, so without this every card recomposes on each state tick.
 */
@Immutable
data class VideoItem(
    val id: String,
    val title: String,
    val uri: Uri = Uri.EMPTY,
    val path: String = "",
    val durationMs: Long = 0L,
    val durationFormatted: String = "00:00",
    val sizeBytes: Long = 0L,
    val fileSizeFormatted: String = "",
    val format: String = "MP4",
    val resolution: String = "1080p",
    val year: String = "",
    val thumbnailUrl: String? = null,
    val dateModifiedMs: Long = 0L,
    val isNew: Boolean = false,
    val watchProgressPercent: Float = 0f,
    val badges: List<VideoBadge> = emptyList(),
    val parentFolder: String = ""
)

@Immutable
data class FolderItem(
    val id: String,
    val name: String,
    val path: String,
    val videoCount: Int,
    val totalSizeBytes: Long = 0L,
    val totalSizeFormatted: String = "",
    val thumbnailUrl: String? = null
)

@Immutable
data class ContinueWatchingItem(
    val id: String,
    val title: String,
    val durationFormatted: String,
    val timeLeftFormatted: String,
    val progressPercent: Float,
    val thumbnailUrl: String? = null,
    val videoUri: Uri = Uri.EMPTY,
    val path: String = ""
)
