package com.playme.player.feature.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.ViewMode

@Composable
fun LibraryFoldersPage(
    folders: List<FolderItem>,
    viewMode: ViewMode,
    onFolderClick: (FolderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (folders.isEmpty()) {
        LibraryEmptyState(
            icon = Icons.Rounded.FolderOff,
            title = "No folders yet",
            description = "Device folders appear once videos are indexed.",
            modifier = modifier
        )
        return
    }

    if (viewMode == ViewMode.GRID) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = folders, key = { it.path }, contentType = { "folder_grid" }) { folder ->
                FolderCardItem(
                    folder = folder,
                    viewMode = ViewMode.GRID,
                    onClick = { onFolderClick(folder) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = folders, key = { it.path }, contentType = { "folder_list" }) { folder ->
                FolderCardItem(
                    folder = folder,
                    viewMode = ViewMode.LIST,
                    onClick = { onFolderClick(folder) }
                )
            }
        }
    }
}
