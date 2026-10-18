package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.playme.player.core.model.VideoItem

@Composable
fun VideoListCard(
    video: VideoItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(video.thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(video.thumbnailUrl)
            .size(Size(320, 180))
            .memoryCacheKey(video.id)
            .diskCacheKey(video.id)
            .crossfade(true)
            .build()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Left Thumbnail: 142px x 72px matching VideoCard.css
        Box(
            modifier = Modifier
                .size(width = 142.dp, height = 72.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF11131E))
                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(9.dp))
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top-Right NEW Badge
            if (video.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
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

            // Bottom-Right Duration Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xD9000000))
                    .padding(horizontal = 4.5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = video.durationFormatted,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 11.sp
                )
            }

            // Watch Progress Bar
            if (video.watchProgressPercent > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(Color(0x80000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (video.watchProgressPercent / 100f).coerceIn(0f, 1f))
                            .height(2.5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                                )
                            )
                    )
                }
            }
        }

        // Middle Info: 72px height
        Column(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .padding(top = 3.5.dp, bottom = 4.5.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                val metaText = listOfNotNull(
                    video.year.ifEmpty { null },
                    video.fileSizeFormatted.ifEmpty { null },
                    video.format.ifEmpty { null }
                ).joinToString(" • ")

                Text(
                    text = metaText,
                    color = Color(0xFF8E95A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }

            // Spec Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                video.badges.take(4).forEach { badge ->
                    VideoBadgePill(badge = badge, isOverlay = false)
                }
            }
        }

        // Right Options Button
        Box(
            modifier = Modifier
                .height(72.dp)
                .width(24.dp)
                .clickable(onClick = onMoreClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = Color(0x61FFFFFF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
