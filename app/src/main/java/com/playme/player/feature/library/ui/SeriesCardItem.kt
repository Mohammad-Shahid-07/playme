package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.SeriesItem
import com.playme.player.core.model.ViewMode

@Composable
fun SeriesCardItem(
    series: SeriesItem,
    viewMode: ViewMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (viewMode == ViewMode.GRID) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x09FFFFFF))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(11.dp))
                .clickable(onClick = onClick)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeriesPosterMosaic(
                videos = series.episodes.map { it.video },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                cornerRadius = 8.dp
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                Text(
                    text = series.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = series.metaLine,
                    color = Color(0xFF8E95A5),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x09FFFFFF))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(11.dp))
                .clickable(onClick = onClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            SeriesPosterMosaic(
                videos = series.episodes.map { it.video },
                modifier = Modifier.size(64.dp),
                cornerRadius = 9.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = series.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = series.metaLine,
                    color = Color(0xFF8E95A5),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x1F6366F1))
                    .border(1.dp, Color(0x336366F1), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${series.episodeCount}",
                    color = Color(0xFFA5B4FC),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
