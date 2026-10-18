package com.playme.player.feature.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeDesign
import com.playme.player.core.model.OpenSubtitleItem
import com.playme.player.core.player.PlayMePlayerController
import com.playme.player.core.player.PlayerUiState

@Composable
fun PlayerSettingsSidebar(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    activeSubView: String,
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onClose: () -> Unit,
    onScreenshotClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .animateEnterExit(
                        enter = slideInHorizontally(animationSpec = tween(280)) { it },
                        exit = slideOutHorizontally(animationSpec = tween(240)) { it }
                    )
                    .fillMaxHeight()
                    .width(340.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                    .background(Color(0xFF0C0E18).copy(alpha = 0.94f))
                    .border(
                        0.5.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks */ }
            ) {
                when (activeSubView) {
                    "main" -> SidebarMainView(
                        uiState = uiState,
                        controller = controller,
                        onClose = onClose,
                        onScreenshotClick = onScreenshotClick
                    )
                    "eq" -> SidebarEqualizerView(
                        uiState = uiState,
                        controller = controller,
                        onBack = { controller.setSidebarSubView("main") }
                    )
                    "timer" -> SidebarSleepTimerView(
                        uiState = uiState,
                        controller = controller,
                        onBack = { controller.setSidebarSubView("main") }
                    )
                    "audio" -> SidebarAudioTracksView(
                        uiState = uiState,
                        controller = controller,
                        onBack = { controller.setSidebarSubView("main") }
                    )
                    "subtitles" -> SidebarSubtitlesView(
                        uiState = uiState,
                        controller = controller,
                        onBack = { controller.setSidebarSubView("main") },
                        onOpenOnline = { controller.setSidebarSubView("online_subs") }
                    )
                    "online_subs" -> SidebarOnlineSubsView(
                        onBack = { controller.setSidebarSubView("subtitles") },
                        onApplySubtitle = { /* applied */ }
                    )
                    "wifi" -> SidebarWiFiShareView(
                        onBack = { controller.setSidebarSubView("main") }
                    )
                    "queue" -> SidebarQueueView(
                        uiState = uiState,
                        controller = controller,
                        onBack = { controller.setSidebarSubView("main") }
                    )
                    else -> SidebarMainView(
                        uiState = uiState,
                        controller = controller,
                        onClose = onClose,
                        onScreenshotClick = onScreenshotClick
                    )
                }
            }
        }
    }
}

// ==========================================
// SUBVIEW 1: MAIN MENU
// ==========================================

