package com.playme.player.feature.library.model

import androidx.compose.runtime.Immutable
import com.playme.player.core.model.ContinueWatchingItem
import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.SeriesItem
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.VideoSubTab
import com.playme.player.core.model.ViewMode

@Immutable
data class LibraryState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val allVideos: List<VideoItem> = emptyList(),
    val displayedVideos: List<VideoItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val series: List<SeriesItem> = emptyList(),
    val displayedSeries: List<SeriesItem> = emptyList(),
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val activeSubTab: VideoSubTab = VideoSubTab.VIDEOS,
    val sortOption: SortOption = SortOption.NEWEST,
    val viewMode: ViewMode = ViewMode.LIST,
    val searchQuery: String = "",
    val selectedFolder: FolderItem? = null,
    val selectedSeries: SeriesItem? = null
)
