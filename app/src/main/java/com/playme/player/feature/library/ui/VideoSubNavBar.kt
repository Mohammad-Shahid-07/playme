package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.CheckmarkIcon
import com.playme.player.core.designsystem.GridViewIcon
import com.playme.player.core.designsystem.ListViewIcon
import com.playme.player.core.designsystem.SortSlidersIcon
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
    var isSortMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    ) {
        // 1. Sub-Navigation Bar: Left Tabs & Right Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Videos / Folders Tab Buttons (gap: 16px)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SubNavTabButton(
                    label = "Videos",
                    isActive = activeTab == VideoSubTab.VIDEOS,
                    onClick = { onTabChange(VideoSubTab.VIDEOS) }
                )
                SubNavTabButton(
                    label = "Folders",
                    isActive = activeTab == VideoSubTab.FOLDERS,
                    onClick = { onTabChange(VideoSubTab.FOLDERS) }
                )
                SubNavTabButton(
                    label = "Series",
                    isActive = activeTab == VideoSubTab.SERIES,
                    onClick = { onTabChange(VideoSubTab.SERIES) }
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
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isSortMenuOpen = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SortSlidersIcon(color = Color(0xFFCBD5E1), size = 16.dp)
                        Text(
                            text = sortOptionLabel(sortOption),
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Obsidian Popover Dropdown matching VideoSubNav.css
                    DropdownMenu(
                        expanded = isSortMenuOpen,
                        onDismissRequest = { isSortMenuOpen = false },
                        modifier = Modifier
                            .background(Color(0xF00E111C))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                    ) {
                        SortOption.entries.forEach { option ->
                            val isSelected = option == sortOption
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sortOptionLabel(option),
                                            color = if (isSelected) Color(0xFF818CF8) else Color(0xFFCBD5E1),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            CheckmarkIcon(color = Color(0xFF818CF8), size = 14.dp)
                                        }
                                    }
                                },
                                onClick = {
                                    onSortChange(option)
                                    isSortMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Grid / List View Toggle Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onViewModeToggle),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewMode == ViewMode.LIST) {
                        GridViewIcon(color = Color(0xFFCBD5E1), size = 16.dp)
                    } else {
                        ListViewIcon(color = Color(0xFFCBD5E1), size = 16.dp)
                    }
                }
            }
        }

        // 2. Section Header: "TODAY" or "DEVICE FOLDERS" (9.5px, bold, uppercase, #64748B)
        Text(
            text = when (activeTab) {
                VideoSubTab.VIDEOS -> "TODAY"
                VideoSubTab.FOLDERS -> "DEVICE FOLDERS"
                VideoSubTab.SERIES -> "AUTO-GROUPED SERIES"
            },
            color = Color(0xFF64748B),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
        )
    }
}

@Composable
private fun SubNavTabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else Color(0xFF8E95A5),
            fontSize = 13.5.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(3.dp))
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF818CF8))
            )
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

private fun sortOptionLabel(option: SortOption): String {
    return when (option) {
        SortOption.NEWEST -> "Newest"
        SortOption.NAME -> "Name"
        SortOption.SIZE -> "Size"
        SortOption.DURATION -> "Duration"
    }
}
