package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.SortOption
import com.playme.player.core.model.VideoSubTab
import com.playme.player.core.model.ViewMode

@Composable
fun VideoSubNavBar(
    activeTab: VideoSubTab,
    sortOption: SortOption,
    viewMode: ViewMode,
    onTabChange: (VideoSubTab) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onViewModeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Videos / Folders Tab Buttons with Underline Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubNavTextTab(
                label = "Videos",
                isActive = activeTab == VideoSubTab.VIDEOS,
                onClick = { onTabChange(VideoSubTab.VIDEOS) }
            )
            SubNavTextTab(
                label = "Folders",
                isActive = activeTab == VideoSubTab.FOLDERS,
                onClick = { onTabChange(VideoSubTab.FOLDERS) }
            )
        }

        // Right: Sort & View Mode Controls (gap: 12px)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sort Dropdown Button
            Box {
                Row(
                    modifier = Modifier
                        .clickable { showSortMenu = true }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = sortOption.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Sort Options",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(15.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(Color(0xFF0F1220))
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (option == sortOption) Color(0xFF818CF8) else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortChange(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // View Mode Toggle (18px icon, #CBD5E1)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onViewModeToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
                    contentDescription = "Toggle Grid/List",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SubNavTextTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else Color(0xFF8E95A5),
            fontSize = 13.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
        )

        // Active Indicator Line: 2px bar #818CF8
        if (isActive) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(2.dp)
                    .fillMaxWidth(0.9f)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0x99818CF8)
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF818CF8))
            )
        } else {
            Box(modifier = Modifier.height(6.dp))
        }
    }
}
