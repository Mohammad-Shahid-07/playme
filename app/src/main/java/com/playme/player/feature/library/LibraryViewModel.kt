package com.playme.player.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playme.player.core.database.entity.PlaybackHistoryEntity
import com.playme.player.core.database.entity.VaultItemEntity
import com.playme.player.core.model.VideoFolder
import com.playme.player.core.model.VideoItem
import com.playme.player.core.repository.VideoRepository
import com.playme.player.core.repository.VideoSortOrder
import com.playme.player.core.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LibraryTab {
    VIDEOS,
    FOLDERS,
    RECENT,
    VAULT,
    SETTINGS
}

enum class LibraryMediaViewMode {
    VIDEOS,
    FOLDERS
}

data class LibraryUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val scanMessage: String? = null,
    val selectedTab: LibraryTab = LibraryTab.VIDEOS,
    val mediaViewMode: LibraryMediaViewMode = LibraryMediaViewMode.VIDEOS,
    val videos: List<VideoItem> = emptyList(),
    val folders: List<VideoFolder> = emptyList(),
    val recentHistory: List<PlaybackHistoryEntity> = emptyList(),
    val vaultItems: List<VaultItemEntity> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: VideoSortOrder = VideoSortOrder.DATE_DESC,
    val selectedFolder: VideoFolder? = null,
    val lastPlayedOverall: VideoItem? = null,
    val lastPlayedInFolder: VideoItem? = null,
    val autoGroupSimilarVideos: Boolean = false,
    val folderSpeeds: Map<String, Float> = emptyMap(),
    val globalSpeed: Float = 1.0f,
    val preferredAudioLang: String = "default",
    val preferredSubtitleLang: String = "off"
)

