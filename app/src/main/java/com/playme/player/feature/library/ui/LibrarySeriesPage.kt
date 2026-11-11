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
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.playme.player.core.model.SeriesItem
import com.playme.player.core.model.ViewMode

@Composable
fun LibrarySeriesPage(
    series: List<SeriesItem>,
    viewMode: ViewMode,
    onSeriesClick: (SeriesItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (series.isEmpty()) {
        LibraryEmptyState(
            icon = Icons.Rounded.VideoLibrary,
            title = "No series detected",
            description = "Files that share a title and carry episode numbers " +
                "(S01E02, EP04, Part 2) are grouped here automatically.",
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
                    items = series,
                    key = { "${it.id}:${it.title}:${it.year ?: ""}" },
                    contentType = { "series_grid" }
                ) { item ->
                    SeriesCardItem(
                        series = item,
                        viewMode = ViewMode.GRID,
                        onClick = { onSeriesClick(item) }
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
                    items = series,
                    key = { "${it.id}:${it.title}:${it.year ?: ""}" },
                    contentType = { "series_list" }
                ) { item ->
                    SeriesCardItem(
                        series = item,
                        viewMode = ViewMode.LIST,
                        onClick = { onSeriesClick(item) }
                    )
                }
            }
        }
    }
}
