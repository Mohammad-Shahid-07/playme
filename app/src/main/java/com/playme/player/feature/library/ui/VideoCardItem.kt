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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.playme.player.core.model.BadgeVariant
import com.playme.player.core.model.VideoBadge
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
        VideoGridCard(video = video, onClick = onClick, modifier = modifier)
    } else {
        VideoListCard(video = video, onClick = onClick, onMoreClick = onMoreClick, modifier = modifier)
    }
}

/**
 * Pixel-perfect List Card matching VideoCard.css lines 5 to 235
 */
@Composable
private fun VideoListCard(
    video: VideoItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Left Thumbnail: 142px x 72px, radius 9px, #11131E with 1px border rgba(255, 255, 255, 0.12)
        Box(
            modifier = Modifier
                .size(width = 142.dp, height = 72.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF11131E))
                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(9.dp))
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
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
                        letterSpacing = 0.05.sp,
                        lineHeight = 9.sp
                    )
                }
            }

            // Bottom-Right Duration Badge: rgba(0, 0, 0, 0.85), 9.5px font
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

            // Watch Progress Bar: 2.5px
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

        // Middle Info: Exact 72px height matching thumbnail
        Column(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .padding(top = 3.5.dp, bottom = 4.5.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title + Metadata Group
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

                // Metadata: Year • FileSize • Format
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
            }

            // Spec Badges Row: 16px height, 9px font, transparent background
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                video.badges.take(4).forEach { badge ->
                    CompactSpecBadge(badge = badge)
                }
            }
        }

        // Right Options Button: 72px vertically centering 3 dots
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
                tint = Color(0x61FFFFFF), // rgba(255, 255, 255, 0.38)
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Pixel-perfect 2-Column Grid Card matching VideoCard.css lines 240 to 375
 */
@Composable
private fun VideoGridCard(
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
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            video.badges.firstOrNull()?.let { badge ->
                Box(
                    modifier = Modifier
                        .height(15.dp)
                        .clip(RoundedCornerShape(3.5.dp))
                        .background(Color(0xB3000000))
                        .border(1.dp, getBadgeBorderColor(badge.variant), RoundedCornerShape(3.5.dp))
                        .padding(horizontal = 4.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.label,
                        color = getBadgeTextColor(badge.variant),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 10.sp
                    )
                }
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

        // Bottom Scrim Overlay: linear-gradient(180deg, rgba(12,14,24,0) 0%, rgba(12,14,24,0.72) 48%, rgba(12,14,24,0.96) 100%)
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
                    Text(
                        text = video.fileSizeFormatted,
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

        // Bottom Progress Track (2.5px, #3B82F6)
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

/**
 * Compact 16px spec badge with 1px border and transparent background
 */
@Composable
private fun CompactSpecBadge(badge: VideoBadge) {
    Box(
        modifier = Modifier
            .height(16.dp)
            .clip(RoundedCornerShape(3.5.dp))
            .border(1.dp, getBadgeBorderColor(badge.variant), RoundedCornerShape(3.5.dp))
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badge.label,
            color = getBadgeTextColor(badge.variant),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 10.sp
        )
    }
}

private fun getBadgeBorderColor(variant: BadgeVariant): Color = when (variant) {
    BadgeVariant.GOLD -> Color(0xFFD97706) // #d97706
    BadgeVariant.BLUE -> Color(0x666366F1) // rgba(99, 102, 241, 0.4)
    BadgeVariant.GRAY -> Color(0x38FFFFFF) // rgba(255, 255, 255, 0.22)
    BadgeVariant.GREEN -> Color(0xB310B981) // rgba(16, 185, 129, 0.7)
}

private fun getBadgeTextColor(variant: BadgeVariant): Color = when (variant) {
    BadgeVariant.GOLD -> Color(0xFFFACC15) // #facc15
    BadgeVariant.BLUE -> Color(0xFFA5B4FC) // #a5b4fc
    BadgeVariant.GRAY -> Color(0xB8FFFFFF) // rgba(255, 255, 255, 0.72)
    BadgeVariant.GREEN -> Color(0xFF34D399) // #34d399
}
