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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onVideoClick: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    // Outer Viewport: #080910 Workbench Obsidian matching VideoPlayer.css
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080910))
            .padding(horizontal = 10.dp)
            .padding(top = 6.dp)
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

        // 4. Continue Watching Carousel
        if (state.searchQuery.isEmpty() && state.selectedFolder == null && state.activeSubTab == VideoSubTab.VIDEOS) {
            ContinueWatchingRow(
                items = state.continueWatching,
                onItemClick = { item ->
                    val video = state.allVideos.firstOrNull { it.id == item.id || it.path == item.path }
                    if (video != null) onVideoClick(video)
                }
            )
        }

        // 5. Video Sub-Nav Bar (Videos/Folders, Sort, View mode)
        VideoSubNavBar(
            activeTab = state.activeSubTab,
            sortOption = state.sortOption,
            viewMode = state.viewMode,
            onTabChange = { viewModel.onAction(LibraryAction.SetSubTab(it)) },
            onSortChange = { viewModel.onAction(LibraryAction.SetSortOption(it)) },
            onViewModeToggle = { viewModel.onAction(LibraryAction.ToggleViewMode) }
        )

        // 6. Main Feed Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF818CF8), strokeWidth = 2.5.dp)
                }
            } else if (state.activeSubTab == VideoSubTab.FOLDERS && state.selectedFolder == null) {
                // Folder List: 8px gap matching FolderList.css
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.folders) { folder ->
                        FolderCardItem(
                            folder = folder,
                            onClick = { viewModel.onAction(LibraryAction.SelectFolder(folder)) }
                        )
                    }
                }
            } else {
                // Videos Feed
                if (state.displayedVideos.isEmpty()) {
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
                } else if (state.viewMode == ViewMode.GRID) {
                    // Grid Mode: 2 columns, 10px gap matching VideoFeed.css
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.displayedVideos) { video ->
                            VideoCardItem(
                                video = video,
                                viewMode = ViewMode.GRID,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }
                } else {
                    // List Mode: 8px gap matching VideoFeed.css
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.displayedVideos) { video ->
                            VideoCardItem(
                                video = video,
                                viewMode = ViewMode.LIST,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}