@Composable
private fun SidebarMainView(
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onClose: () -> Unit,
    onScreenshotClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PlayMeDesign.ElectricGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Play Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("PlayMe Pro Tools", color = Color(0xFFA1A1AA), fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // 12-Item Clean Unboxed Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionIcon(
                        icon = Icons.Default.Headphones,
                        label = "Audio",
                        isActive = uiState.currentAudioTrack != null,
                        onClick = { controller.setSidebarSubView("audio") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Equalizer,
                        label = "Equalizer",
                        isActive = uiState.equalizerEnabled,
                        onClick = { controller.setSidebarSubView("eq") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Subtitles,
                        label = "Subtitles",
                        isActive = uiState.currentSubtitle != null,
                        onClick = { controller.setSidebarSubView("subtitles") }
                    )
                    QuickActionIcon(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        label = "Queue",
                        isActive = uiState.playlist.isNotEmpty(),
                        onClick = { controller.setSidebarSubView("queue") }
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionIcon(
                        icon = Icons.Default.Repeat,
                        label = "AB Loop",
                        isActive = uiState.abRepeat.isEnabled,
                        onClick = { controller.toggleABRepeat() }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Bedtime,
                        label = "Timer",
                        isActive = uiState.sleepTimerConfig != null,
                        onClick = { controller.setSidebarSubView("timer") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.DarkMode,
                        label = "Night Mode",
                        isActive = uiState.isNightMode,
                        onClick = { controller.toggleNightMode() }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Flip,
                        label = "Mirror",
                        isActive = uiState.isMirrorMode,
                        onClick = { controller.toggleMirrorMode() }
                    )
                }

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionIcon(
                        icon = Icons.Default.ContentCut,
                        label = "Capture",
                        isActive = false,
                        onClick = onScreenshotClick
                    )
                    QuickActionIcon(
                        icon = Icons.Default.QueryStats,
                        label = "Nerd Stats",
                        isActive = uiState.isNerdStatsVisible,
                        onClick = { controller.toggleNerdStats() }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.WifiTethering,
                        label = "Wi-Fi Share",
                        isActive = false,
                        onClick = { controller.setSidebarSubView("wifi") }
                    )
                    QuickActionIcon(
                        icon = Icons.Default.Headphones,
                        label = "Background",
                        isActive = uiState.isBackgroundPlayEnabled,
                        onClick = { controller.toggleBackgroundPlay() }
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                "PLAY SETTINGS",
                color = Color(0xFF71717A),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Continuous Speed Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Playback Speed", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        "${String.format(java.util.Locale.US, "%.2f", uiState.playbackSpeed)}x",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = uiState.playbackSpeed,
                    onValueChange = { controller.setPlaybackSpeed(it) },
                    valueRange = 0.25f..3.0f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PlayMeDesign.ElectricViolet,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }
        }

        // Hardware Audio Boost (+200%) Switch
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Audio Boost (+200%)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Hardware loudness enhancer", color = Color(0xFF71717A), fontSize = 10.sp)
                    }
                }
                Switch(
                    checked = uiState.isQuickAudioBoostActive,
                    onCheckedChange = { controller.toggleQuickAudioBoost() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PlayMeDesign.ElectricViolet,
                        uncheckedThumbColor = Color(0xFFA1A1AA),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }
        }

        // Audio Sync Delay Shortcut
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { controller.setSidebarSubView("audio") }
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Audio Sync Delay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Fine-tune audio offset (±500ms)", color = Color(0xFF71717A), fontSize = 10.sp)
                    }
                }
                Text(
                    if (uiState.audioDelayMs == 0L) "0 ms" else "${if (uiState.audioDelayMs > 0) "+" else ""}${uiState.audioDelayMs} ms",
                    color = PlayMeDesign.BadgeIndigoText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Subtitle Sync Delay Shortcut
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { controller.setSidebarSubView("subtitles") }
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Subtitle Sync Delay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Fine-tune subtitle offset (±500ms)", color = Color(0xFF71717A), fontSize = 10.sp)
                    }
                }
                Text(
                    if (uiState.subtitleDelayMs == 0L) "0 ms" else "${if (uiState.subtitleDelayMs > 0) "+" else ""}${uiState.subtitleDelayMs} ms",
                    color = PlayMeDesign.BadgeIndigoText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// Helper quick action icon button (Clean unboxed rounded-full)
@Composable
private fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) PlayMeDesign.ElectricViolet.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.05f))
                .border(
                    1.dp,
                    if (isActive) PlayMeDesign.ElectricViolet.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color(0xFFA1A1AA),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            color = if (isActive) Color.White else Color(0xFFA1A1AA),
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================
// SUBVIEW 2: EQUALIZER & SOUND FX
// ==========================================

@Composable
private fun SidebarEqualizerView(
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubViewHeader(
                title = "Equalizer",
                onBack = onBack,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (uiState.equalizerEnabled) PlayMeDesign.ElectricViolet else Color.White.copy(alpha = 0.1f))
                            .clickable { controller.setEqualizerEnabled(!uiState.equalizerEnabled) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (uiState.equalizerEnabled) "Active" else "Bypassed",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

        // Preset Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRESETS", color = Color(0xFF71717A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf("Flat", "Rock", "Pop", "Dance", "Bass Boost", "Vocal")
                    items(presets) { preset ->
                        val isSelected = uiState.currentEqPreset == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) PlayMeDesign.ElectricViolet else Color.White.copy(alpha = 0.06f))
                                .clickable { controller.setEqualizerPreset(preset) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                preset,
                                color = if (isSelected) Color.White else Color(0xFFA1A1AA),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 5-Band Sliders Deck
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("+12dB", color = Color(0xFF71717A), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("5-Band EQ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("-12dB", color = Color(0xFF71717A), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }

                uiState.equalizerBands.forEach { band ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            band.formattedFreq,
                            color = Color(0xFFA1A1AA),
                            fontSize = 11.sp,
                            modifier = Modifier.width(42.dp),
                            fontFamily = FontFamily.Monospace
                        )
                        Slider(
                            value = band.dB.toFloat(),
                            onValueChange = { newVal ->
                                controller.setEqualizerBandLevel(band.index, (newVal * 100).toInt())
                            },
                            valueRange = -12f..12f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = PlayMeDesign.ElectricViolet,
                                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                            )
                        )
                        Text(
                            "${if (band.dB > 0) "+" else ""}${band.dB}dB",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(46.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Bass Boost Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bass Boost", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "${uiState.bassBoostStrength / 10}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = (uiState.bassBoostStrength / 10).toFloat(),
                    onValueChange = { controller.setBassBoost(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PlayMeDesign.ElectricViolet,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }
        }

        // 3D Virtualizer Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3D Virtualizer", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "${uiState.virtualizerStrength / 10}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = (uiState.virtualizerStrength / 10).toFloat(),
                    onValueChange = { controller.setVirtualizer(it.toInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PlayMeDesign.ElectricViolet,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

// ==========================================
// SUBVIEW 3: SLEEP TIMER
// ==========================================

@Composable
private fun SidebarSleepTimerView(
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onBack: () -> Unit
) {
    var finishActiveVideo by remember { mutableStateOf(uiState.sleepTimerConfig?.waitForMediaEnd ?: true) }
    var resetOnTouch by remember { mutableStateOf(uiState.sleepTimerConfig?.resetOnInteraction ?: true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubViewHeader(
                title = "Sleep Timer",
                onBack = onBack,
                trailingContent = {
                    val remaining = uiState.sleepTimerRemainingSec
                    if (remaining != null && remaining > 0) {
                        Text(
                            "${remaining / 60}m ${remaining % 60}s",
                            color = PlayMeDesign.BadgeIndigoText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else if (uiState.sleepTimerConfig?.minutes == -1) {
                        Text("End of media", color = PlayMeDesign.BadgeIndigoText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Off", color = Color(0xFF71717A), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // Presets Grid
        item {
            val presets = listOf(
                0 to "Off",
                15 to "15m",
                30 to "30m",
                45 to "45m",
                60 to "60m",
                -1 to "End"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TIMER PRESETS", color = Color(0xFF71717A), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.take(3).forEach { (minutes, label) ->
                        val isSelected = uiState.sleepTimerConfig?.minutes == minutes || (minutes == 0 && uiState.sleepTimerConfig == null)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) PlayMeDesign.ElectricViolet else Color.White.copy(alpha = 0.06f))
                                .clickable { controller.setSleepTimer(minutes, finishActiveVideo, resetOnTouch) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) Color.White else Color(0xFFA1A1AA),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.drop(3).forEach { (minutes, label) ->
                        val isSelected = uiState.sleepTimerConfig?.minutes == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) PlayMeDesign.ElectricViolet else Color.White.copy(alpha = 0.06f))
                                .clickable { controller.setSleepTimer(minutes, finishActiveVideo, resetOnTouch) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) Color.White else Color(0xFFA1A1AA),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // VLC Smart Switches
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Finish active video first", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Waits for current video to finish", color = Color(0xFF71717A), fontSize = 10.sp)
                    }
                    Checkbox(
                        checked = finishActiveVideo,
                        onCheckedChange = {
                            finishActiveVideo = it
                            uiState.sleepTimerConfig?.minutes?.let { m ->
                                controller.setSleepTimer(m, finishActiveVideo, resetOnTouch)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PlayMeDesign.ElectricViolet,
                            checkmarkColor = Color.White
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.05f)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reset on user interaction", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Extends countdown when screen is touched", color = Color(0xFF71717A), fontSize = 10.sp)
                    }
                    Checkbox(
                        checked = resetOnTouch,
                        onCheckedChange = {
                            resetOnTouch = it
                            uiState.sleepTimerConfig?.minutes?.let { m ->
                                controller.setSleepTimer(m, finishActiveVideo, resetOnTouch)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PlayMeDesign.ElectricViolet,
                            checkmarkColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// SUBVIEW 4: AUDIO TRACKS
// ==========================================

@Composable
private fun SidebarAudioTracksView(
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubViewHeader(title = "Audio Tracks", onBack = onBack)
        }

        // Available Tracks List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (uiState.availableAudioTracks.isEmpty()) {
                    Text(
                        "Default Stereo Track (AAC)",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    uiState.availableAudioTracks.forEachIndexed { index, track ->
                        val isSelected = track.isSelected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { controller.selectAudioTrack(track.id) }
                                .background(if (isSelected) Color.White.copy(alpha = 0.04f) else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(18.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        track.name,
                                        color = if (isSelected) Color.White else Color(0xFFA1A1AA),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        "${track.codec ?: "AAC"} • ${track.bitrate ?: "128 kbps"}",
                                        color = Color(0xFF71717A),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (track.isHardwareAccelerated) "HW" else "SW",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (index < uiState.availableAudioTracks.size - 1) {
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.04f)))
                        }
                    }
                }
            }
        }

        // Continuous Audio Delay Sync Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Audio Sync Delay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.audioDelayMs == 0L) "0 ms" else "${if (uiState.audioDelayMs > 0) "+" else ""}${uiState.audioDelayMs} ms",
                        color = PlayMeDesign.BadgeIndigoText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = uiState.audioDelayMs.toFloat(),
                    onValueChange = { controller.setAudioDelay(it.toLong()) },
                    valueRange = -500f..500f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PlayMeDesign.ElectricViolet,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { controller.setAudioDelay((uiState.audioDelayMs - 50L).coerceIn(-500L, 500L)) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-50ms", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (uiState.audioDelayMs == 0L) PlayMeDesign.ElectricViolet.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f))
                            .clickable { controller.setAudioDelay(0L) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reset", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { controller.setAudioDelay((uiState.audioDelayMs + 50L).coerceIn(-500L, 500L)) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+50ms", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Bluetooth Offset Quick Toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bluetooth Earphones Delay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("+180ms wireless sync offset", color = Color(0xFF71717A), fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (uiState.isBluetoothDelayActive) PlayMeDesign.ElectricViolet else Color.White.copy(alpha = 0.06f))
                        .clickable { controller.toggleBluetoothDelay() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (uiState.isBluetoothDelayActive) "Active" else "Off",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// SUBVIEW 5: SUBTITLES
// ==========================================

@Composable
private fun SidebarSubtitlesView(
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onBack: () -> Unit,
    onOpenOnline: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubViewHeader(title = "Subtitles", onBack = onBack)
        }

        // Download Online Action Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable { onOpenOnline() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Online (OpenSubtitles)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Subtitle Off & Available Tracks
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            ) {
                // Off option
                val isOff = uiState.currentSubtitle == null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { controller.selectSubtitleTrack(null) }
                        .background(if (isOff) Color.White.copy(alpha = 0.04f) else Color.Transparent)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Off", color = if (isOff) Color.White else Color(0xFFA1A1AA), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    if (isOff) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                    }
                }

                uiState.availableSubtitles.forEach { sub ->
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.04f)))
                    val isSelected = sub.isSelected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { controller.selectSubtitleTrack(sub.id) }
                            .background(if (isSelected) Color.White.copy(alpha = 0.04f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sub.name,
                            color = if (isSelected) Color.White else Color(0xFFA1A1AA),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Continuous Subtitle Delay Sync Slider
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtitle Sync Delay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (uiState.subtitleDelayMs == 0L) "0 ms" else "${if (uiState.subtitleDelayMs > 0) "+" else ""}${uiState.subtitleDelayMs} ms",
                        color = PlayMeDesign.BadgeIndigoText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = uiState.subtitleDelayMs.toFloat(),
                    onValueChange = { controller.setSubtitleDelay(it.toLong()) },
                    valueRange = -500f..500f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = PlayMeDesign.ElectricViolet,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { controller.setSubtitleDelay((uiState.subtitleDelayMs - 50L).coerceIn(-500L, 500L)) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-50ms", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (uiState.subtitleDelayMs == 0L) PlayMeDesign.ElectricViolet.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f))
                            .clickable { controller.setSubtitleDelay(0L) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reset", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable { controller.setSubtitleDelay((uiState.subtitleDelayMs + 50L).coerceIn(-500L, 500L)) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+50ms", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ==========================================
// SUBVIEW 6: OPENSUBTITLES SEARCH & DOWNLOAD
// ==========================================

@Composable
private fun SidebarOnlineSubsView(
    onBack: () -> Unit,
    onApplySubtitle: (OpenSubtitleItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("Avengers Infinity War") }

    val mockSubtitles = remember {
        listOf(
            OpenSubtitleItem("1", "Avengers.Infinity.War.1080p.BluRay.srt", "English", 99, ""),
            OpenSubtitleItem("2", "Avengers.Infinity.War.WEBRip.CC.srt", "English (CC)", 96, ""),
            OpenSubtitleItem("3", "Avengers.Infinity.War.Spanish.srt", "Spanish", 92, "")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubViewHeader(
                title = "OpenSubtitles",
                onBack = onBack,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33F59E0B))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("v2", color = Color(0xFFFCD34D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Search Pill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(searchQuery, color = Color.White, fontSize = 12.sp)
            }
        }

        // Subtitles List
        items(mockSubtitles) { item ->
            var downloaded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.fileName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${item.matchPercentage}% Match • ${item.language}",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (downloaded) Color(0x3334D399) else Color.White.copy(alpha = 0.12f))
                        .clickable {
                            downloaded = true
                            onApplySubtitle(item)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (downloaded) "Applied ✓" else "Download",
                        color = if (downloaded) Color(0xFF34D399) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// SUBVIEW 7: WI-FI SHARE / WEB TRANSFER
// ==========================================

@Composable
private fun SidebarWiFiShareView(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SubViewHeader(
            title = "Wi-Fi Share",
            onBack = onBack,
            trailingContent = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34D399))
                )
            }
        )

        // URL Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, PlayMeDesign.ElectricViolet.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Open on your PC browser:", color = Color(0xFFA1A1AA), fontSize = 11.sp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.60f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "http://192.168.1.15:8080",
                    color = PlayMeDesign.BadgeIndigoText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text("PIN: 4829 • Ready to Transfer", color = Color(0xFF71717A), fontSize = 10.sp)
        }

        // Features list
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiTethering, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Drag & drop videos from PC without cables", color = Color(0xFFA1A1AA), fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cast, contentDescription = null, tint = PlayMeDesign.ElectricViolet, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stream phone videos directly on computer", color = Color(0xFFA1A1AA), fontSize = 11.sp)
            }
        }
    }
}

// Reusable subview top navigation header
@Composable
private fun SubViewHeader(
    title: String,
    onBack: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

// ==========================================
// SUBVIEW 7: PLAYBACK QUEUE & REORDERING
// ==========================================

@Composable
private fun SidebarQueueView(
    uiState: PlayerUiState,
    controller: PlayMePlayerController,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SubViewHeader(
                title = "Playback Queue",
                onBack = onBack,
                trailingContent = {
                    Text(
                        "${uiState.playlist.size} videos",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            )
        }

        if (uiState.playlist.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No videos in queue",
                        color = Color(0xFF71717A),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            itemsIndexed(uiState.playlist) { index, item ->
                val isCurrent = index == uiState.currentPlaylistIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isCurrent) PlayMeDesign.ElectricViolet.copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.03f)
                        )
                        .border(
                            0.5.dp,
                            if (isCurrent) PlayMeDesign.ElectricViolet.copy(alpha = 0.6f)
                            else Color.White.copy(alpha = 0.04f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { controller.skipToQueueIndex(index) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Index badge or Playing icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .then(
                                if (isCurrent) Modifier.background(PlayMeDesign.ElectricGradient)
                                else Modifier.background(Color.White.copy(alpha = 0.08f))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("${index + 1}", color = Color(0xFFA1A1AA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.displayName,
                            color = if (isCurrent) Color.White else Color(0xFFE4E4E7),
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.formattedDuration.isNotBlank()) {
                            Text(
                                text = item.formattedDuration,
                                color = Color(0xFFA1A1AA),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Up / Down Reorder Arrows
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (index > 0) {
                            IconButton(
                                onClick = { controller.reorderPlaylist(index, index - 1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }
                        }
                        if (index < uiState.playlist.size - 1) {
                            IconButton(
                                onClick = { controller.reorderPlaylist(index, index + 1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
