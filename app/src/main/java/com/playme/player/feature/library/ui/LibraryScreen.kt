package com.playme.player.feature.library.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.VideoSubTab
import com.playme.player.feature.library.model.LibraryAction
import com.playme.player.feature.library.vm.LibraryViewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onVideoClick: (VideoItem, List<VideoItem>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(initialPage = state.activeSubTab.ordinal) { PAGE_COUNT }
    val scope = rememberCoroutineScope()
    val currentTab = VideoSubTab.entries[pagerState.currentPage.coerceIn(0, PAGE_COUNT - 1)]
    val selectedSeries = state.selectedSeries
    val selectedFolder = state.selectedFolder

    // Files copied while the app was in the background (or into a .nomedia
    // folder) are picked up as soon as the library is visible again.
    LibraryResumeRefresh { viewModel.onAction(LibraryAction.RefreshVideos) }

    LaunchedEffect(pagerState.currentPage) {
        if (selectedFolder == null && state.activeSubTab != currentTab) {
            viewModel.onAction(LibraryAction.SetSubTab(currentTab))
        }
    }

    LaunchedEffect(selectedFolder) {
        if (selectedFolder != null && pagerState.currentPage != VideoSubTab.VIDEOS.ordinal) {
            pagerState.scrollToPage(VideoSubTab.VIDEOS.ordinal)
        }
    }

    BackHandler(enabled = selectedSeries != null || selectedFolder != null) {
        if (selectedSeries != null) {
            viewModel.onAction(LibraryAction.SelectSeries(null))
        } else if (selectedFolder != null) {
            viewModel.onAction(LibraryAction.SelectFolder(null))
            scope.launch { pagerState.animateScrollToPage(VideoSubTab.FOLDERS.ordinal) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080910))
            .statusBarsPadding()
            .padding(horizontal = 10.dp)
            .padding(top = 4.dp)
    ) {
        LibraryTopBar(
            searchQuery = state.searchQuery,
            onSearchQueryChange = { viewModel.onAction(LibraryAction.SetSearchQuery(it)) }
        )

        LibraryScanIndicator(isScanning = state.isScanning)

        if (selectedSeries != null) {
            SeriesDetailView(
                series = selectedSeries,
                onBack = { viewModel.onAction(LibraryAction.SelectSeries(null)) },
                onEpisodeClick = { episode ->
                    onVideoClick(episode.video, selectedSeries.episodes.map { it.video })
                }
            )
            return@Column
        }

        selectedFolder?.let { folder ->
            FolderBackHeader(
                name = folder.name,
                onClick = {
                    viewModel.onAction(LibraryAction.SelectFolder(null))
                    scope.launch { pagerState.animateScrollToPage(VideoSubTab.FOLDERS.ordinal) }
                }
            )
        }

        if (state.searchQuery.isEmpty() && selectedFolder == null) {
            ContinueWatchingRow(
                items = state.continueWatching,
                onItemClick = { item ->
                    val video = state.allVideos.firstOrNull { it.id == item.id || it.path == item.path }
                    if (video != null) onVideoClick(video, state.allVideos)
                },
                onClear = { viewModel.onAction(LibraryAction.ClearWatchHistory) }
            )
        }

        VideoSubNavBar(
            activeTab = if (selectedFolder != null) VideoSubTab.VIDEOS else currentTab,
            sortOption = state.sortOption,
            viewMode = state.viewMode,
            onTabChange = { tab ->
                if (selectedFolder != null) {
                    viewModel.onAction(LibraryAction.SelectFolder(null))
                }
                scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
            },
            onSortChange = { viewModel.onAction(LibraryAction.SetSortOption(it)) },
            onViewModeToggle = { viewModel.onAction(LibraryAction.ToggleViewMode) }
        )

        if (state.isLoading) {
            LoadingState(modifier = Modifier.weight(1f))
        } else {
            CompositionLocalProvider(
                LocalListScrolling provides pagerState.isScrollInProgress
            ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                userScrollEnabled = state.selectedFolder == null,
                key = { it }
            ) { page ->
                when (page) {
                    0 -> LibraryVideosPage(
                        videos = state.displayedVideos,
                        viewMode = state.viewMode,
                        onVideoClick = { video ->
                            onVideoClick(video, state.displayedVideos.ifEmpty { state.allVideos })
                        }
                    )
                    1 -> LibraryFoldersPage(
                        folders = state.folders,
                        viewMode = state.viewMode,
                        onFolderClick = { folder ->
                            viewModel.onAction(LibraryAction.SelectFolder(folder))
                            scope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )
                    else -> LibrarySeriesPage(
                        series = state.displayedSeries,
                        viewMode = state.viewMode,
                        onSeriesClick = { series ->
                            viewModel.onAction(LibraryAction.SelectSeries(series))
                        }
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun FolderBackHeader(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x14FFFFFF))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back to folders",
            tint = Color(0xFF818CF8),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF818CF8), strokeWidth = 2.5.dp)
            Text(
                text = "Scanning storage",
                color = Color(0xFF8E95A5),
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private const val PAGE_COUNT = 3
