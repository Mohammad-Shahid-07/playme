package com.playme.player.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.playme.player.core.designsystem.PlayMeDesign
import com.playme.player.core.designsystem.darkGlass
import com.playme.player.core.designsystem.darkGlassCard
import com.playme.player.core.model.VideoFolder
import com.playme.player.core.settings.SettingsManager
import com.playme.player.feature.library.LibraryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: LibraryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var editingFolderSpeedFor by remember { mutableStateOf<VideoFolder?>(null) }
    var showAudioLangDialog by remember { mutableStateOf(false) }
    var showSubtitleLangDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PlayMeDesign.ObsidianDark),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- HEADER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .darkGlass(shape = CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Playback, Languages & Organization",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // --- SECTION 1: GLOBAL PLAYBACK SPEED ---
        item {
            SettingsSectionHeader(icon = Icons.Default.Speed, title = "Playback Speed")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .darkGlassCard(shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Global Default Speed",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.globalSpeed}x",
                            color = PlayMeDesign.ElectricViolet,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsManager.AVAILABLE_SPEEDS.forEach { speed ->
                            val isSelected = (uiState.globalSpeed == speed)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PlayMeDesign.ElectricViolet else Color(0xFF1E2030))
                                    .border(
                                        1.dp,
                                        if (isSelected) PlayMeDesign.ElectricViolet else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setGlobalPlaybackSpeed(speed) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                                    color = if (isSelected) Color.White else Color(0xFFD4D4D8),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: CUSTOM SPEED FOR EACH FOLDER ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SettingsSectionHeader(icon = Icons.Default.Folder, title = "Custom Speed for Each Folder")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .darkGlassCard(shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Assign a dedicated playback speed to specific folders (e.g. Lectures at 1.5x, Movies at 1.0x)",
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.folders.isEmpty()) {
                        Text("No folders found", color = Color(0xFF71717A), fontSize = 12.sp)
                    } else {
                        uiState.folders.forEach { folder ->
                            val currentFolderSpeed = uiState.folderSpeeds[folder.path]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF141624))
                                    .clickable { editingFolderSpeedFor = folder }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${folder.videoCount} videos",
                                        color = Color(0xFF71717A),
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (currentFolderSpeed != null) PlayMeDesign.ElectricViolet.copy(alpha = 0.2f) else Color(0xFF27272A))
                                        .border(
                                            0.8.dp,
                                            if (currentFolderSpeed != null) PlayMeDesign.ElectricViolet else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (currentFolderSpeed != null) "${currentFolderSpeed}x" else "Default (${uiState.globalSpeed}x)",
                                        color = if (currentFolderSpeed != null) PlayMeDesign.BadgeIndigoText else Color(0xFFA1A1AA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: AUDIO & SUBTITLES LANGUAGE PREFERENCES ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SettingsSectionHeader(icon = Icons.Default.Audiotrack, title = "Preferred Audio & Subtitles")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .darkGlassCard(shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Audio Language Preference Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAudioLangDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Audio Language",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Auto-select matching track (English, Hindi, etc.)",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.5.sp
                            )
                        }

                        val activeAudioLabel = SettingsManager.AUDIO_LANGUAGES.firstOrNull { it.first == uiState.preferredAudioLang }?.second ?: "Default"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PlayMeDesign.BadgeIndigoBg)
                                .border(0.8.dp, PlayMeDesign.ElectricViolet, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeAudioLabel,
                                color = PlayMeDesign.BadgeIndigoText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color.White.copy(alpha = 0.08f)))

                    // Subtitle Language Preference Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSubtitleLangDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Subtitle Language",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Auto-enable matching subtitle track",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.5.sp
                            )
                        }

                        val activeSubLabel = SettingsManager.SUBTITLE_LANGUAGES.firstOrNull { it.first == uiState.preferredSubtitleLang }?.second ?: "Off"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PlayMeDesign.BadgeVioletBg)
                                .border(0.8.dp, PlayMeDesign.Ultraviolet, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeSubLabel,
                                color = PlayMeDesign.BadgeVioletText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 4: LIBRARY & MEDIA SCANNING ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SettingsSectionHeader(icon = Icons.Default.VideoLibrary, title = "Library & Media Management")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .darkGlassCard(shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Find all media / Rescan Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Find All Media",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Perform a complete rescan of device storage and folders",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PlayMeDesign.ElectricViolet)
                                .clickable(enabled = !uiState.isScanning) { viewModel.rescanAllMedia() }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("Rescan", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Scan notification message
                    if (uiState.scanMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = uiState.scanMessage!!,
                                color = Color(0xFF34D399),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color.White.copy(alpha = 0.08f)))

                    // Automatically group videos by similar name toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Group Videos by Similar Name",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Automatically group series, anime, and episodes into collections",
                                color = Color(0xFFA1A1AA),
                                fontSize = 11.5.sp
                            )
                        }

                        Switch(
                            checked = uiState.autoGroupSimilarVideos,
                            onCheckedChange = { viewModel.toggleAutoGrouping() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PlayMeDesign.ElectricViolet,
                                uncheckedThumbColor = Color(0xFF71717A),
                                uncheckedTrackColor = Color(0xFF27272A)
                            )
                        )
                    }
                }
            }
        }

        // --- SECTION 5: APP INFO ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SettingsSectionHeader(icon = Icons.Default.Info, title = "About")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .darkGlassCard(shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PlayMe Media Player",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0.0 (Obsidian Violet Edition)",
                        color = PlayMeDesign.BadgeIndigoText,
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = "Media3 ExoPlayer Engine · Hardware Acceleration Enabled",
                        color = Color(0xFF71717A),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // --- DIALOG: FOLDER SPEED SELECTOR ---
    if (editingFolderSpeedFor != null) {
        val folder = editingFolderSpeedFor!!
        val currentSpeed = uiState.folderSpeeds[folder.path]
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editingFolderSpeedFor = null },
            containerColor = Color(0xFF141727),
            title = {
                Text(
                    text = "Speed for: ${folder.name}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select playback speed for all videos inside this folder:",
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp
                    )

                    // Default (Global) option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentSpeed == null) PlayMeDesign.ElectricViolet.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable {
                                viewModel.setFolderPlaybackSpeed(folder.path, null)
                                editingFolderSpeedFor = null
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use Global Default (${uiState.globalSpeed}x)",
                            color = if (currentSpeed == null) Color.White else Color(0xFFD4D4D8),
                            fontSize = 13.sp,
                            fontWeight = if (currentSpeed == null) FontWeight.Bold else FontWeight.Normal
                        )
                        if (currentSpeed == null) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Available speeds
                    SettingsManager.AVAILABLE_SPEEDS.forEach { spd ->
                        val isSel = (currentSpeed == spd)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PlayMeDesign.ElectricViolet.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable {
                                    viewModel.setFolderPlaybackSpeed(folder.path, spd)
                                    editingFolderSpeedFor = null
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${spd}x",
                                color = if (isSel) Color.White else Color(0xFFD4D4D8),
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // --- DIALOG: AUDIO LANGUAGE SELECTOR ---
    if (showAudioLangDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAudioLangDialog = false },
            containerColor = Color(0xFF141727),
            title = {
                Text("Default Audio Language", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsManager.AUDIO_LANGUAGES.forEach { (code, label) ->
                        val isSel = (uiState.preferredAudioLang == code)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PlayMeDesign.ElectricViolet.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable {
                                    viewModel.setPreferredAudio(code)
                                    showAudioLangDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color(0xFFD4D4D8),
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // --- DIALOG: SUBTITLE LANGUAGE SELECTOR ---
    if (showSubtitleLangDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSubtitleLangDialog = false },
            containerColor = Color(0xFF141727),
            title = {
                Text("Default Subtitle Language", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsManager.SUBTITLE_LANGUAGES.forEach { (code, label) ->
                        val isSel = (uiState.preferredSubtitleLang == code)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PlayMeDesign.Ultraviolet.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable {
                                    viewModel.setPreferredSubtitle(code)
                                    showSubtitleLangDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color(0xFFD4D4D8),
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PlayMeDesign.Ultraviolet, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PlayMeDesign.ElectricViolet,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title.uppercase(),
            color = PlayMeDesign.BadgeIndigoText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}
