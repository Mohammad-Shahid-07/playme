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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.BrowseNavIcon
import com.playme.player.core.designsystem.MusicNavIcon
import com.playme.player.core.designsystem.SettingsNavIcon
import com.playme.player.core.designsystem.VideoNavIcon

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
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Capsule Dock: 60px height, rgba(10, 13, 24, 0.75), 1px solid rgba(255, 255, 255, 0.1)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    spotColor = Color(0xCC000000),
                    ambientColor = Color(0x206366F1)
                )
                .clip(CircleShape)
                .background(Color(0xBF0A0D18))
                .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Video
                NavTabItem(
                    label = "Video",
                    isActive = activeTab == MainNavTab.VIDEO,
                    icon = { VideoNavIcon(active = activeTab == MainNavTab.VIDEO, size = 21.dp) },
                    onClick = { onTabSelected(MainNavTab.VIDEO) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Music
                NavTabItem(
                    label = "Music",
                    isActive = activeTab == MainNavTab.MUSIC,
                    icon = { MusicNavIcon(active = activeTab == MainNavTab.MUSIC, size = 21.dp) },
                    onClick = { onTabSelected(MainNavTab.MUSIC) },
                    modifier = Modifier.weight(1f)
                )

                // Center Slot Spacer (flex: 1.1)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(60.dp)
                )

                // Tab 3: Browse
                NavTabItem(
                    label = "Browse",
                    isActive = activeTab == MainNavTab.BROWSE,
                    icon = { BrowseNavIcon(active = activeTab == MainNavTab.BROWSE, size = 21.dp) },
                    onClick = { onTabSelected(MainNavTab.BROWSE) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: Settings
                NavTabItem(
                    label = "Settings",
                    isActive = activeTab == MainNavTab.SETTINGS,
                    icon = { SettingsNavIcon(active = activeTab == MainNavTab.SETTINGS, size = 21.dp) },
                    onClick = { onTabSelected(MainNavTab.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Center Protruding Big Play Button (50px x 50px, top: -9px)
        Box(
            modifier = Modifier
                .offset(y = (-9).dp)
                .size(50.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0x806366F1)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                    )
                )
                .clickable(onClick = onBigPlayClick)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F111E))
                    .border(1.dp, Color(0x14FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    isActive: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF38BDF8) // Clean cyan matching Image 1 prototype
    val inactiveColor = Color(0x80FFFFFF)

    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = label,
            color = if (isActive) activeColor else inactiveColor,
            fontSize = 10.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = (-0.01).sp,
            lineHeight = 10.5.sp
        )
    }
}
