package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.feature.player.model.SettingsTab
import com.playme.player.feature.player.model.VideoScaleMode

@Composable
fun PlayerSettingsSidebar(
    isOpen: Boolean,
    activeTab: SettingsTab,
    scaleMode: VideoScaleMode,
    onTabSelected: (SettingsTab) -> Unit,
    onScaleModeSelected: (VideoScaleMode) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogueBoost by remember { mutableStateOf(true) }
    var volumeBoost by remember { mutableFloatStateOf(100f) }
    var ambientGlow by remember { mutableStateOf(true) }
    var backgroundPlay by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .background(PlayMeColors.GlassElevated)
                .border(1.dp, PlayMeColors.GlassBorderHover)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = PlayMeColors.ElectricViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Quick Settings",
                        color = PlayMeColors.TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Settings",
                        tint = PlayMeColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PlayMeColors.GlassSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SettingsTab.entries.forEach { tab ->
                    val isSelected = (tab == activeTab)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) PlayMeColors.ElectricViolet else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) PlayMeColors.TextPrimary else PlayMeColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeTab) {
                    SettingsTab.AUDIO -> {
                        SettingsSectionTitle(title = "Audio Track", icon = Icons.Rounded.GraphicEq)
                        SelectableCard(label = "English [5.1 Surround (E-AC-3)]", selected = true, onSelect = {})
                        SelectableCard(label = "Japanese [Original Audio]", selected = false, onSelect = {})

                        Spacer(modifier = Modifier.height(8.dp))
                        ToggleRow(
                            label = "Dialogue Clarity Boost",
                            subtitle = "Enhance vocal frequencies",
                            checked = dialogueBoost,
                            onCheckedChange = { dialogueBoost = it }
                        )

                        Text(
                            text = "Volume Boost (${volumeBoost.toInt()}%)",
                            color = PlayMeColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = volumeBoost,
                            onValueChange = { volumeBoost = it },
                            valueRange = 100f..200f,
                            colors = SliderDefaults.colors(
                                thumbColor = PlayMeColors.LavenderThumb,
                                activeTrackColor = PlayMeColors.ElectricViolet
                            )
                        )
                    }

                    SettingsTab.SUBTITLES -> {
                        SettingsSectionTitle(title = "Subtitle Stream", icon = Icons.Rounded.Subtitles)
                        SelectableCard(label = "English [CC - Full]", selected = true, onSelect = {})
                        SelectableCard(label = "Spanish [Sub]", selected = false, onSelect = {})
                        SelectableCard(label = "None (Off)", selected = false, onSelect = {})
                    }

                    SettingsTab.DISPLAY -> {
                        SettingsSectionTitle(title = "Aspect Ratio", icon = Icons.Rounded.AspectRatio)
                        VideoScaleMode.entries.forEach { mode ->
                            SelectableCard(
                                label = mode.name.replace("_", " "),
                                selected = mode == scaleMode,
                                onSelect = { onScaleModeSelected(mode) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        ToggleRow(
                            label = "Ambient Edge Glow",
                            subtitle = "Dynamic violet bloom behind player",
                            checked = ambientGlow,
                            onCheckedChange = { ambientGlow = it }
                        )
                    }

                    SettingsTab.PLAYBACK -> {
                        SettingsSectionTitle(title = "Playback Engine", icon = Icons.Rounded.PlayCircle)
                        SelectableCard(label = "Hardware Decoder (HW+)", selected = true, onSelect = {})
                        SelectableCard(label = "Software Decoder (SW)", selected = false, onSelect = {})

                        Spacer(modifier = Modifier.height(8.dp))
                        ToggleRow(
                            label = "Background Audio Playback",
                            subtitle = "Continue audio when screen turns off",
                            checked = backgroundPlay,
                            onCheckedChange = { backgroundPlay = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PlayMeColors.LavenderText,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            color = PlayMeColors.LavenderText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SelectableCard(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) PlayMeColors.ElectricViolet.copy(alpha = 0.15f)
                else PlayMeColors.GlassSurface
            )
            .border(
                width = 1.dp,
                color = if (selected) PlayMeColors.ElectricViolet else PlayMeColors.GlassBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (selected) PlayMeColors.TextPrimary else PlayMeColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = PlayMeColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = PlayMeColors.TextTertiary,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PlayMeColors.LavenderThumb,
                checkedTrackColor = PlayMeColors.ElectricViolet
            )
        )
    }
}
