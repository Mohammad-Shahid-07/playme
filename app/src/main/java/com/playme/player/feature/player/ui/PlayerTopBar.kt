package com.playme.player.feature.player.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.feature.player.model.SettingsTab

@Composable
fun PlayerTopBar(
    title: String,
    resolution: String,
    fileSize: String,
    format: String,
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    onOpenSettings: (SettingsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xEB000000),
                        Color(0x73000000),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back button + Title & metadata
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // Back Button: 34px circular glass button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x80000000))
                        .clip(CircleShape)
                        .background(Color(0xD10E111D))
                        .border(1.dp, Color(0x17FFFFFF), CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Title and Meta column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.01).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = resolution,
                            color = Color(0xFFA5B4FC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "·", color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text(text = fileSize, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                        Text(text = "·", color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text(text = format, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                    }
                }
            }

            // Right: Liquid Glass Top Utility Capsule (Queue, Subtitles, Audio, Settings)
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xD10E111D))
                    .border(1.dp, Color(0x17FFFFFF), CircleShape)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TopUtilityButton(
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        onClick = onQueueClick
                    )
                    TopUtilityButton(
                        icon = Icons.Rounded.ClosedCaption,
                        contentDescription = "Subtitles",
                        onClick = { onOpenSettings(SettingsTab.SUBTITLES) }
                    )
                    TopUtilityButton(
                        icon = Icons.Rounded.Headphones,
                        contentDescription = "Audio",
                        onClick = { onOpenSettings(SettingsTab.AUDIO) }
                    )
                    TopUtilityButton(
                        icon = Icons.Rounded.Tune,
                        contentDescription = "Settings",
                        onClick = { onOpenSettings(SettingsTab.PLAYBACK) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopUtilityButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xE0FFFFFF),
            modifier = Modifier.size(17.dp)
        )
    }
}
