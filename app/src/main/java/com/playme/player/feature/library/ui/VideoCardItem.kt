package com.playme.player.feature.library.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.playme.player.core.model.VideoItem
import com.playme.player.core.model.ViewMode

@Composable
fun VideoCardItem(
    video: VideoItem,
    viewMode: ViewMode,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (viewMode == ViewMode.GRID) {
        VideoGridCard(
            video = video,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        VideoListCard(
            video = video,
            onClick = onClick,
            onMoreClick = onMoreClick,
            modifier = modifier
        )
    }
}
