package com.playme.player.feature.library.model

import com.playme.player.core.model.ContinueWatchingItem
import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.VideoSubTab
import com.playme.player.core.model.ViewMode

data class LibraryState(
    val isLoading: Boolean = false,
    val allVideos: List<VideoItem> = emptyList(),
    val displayedVideos: List<VideoItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val activeSubTab: VideoSubTab = VideoSubTab.VIDEOS,
    val sortOption: SortOption = SortOption.NEWEST,
    val viewMode: ViewMode = ViewMode.LIST,
    val searchQuery: String = "",
    val hasDeepScanAccess: Boolean = false,
    val selectedFolder: FolderItem? = null
)
