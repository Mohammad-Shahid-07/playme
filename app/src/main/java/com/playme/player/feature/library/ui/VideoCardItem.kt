package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.playme.player.core.designsystem.PlayMeColors
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
            .clip(RoundedCornerShape(12.dp))
            .background(PlayMeColors.GlassCard)
            .border(1.dp, PlayMeColors.GlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail Box
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(66.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PlayMeColors.ObsidianChassis)
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Duration Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PlayMeColors.ObsidianDark.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.durationFormatted,
                    color = PlayMeColors.TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = PlayMeColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: folder and file size
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = video.parentFolder,
                    color = PlayMeColors.TextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "•", color = PlayMeColors.TextTertiary, fontSize = 11.sp)
                Text(
                    text = video.fileSizeFormatted,
                    color = PlayMeColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Spec Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                video.badges.take(3).forEach { badge ->
                    SpecBadgePill(badge = badge)
                }
            }
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = PlayMeColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun VideoGridCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PlayMeColors.GlassCard)
            .border(1.dp, PlayMeColors.GlassBorder, RoundedCornerShape(14.dp))
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
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            video.badges.firstOrNull()?.let { badge ->
                SpecBadgePill(badge = badge)
            } ?: Box(modifier = Modifier.size(1.dp))

            if (video.isNew) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PlayMeColors.ElectricViolet)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "NEW", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Gradient Overlay with Title & Duration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, PlayMeColors.ObsidianDark.copy(alpha = 0.95f))
                    )
                )
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = video.title,
                    color = PlayMeColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = video.fileSizeFormatted,
                        color = PlayMeColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = video.durationFormatted,
                        color = PlayMeColors.LavenderText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecBadgePill(badge: VideoBadge) {
    val bgColor = when (badge.variant) {
        BadgeVariant.GOLD -> PlayMeColors.AmberBoost.copy(alpha = 0.15f)
        BadgeVariant.BLUE -> PlayMeColors.ElectricViolet.copy(alpha = 0.15f)
        BadgeVariant.GREEN -> PlayMeColors.EmeraldActive.copy(alpha = 0.15f)
        BadgeVariant.GRAY -> PlayMeColors.GlassSurface
    }
    val textColor = when (badge.variant) {
        BadgeVariant.GOLD -> PlayMeColors.AmberBoost
        BadgeVariant.BLUE -> PlayMeColors.ElectricViolet
        BadgeVariant.GREEN -> PlayMeColors.EmeraldActive
        BadgeVariant.GRAY -> PlayMeColors.TextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = badge.label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
