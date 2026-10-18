package com.playme.player.feature.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeColors

enum class MainNavTab {
    VIDEO, MUSIC, BROWSE, SETTINGS
}

@Composable
fun BottomNavBar(
    activeTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    onBigPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Dock Chassis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(PlayMeColors.GlassElevated)
                .border(1.dp, PlayMeColors.GlassBorderHover, RoundedCornerShape(32.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 2 items: Video & Music
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTabItem(
                        icon = Icons.Rounded.Videocam,
                        label = "Video",
                        isSelected = activeTab == MainNavTab.VIDEO,
                        onClick = { onTabSelected(MainNavTab.VIDEO) }
                    )
                    NavTabItem(
                        icon = Icons.Rounded.MusicNote,
                        label = "Music",
                        isSelected = activeTab == MainNavTab.MUSIC,
                        onClick = { onTabSelected(MainNavTab.MUSIC) }
                    )
                }

                // Center Spacer for Protruding Play Button
                Box(modifier = Modifier.size(56.dp))

                // Right 2 items: Browse & Settings
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTabItem(
                        icon = Icons.Rounded.Folder,
                        label = "Browse",
                        isSelected = activeTab == MainNavTab.BROWSE,
                        onClick = { onTabSelected(MainNavTab.BROWSE) }
                    )
                    NavTabItem(
                        icon = Icons.Rounded.Settings,
                        label = "Settings",
                        isSelected = activeTab == MainNavTab.SETTINGS,
                        onClick = { onTabSelected(MainNavTab.SETTINGS) }
                    )
                }
            }
        }

        // Protruding 3D Big Play Button
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(58.dp)
                .shadow(12.dp, CircleShape, spotColor = PlayMeColors.ElectricViolet)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(PlayMeColors.ElectricViolet, PlayMeColors.DeepViolet)
                    )
                )
                .border(2.dp, PlayMeColors.LavenderThumb.copy(alpha = 0.8f), CircleShape)
                .clickable(onClick = onBigPlayClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Quick Play",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PlayMeColors.ElectricViolet else PlayMeColors.TextTertiary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isSelected) PlayMeColors.TextPrimary else PlayMeColors.TextTertiary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
