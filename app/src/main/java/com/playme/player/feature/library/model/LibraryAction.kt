package com.playme.player.feature.library.model

import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.SeriesItem
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.VideoSubTab

sealed interface LibraryAction {
    data object RefreshVideos : LibraryAction
    data class SetSubTab(val tab: VideoSubTab) : LibraryAction
    data class SetSortOption(val option: SortOption) : LibraryAction
    data object ToggleViewMode : LibraryAction
    data class SetSearchQuery(val query: String) : LibraryAction
    data class SelectFolder(val folder: FolderItem?) : LibraryAction
    data class SelectSeries(val series: SeriesItem?) : LibraryAction
    data object ClearWatchHistory : LibraryAction
    data class PlayVideo(val video: VideoItem) : LibraryAction
}
