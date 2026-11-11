package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.playme.player.core.model.VideoItem

/**
 * Four-up poster collage used as a series cover.
 *
 * A single hero thumbnail reads as "one video"; the collage is what makes a
 * group of episodes legible at a glance without needing real poster art.
 */
@Composable
fun SeriesPosterMosaic(
    videos: List<VideoItem>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    gap: Dp = 2.dp
) {
    val tiles = videos.take(4)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF11131E))
    ) {
        when {
            tiles.isEmpty() -> MosaicPlaceholder(Modifier.fillMaxSize())

            tiles.size == 1 -> MosaicTile(tiles[0], Modifier.fillMaxSize())

            else -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    MosaicTile(tiles.getOrNull(0), Modifier.weight(1f).fillMaxHeight())
                    MosaicTile(tiles.getOrNull(1), Modifier.weight(1f).fillMaxHeight())
                }
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    MosaicTile(tiles.getOrNull(2), Modifier.weight(1f).fillMaxHeight())
                    MosaicTile(tiles.getOrNull(3), Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun MosaicTile(video: VideoItem?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color(0xFF141726))) {
        if (video != null) {
            VideoThumbnailImage(
                video = video,
                targetWidth = 140,
                targetHeight = 100,
                showPlaceholderGlyph = false,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            MosaicPlaceholder(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MosaicPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(Color(0xFF151827), Color(0xFF0F1220)))
        )
    )
}
