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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playme.player.core.designsystem.PlayMeColors
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PlayMeColors.ObsidianDark)
    ) {
        // 1. Top Brand & Search Bar
        LibraryTopBar(
            searchQuery = state.searchQuery,
            onSearchQueryChange = { viewModel.onAction(LibraryAction.SetSearchQuery(it)) }
        )

        // 2. Permission Banner (Deep Storage)
        PermissionBanner(hasDeepAccess = state.hasDeepScanAccess)

        // 3. Folder Drill-Down Header (if inside a folder)
        state.selectedFolder?.let { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PlayMeColors.GlassSurface)
                    .clickable { viewModel.onAction(LibraryAction.SelectFolder(null)) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to folders",
                    tint = PlayMeColors.ElectricViolet,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = folder.name,
                    color = PlayMeColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 4. Continue Watching Row (only on main video list without active search)
        if (state.searchQuery.isEmpty() && state.selectedFolder == null && state.activeSubTab == VideoSubTab.VIDEOS) {
            ContinueWatchingRow(
                items = state.continueWatching,
                onItemClick = { item ->
                    val video = state.allVideos.firstOrNull { it.id == item.id || it.path == item.path }
                    if (video != null) onVideoClick(video)
                }
            )
        }

        // 5. Sub-Nav Bar (Videos/Folders toggle, Sort, View mode)
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
                    CircularProgressIndicator(color = PlayMeColors.ElectricViolet)
                }
            } else if (state.activeSubTab == VideoSubTab.FOLDERS && state.selectedFolder == null) {
                // Folders List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                tint = PlayMeColors.TextTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No videos found", color = PlayMeColors.TextSecondary, fontSize = 15.sp)
                        }
                    }
                } else if (state.viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