class LibraryViewModel(
    private val repository: VideoRepository,
    val settingsManager: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LibraryUiState(
            globalSpeed = settingsManager.getGlobalPlaybackSpeed(),
            folderSpeeds = settingsManager.getAllFolderPlaybackSpeeds(),
            preferredAudioLang = settingsManager.getPreferredAudioLanguage(),
            preferredSubtitleLang = settingsManager.getPreferredSubtitleLanguage(),
            autoGroupSimilarVideos = settingsManager.isAutoGroupSimilarVideos()
        )
    )
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadMedia()
        observeHistory()
        observeVault()
    }

    fun setTab(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            selectedFolder = null,
            mediaViewMode = if (tab == LibraryTab.FOLDERS) LibraryMediaViewMode.FOLDERS else LibraryMediaViewMode.VIDEOS
        )
    }

    fun setMediaViewMode(mode: LibraryMediaViewMode) {
        _uiState.value = _uiState.value.copy(
            mediaViewMode = mode,
            selectedFolder = null
        )
        if (mode == LibraryMediaViewMode.VIDEOS && _uiState.value.selectedFolder != null) {
            loadMedia()
        }
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val videos = repository.getAllVideos(_uiState.value.sortOrder)
            val folders = repository.getVideoFolders()
            val lastOverall = videos.filter { it.lastWatchedTimestamp > 0L || it.lastPlayedPositionMs > 0L }
                .maxByOrNull { it.lastWatchedTimestamp }
                ?: videos.firstOrNull { it.contentUri == _uiState.value.recentHistory.firstOrNull()?.mediaUri }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                videos = videos,
                folders = folders,
                lastPlayedOverall = lastOverall,
                folderSpeeds = settingsManager.getAllFolderPlaybackSpeeds()
            )
        }
    }

    fun rescanAllMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, scanMessage = "Scanning storage for all media…")
            val videos = repository.getAllVideos(_uiState.value.sortOrder)
            val folders = repository.getVideoFolders()
            val lastOverall = videos.filter { it.lastWatchedTimestamp > 0L || it.lastPlayedPositionMs > 0L }
                .maxByOrNull { it.lastWatchedTimestamp }
                ?: videos.firstOrNull { it.contentUri == _uiState.value.recentHistory.firstOrNull()?.mediaUri }

            _uiState.value = _uiState.value.copy(
                isScanning = false,
                videos = videos,
                folders = folders,
                lastPlayedOverall = lastOverall,
                scanMessage = "Found ${videos.size} videos in ${folders.size} folders"
            )
        }
    }

    fun clearScanMessage() {
        _uiState.value = _uiState.value.copy(scanMessage = null)
    }

    fun setSortOrder(order: VideoSortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
        loadMedia()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isBlank()) {
                val videos = repository.getAllVideos(_uiState.value.sortOrder)
                _uiState.value = _uiState.value.copy(videos = videos)
            } else {
                val searchResults = repository.searchVideos(query)
                _uiState.value = _uiState.value.copy(videos = searchResults)
            }
        }
    }

    fun selectFolder(folder: VideoFolder?) {
        _uiState.value = _uiState.value.copy(selectedFolder = folder)
        if (folder != null) {
            viewModelScope.launch {
                val folderVideos = repository.getVideosInFolder(folder.path)
                val lastInFolder = folderVideos.filter { it.lastWatchedTimestamp > 0L || it.lastPlayedPositionMs > 0L }
                    .maxByOrNull { it.lastWatchedTimestamp }

                _uiState.value = _uiState.value.copy(
                    videos = folderVideos,
                    lastPlayedInFolder = lastInFolder
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(lastPlayedInFolder = null)
            loadMedia()
        }
    }

    fun setFolderPlaybackSpeed(folderPath: String, speed: Float?) {
        settingsManager.setFolderPlaybackSpeed(folderPath, speed)
        _uiState.value = _uiState.value.copy(folderSpeeds = settingsManager.getAllFolderPlaybackSpeeds())
    }

    fun setGlobalPlaybackSpeed(speed: Float) {
        settingsManager.setGlobalPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(globalSpeed = speed)
    }

    fun setPreferredAudio(lang: String) {
        settingsManager.setPreferredAudioLanguage(lang)
        _uiState.value = _uiState.value.copy(preferredAudioLang = lang)
    }

    fun setPreferredSubtitle(lang: String) {
        settingsManager.setPreferredSubtitleLanguage(lang)
        _uiState.value = _uiState.value.copy(preferredSubtitleLang = lang)
    }

    fun toggleAutoGrouping() {
        val next = !_uiState.value.autoGroupSimilarVideos
        settingsManager.setAutoGroupSimilarVideos(next)
        _uiState.value = _uiState.value.copy(autoGroupSimilarVideos = next)
    }

    fun getGroupedVideos(): Map<String, List<VideoItem>> {
        val vids = _uiState.value.videos
        if (!_uiState.value.autoGroupSimilarVideos || vids.isEmpty()) {
            return mapOf("All Videos" to vids)
        }

        val groups = mutableMapOf<String, MutableList<VideoItem>>()
        for (v in vids) {
            val cleanName = v.displayName.substringBeforeLast('.')
            val prefix = extractGroupPrefix(cleanName)
            groups.getOrPut(prefix) { mutableListOf() }.add(v)
        }

        val result = mutableMapOf<String, List<VideoItem>>()
        val singles = mutableListOf<VideoItem>()

        for ((prefix, list) in groups) {
            if (list.size >= 2) {
                result[prefix] = list
            } else {
                singles.addAll(list)
            }
        }
        if (singles.isNotEmpty()) {
            result["Other Videos"] = singles
        }
        return result
    }

    private fun extractGroupPrefix(name: String): String {
        val regex = Regex("""^([A-Za-z0-9_\-\s\[\]]+?)(?:[-_.\s]+(?:\d+|ep|episode|s\d+|e\d+|part|season)).*""", RegexOption.IGNORE_CASE)
        val match = regex.find(name)
        val rawPrefix = match?.groupValues?.getOrNull(1)?.trim() ?: name.take(15)
        val cleaned = rawPrefix.trimEnd('_', '-', '.', ' ')
        return if (cleaned.length >= 3) cleaned else name.take(10)
    }

    fun moveToVault(video: VideoItem) {
        viewModelScope.launch {
            repository.moveToVault(video)
            loadMedia()
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.getHistoryFlow().collect { history ->
                _uiState.value = _uiState.value.copy(recentHistory = history)
                val videos = repository.getAllVideos(_uiState.value.sortOrder)
                val lastOverall = videos.filter { it.lastWatchedTimestamp > 0L || it.lastPlayedPositionMs > 0L }
                    .maxByOrNull { it.lastWatchedTimestamp }
                    ?: videos.firstOrNull { it.contentUri == history.firstOrNull()?.mediaUri }
                _uiState.value = _uiState.value.copy(
                    videos = if (_uiState.value.selectedFolder != null) _uiState.value.videos else videos,
                    lastPlayedOverall = lastOverall
                )
            }
        }
    }

    private fun observeVault() {
        viewModelScope.launch {
            repository.getVaultItemsFlow().collect { items ->
                _uiState.value = _uiState.value.copy(vaultItems = items)
            }
        }
    }
}
