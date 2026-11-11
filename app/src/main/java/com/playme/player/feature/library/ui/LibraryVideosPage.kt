package com.playme.player.feature.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.ViewMode

@Composable
fun LibraryVideosPage(
    videos: List<VideoItem>,
    viewMode: ViewMode,
    onVideoClick: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) {
        LibraryEmptyState(
            icon = Icons.Rounded.VideocamOff,
            title = "No videos found",
            description = "Videos stored on this device will appear here.",
            modifier = modifier
        )
        return
    }

    if (viewMode == ViewMode.GRID) {
        val gridState = rememberLazyGridState()
        CompositionLocalProvider(
            LocalListScrolling provides gridState.isScrollInProgress
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = videos,
                    key = { "${it.id}:${it.path}" },
                    contentType = { "video_grid" }
                ) { video ->
                    VideoCardItem(
                        video = video,
                        viewMode = ViewMode.GRID,
                        onClick = { onVideoClick(video) }
                    )
                }
            }
        }
    } else {
        val listState = rememberLazyListState()
        CompositionLocalProvider(
            LocalListScrolling provides listState.isScrollInProgress
        ) {
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = videos,
                    key = { "${it.id}:${it.path}" },
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
    }
}
