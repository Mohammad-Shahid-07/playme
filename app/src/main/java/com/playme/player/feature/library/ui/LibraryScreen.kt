package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.playme.player.core.model.ViewMode
import com.playme.player.feature.library.model.LibraryAction
import com.playme.player.feature.library.vm.LibraryViewModel
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onVideoClick: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val coroutineScope = rememberCoroutineScope()

    // Direct single source of truth from pagerState - NO fighting loops!
    val currentSubTab = if (pagerState.currentPage == 0) VideoSubTab.VIDEOS else VideoSubTab.FOLDERS

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080910))
            .statusBarsPadding()
            .padding(horizontal = 10.dp)
            .padding(top = 4.dp)
    ) {
        // 1. Home App Brand Top Bar
        LibraryTopBar(
            searchQuery = state.searchQuery,
            onSearchQueryChange = { viewModel.onAction(LibraryAction.SetSearchQuery(it)) }
        )

        // 2. Permission Banner (Deep Storage)
        PermissionBanner(hasDeepAccess = state.hasDeepScanAccess)

        // 3. Folder Drill-Down Back Header
        state.selectedFolder?.let { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x14FFFFFF))
                    .clickable { viewModel.onAction(LibraryAction.SelectFolder(null)) }
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
                    text = folder.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 4. Continue Watching Carousel (Persistent across both tabs)
        if (state.searchQuery.isEmpty() && state.selectedFolder == null) {
            ContinueWatchingRow(
                items = state.continueWatching,
                onItemClick = { item ->
                    val video = state.allVideos.firstOrNull { it.id == item.id || it.path == item.path }
                    if (video != null) onVideoClick(video)
                }
            )
        }

        // 5. Video Sub-Nav Bar (Videos / Folders, Sort, View mode)
        VideoSubNavBar(
            activeTab = currentSubTab,
            sortOption = state.sortOption,
            viewMode = state.viewMode,
            onTabChange = { tab ->
                val target = if (tab == VideoSubTab.VIDEOS) 0 else 1
                coroutineScope.launch {
                    pagerState.animateScrollToPage(target)
                }
            },
            onSortChange = { viewModel.onAction(LibraryAction.SetSortOption(it)) },
            onViewModeToggle = { viewModel.onAction(LibraryAction.ToggleViewMode) }
        )

        // 6. Smooth Swipable Animated Pager with Zero Recomposition Jitter
        if (state.isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF818CF8), strokeWidth = 2.5.dp)
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                userScrollEnabled = state.selectedFolder == null,
                key = { it }
            ) { page ->
                if (page == 0) {
                    // Page 0: Videos Feed
                    if (state.displayedVideos.isEmpty()) {
                        EmptyVideosView()
                    } else if (state.viewMode == ViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.displayedVideos,
                                key = { it.id },
                                contentType = { "video_grid" }
                            ) { video ->
                                VideoCardItem(
                                    video = video,
                                    viewMode = ViewMode.GRID,
                                    onClick = { onVideoClick(video) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.displayedVideos,
                                key = { it.id },
                                contentType = { "video_list" }
                            ) { video ->
                                VideoCardItem(
                                    video = video,
                                    viewMode = ViewMode.LIST,
                                    onClick = { onVideoClick(video) }
                                )
                            }
                        }
                    }
                } else {
                    // Page 1: Folders Feed
                    if (state.viewMode == ViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.folders,
                                key = { it.id },
                                contentType = { "folder_grid" }
                            ) { folder ->
                                FolderCardItem(
                                    folder = folder,
                                    viewMode = ViewMode.GRID,
                                    onClick = { viewModel.onAction(LibraryAction.SelectFolder(folder)) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = state.folders,
                                key = { it.id },
                                contentType = { "folder_list" }
                            ) { folder ->
                                FolderCardItem(
                                    folder = folder,
                                    viewMode = ViewMode.LIST,
                                    onClick = { viewModel.onAction(LibraryAction.SelectFolder(folder)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyVideosView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.VideocamOff,
                contentDescription = null,
                tint = Color(0x61FFFFFF),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "No videos found", color = Color(0xFF8E95A5), fontSize = 13.5.sp)
        }
    }
}
