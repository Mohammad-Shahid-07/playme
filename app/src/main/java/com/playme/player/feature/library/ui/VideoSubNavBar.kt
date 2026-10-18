package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.playme.player.core.designsystem.PlayMeColors
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
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Videos vs Folders Segmented Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(PlayMeColors.GlassSurface)
                .border(1.dp, PlayMeColors.GlassBorder, RoundedCornerShape(20.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubTabPill(
                label = "Videos",
                isSelected = activeTab == VideoSubTab.VIDEOS,
                onClick = { onTabChange(VideoSubTab.VIDEOS) }
            )
            SubTabPill(
                label = "Folders",
                isSelected = activeTab == VideoSubTab.FOLDERS,
                onClick = { onTabChange(VideoSubTab.FOLDERS) }
            )
        }

        // Actions: Sort & View Mode
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sort Dropdown Anchor
            Box {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PlayMeColors.GlassSurface)
                        .border(1.dp, PlayMeColors.GlassBorder, CircleShape)
                        .clickable { showSortMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                        contentDescription = "Sort",
                        tint = PlayMeColors.LavenderText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = sortOption.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = PlayMeColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(PlayMeColors.GlassElevated)
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (option == sortOption) PlayMeColors.ElectricViolet else PlayMeColors.TextPrimary,
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

            // List vs Grid View Toggle
            IconButton(
                onClick = onViewModeToggle,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PlayMeColors.GlassSurface)
                    .border(1.dp, PlayMeColors.GlassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.LIST) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
                    contentDescription = "Toggle View Mode",
                    tint = PlayMeColors.LavenderText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SubTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) PlayMeColors.ElectricViolet else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) PlayMeColors.TextPrimary else PlayMeColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
