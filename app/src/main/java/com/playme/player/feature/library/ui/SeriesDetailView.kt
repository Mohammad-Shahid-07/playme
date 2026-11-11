package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
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
import com.playme.player.core.model.SeriesEpisode
import com.playme.player.core.model.SeriesItem

@Composable
fun SeriesDetailView(
    series: SeriesItem,
    onBack: () -> Unit,
    onEpisodeClick: (SeriesEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(9.dp))
                .clickable(onClick = onBack)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back to series",
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(17.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = series.title,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(series.metaLine)
                        series.year?.let { append("  ·  ").append(it) }
                    },
                    color = Color(0xFF8E95A5),
                    fontSize = 11.sp
                )
            }

            if (series.seasons.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    series.seasons.take(4).forEach { season ->
                        SeasonChip(label = "S%02d".format(season))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(items = series.episodes, key = { it.video.id }) { episode ->
                EpisodeRow(
                    episode = episode,
                    onClick = { onEpisodeClick(episode) }
                )
            }
        }
    }
}

@Composable
private fun SeasonChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x1F6366F1))
            .border(1.dp, Color(0x336366F1), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFA5B4FC),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
