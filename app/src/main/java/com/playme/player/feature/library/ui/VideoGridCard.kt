package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.VideoItem

@Composable
fun VideoGridCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10.5f)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF11131E))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
    ) {
        VideoThumbnailImage(
            video = video,
            targetWidth = 360,
            targetHeight = 240,
            modifier = Modifier.fillMaxSize()
        )

        // Top Badges (Spec on left, NEW on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            video.badges.firstOrNull()?.let { badge ->
                VideoBadgePill(badge = badge, isOverlay = true)
            } ?: Spacer(modifier = Modifier.size(1.dp))

            if (video.isNew) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                            )
                        )
                        .padding(horizontal = 4.5.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = Color(0xFF0C0E18),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 9.sp
                    )
                }
            }
        }

        // Bottom Scrim Overlay: 180deg fade to dark obsidian
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x000C0E18),
                            Color(0xB80C0E18),
                            Color(0xF50C0E18)
                        )
                    )
                )
                .padding(start = 8.dp, end = 8.dp, top = 26.dp, bottom = 6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val meta = if (video.year.isNotEmpty()) "${video.year} • ${video.fileSizeFormatted}" else video.fileSizeFormatted
                    Text(
                        text = meta,
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xBF000000))
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
            }
        }

        // Watch Progress Track at Bottom
        if (video.watchProgressPercent > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0x99000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (video.watchProgressPercent / 100f).coerceIn(0f, 1f))
                        .height(2.5.dp)
                        .background(Color(0xFF3B82F6))
                )
            }
        }
    }
}
