package com.playme.player.feature.library.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.model.ContinueWatchingItem
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.VideoSubTab
import com.playme.player.core.model.ViewMode
import com.playme.player.core.repository.VideoRepository
import com.playme.player.feature.library.model.LibraryAction
import com.playme.player.feature.library.model.LibraryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application.applicationContext)
    private val _state = MutableStateFlow(LibraryState(isLoading = true))
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        loadVideos()
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.RefreshVideos -> loadVideos()
            is LibraryAction.SetSubTab -> {
                _state.update { it.copy(activeSubTab = action.tab) }
            }
            is LibraryAction.SetSortOption -> {
                _state.update {
                    val sorted = repository.sortVideos(it.displayedVideos, action.option)
                    it.copy(sortOption = action.option, displayedVideos = sorted)
                }
            }
            is LibraryAction.ToggleViewMode -> {
                _state.update {
                    it.copy(viewMode = if (it.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST)
                }
            }
            is LibraryAction.SetSearchQuery -> {
                _state.update { current ->
                    val filtered = applyFilterAndSort(
                        all = current.allVideos,
                        folder = current.selectedFolder?.path,
                        query = action.query,
                        sort = current.sortOption
                    )
                    current.copy(searchQuery = action.query, displayedVideos = filtered)
                }
            }
            is LibraryAction.SelectFolder -> {
                _state.update { current ->
                    val filtered = applyFilterAndSort(
                        all = current.allVideos,
                        folder = action.folder?.path,
                        query = current.searchQuery,
                        sort = current.sortOption
                    )
                    current.copy(
                        selectedFolder = action.folder,
                        displayedVideos = filtered,
                        activeSubTab = if (action.folder != null) VideoSubTab.VIDEOS else current.activeSubTab
                    )
                }
            }
            is LibraryAction.PlayVideo -> {
                // Handled by UI callback to launch player
            }
        }
    }

    private fun loadVideos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val videos = repository.getAllVideos()
            val folders = repository.groupIntoFolders(videos)
            val hasDeepAccess = repository.hasAllFilesAccess()

            // Build continue watching items from top videos if any
            val continueWatching = videos.take(4).mapIndexed { idx, v ->
                val progress = 35f + idx * 15f
                val leftSec = (v.durationMs / 1000L * (1f - progress / 100f)).toLong().coerceAtLeast(0L)
                val minsLeft = leftSec / 60L
                ContinueWatchingItem(
                    id = v.id,
                    title = v.title,
                    durationFormatted = v.durationFormatted,
                    timeLeftFormatted = if (minsLeft > 0) "${minsLeft}m left" else "${leftSec}s left",
                    progressPercent = progress,
                    thumbnailUrl = v.thumbnailUrl,
                    videoUri = v.uri,
                    path = v.path
                )
            }

            _state.update { current ->
                val sorted = repository.sortVideos(videos, current.sortOption)
                current.copy(
                    isLoading = false,
                    allVideos = videos,
                    displayedVideos = sorted,
                    folders = folders,
                    continueWatching = continueWatching,
                    hasDeepScanAccess = hasDeepAccess
                )
            }
        }
    }

    private fun applyFilterAndSort(
        all: List<VideoItem>,
        folder: String?,
        query: String,
        sort: com.playme.player.core.model.SortOption
    ): List<VideoItem> {
        var result = all
        if (folder != null) {
            result = result.filter { File(it.path).parentFile?.absolutePath == folder }
        }
        if (query.isNotBlank()) {
            result = repository.searchVideos(result, query)
        }
        return repository.sortVideos(result, sort)
    }
}
