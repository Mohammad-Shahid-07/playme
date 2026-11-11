package com.playme.player.feature.library.vm

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.history.WatchProgressStore
import com.playme.player.core.model.LibraryPhase
import com.playme.player.core.model.LibrarySnapshot
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.VideoSubTab
import com.playme.player.core.model.ViewMode
import com.playme.player.core.repository.VideoRepository
import com.playme.player.feature.library.model.LibraryAction
import com.playme.player.feature.library.model.LibraryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application.applicationContext)
    private val progressStore = WatchProgressStore(application.applicationContext)

    private val _state = MutableStateFlow(LibraryState(isLoading = true))
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)
    private var lastRefreshAt = SystemClock.elapsedRealtime()

    init {
        repository.observeMediaChanges()
            .onEach { requestRefresh(force = true) }
            .launchIn(viewModelScope)

        refreshTrigger
            .flatMapLatest { repository.observeLibrary() }
            .onEach { snapshot ->
                val current = _state.value
                val scanning = snapshot.phase != LibraryPhase.COMPLETE
                // A deep scan emits every 800ms. Re-publishing an identical
                // list forces the whole library to recompose mid-scroll, so
                // only the scanning flag is refreshed in that case.
                if (current.allVideos == snapshot.videos && current.isScanning == scanning) {
                    return@onEach
                }
                if (current.allVideos == snapshot.videos) {
                    _state.value = current.copy(isScanning = scanning)
                    return@onEach
                }
                // Folder grouping and the watch-history read are off-main work.
                _state.value = withContext(Dispatchers.IO) { reduce(current, snapshot) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Coalesces refresh requests: a file copy emits a burst of MediaStore
     * changes, and a resume right after launch must not cancel the first scan.
     */
    private fun requestRefresh(force: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastRefreshAt < MIN_REFRESH_INTERVAL_MS) return
        lastRefreshAt = now
        refreshTrigger.update { it + 1 }
    }

    fun onAction(action: LibraryAction) {
        when (action) {
            is LibraryAction.RefreshVideos -> requestRefresh()
            is LibraryAction.SetSubTab -> _state.update { it.copy(activeSubTab = action.tab) }
            is LibraryAction.SetSortOption -> _state.update { current ->
                current.copy(
                    sortOption = action.option,
                    displayedVideos = repository.sortVideos(current.displayedVideos, action.option),
                    displayedSeries = repository.sortSeries(current.displayedSeries, action.option)
                )
            }
            is LibraryAction.ToggleViewMode -> _state.update {
                it.copy(viewMode = if (it.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST)
            }
            is LibraryAction.SetSearchQuery -> _state.update { current ->
                current.copy(
                    searchQuery = action.query,
                    displayedVideos = filterVideos(current, action.query, current.selectedFolder?.path),
                    displayedSeries = repository.sortSeries(
                        repository.searchSeries(current.series, action.query),
                        current.sortOption
                    )
                )
            }
            is LibraryAction.SelectFolder -> _state.update { current ->
                current.copy(
                    selectedFolder = action.folder,
                    selectedSeries = null,
                    displayedVideos = filterVideos(current, current.searchQuery, action.folder?.path),
                    activeSubTab = if (action.folder != null) VideoSubTab.VIDEOS else current.activeSubTab
                )
            }
            is LibraryAction.SelectSeries -> _state.update {
                it.copy(selectedSeries = action.series, selectedFolder = null)
            }
            is LibraryAction.ClearWatchHistory -> {
                progressStore.clear()
                _state.update { it.copy(continueWatching = emptyList()) }
            }
            is LibraryAction.PlayVideo -> Unit
        }
    }

    private fun reduce(current: LibraryState, snapshot: LibrarySnapshot): LibraryState {
        val folders = repository.groupIntoFolders(snapshot.videos)
        val selectedFolder = current.selectedFolder?.let { selected ->
            folders.firstOrNull { it.path == selected.path }
        }
        val selectedSeries = current.selectedSeries?.let { selected ->
            snapshot.series.firstOrNull { it.id == selected.id }
        }

        return current.copy(
            isLoading = false,
            isScanning = snapshot.phase != LibraryPhase.COMPLETE,
            allVideos = snapshot.videos,
            displayedVideos = filterVideos(
                all = snapshot.videos,
                query = current.searchQuery,
                folderPath = selectedFolder?.path,
                sort = current.sortOption
            ),
            folders = folders,
            series = snapshot.series,
            displayedSeries = repository.sortSeries(
                repository.searchSeries(snapshot.series, current.searchQuery),
                current.sortOption
            ),
            continueWatching = progressStore.toContinueWatching(snapshot.videos),
            selectedFolder = selectedFolder,
            selectedSeries = selectedSeries
        )
    }

    private fun filterVideos(
        current: LibraryState,
        query: String,
        folderPath: String?
    ): List<VideoItem> = filterVideos(current.allVideos, query, folderPath, current.sortOption)

    private fun filterVideos(
        all: List<VideoItem>,
        query: String,
        folderPath: String?,
        sort: SortOption
    ): List<VideoItem> {
        var result = all
        if (folderPath != null) {
            result = result.filter { File(it.path).parentFile?.absolutePath == folderPath }
        }
        if (query.isNotBlank()) {
            result = repository.searchVideos(result, query)
        }
        return repository.sortVideos(result, sort)
    }

    private companion object {
        const val MIN_REFRESH_INTERVAL_MS = 4_000L
    }
}
