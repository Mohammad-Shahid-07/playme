package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.playme.player.core.model.SeriesEpisode

@Composable
fun EpisodeRow(
    episode: SeriesEpisode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val video = episode.video

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x09FFFFFF))
            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 92.dp, height = 54.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF11131E))
        ) {
            VideoThumbnailImage(
                video = video,
                targetWidth = 200,
                targetHeight = 120,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xD9000000))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = video.durationFormatted,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (episode.label.isNotEmpty()) {
                Text(
                    text = episode.label,
                    color = Color(0xFFA5B4FC),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
            Text(
                text = episode.title.ifBlank { video.title },
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(video.fileSizeFormatted, video.resolution)
                    .filter { it.isNotBlank() }
                    .joinToString("  ·  "),
                color = Color(0xFF8E95A5),
                fontSize = 10.5.sp,
                maxLines = 1
            )
        }
    }
}
