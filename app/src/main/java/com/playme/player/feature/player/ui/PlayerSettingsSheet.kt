package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import com.playme.player.feature.player.model.SettingsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    isOpen: Boolean,
    onClose: () -> Unit,
    activeTab: SettingsTab,
    onTabSelected: (SettingsTab) -> Unit,
    audioDelayMs: Long,
    subtitleDelayMs: Long,
    decoderMode: DecoderMode,
    aspectRatioMode: AspectRatioMode,
    onAdjustAudioDelay: (Long) -> Unit,
    onAdjustSubtitleDelay: (Long) -> Unit,
    onToggleDecoderMode: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onResetDelays: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color(0xF50C0E1A),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Player Settings",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color(0xB3FFFFFF), modifier = Modifier.size(18.dp))
                }
            }

            // Tab Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsTabItem("Audio", activeTab == SettingsTab.AUDIO, onClick = { onTabSelected(SettingsTab.AUDIO) }, modifier = Modifier.weight(1f))
                SettingsTabItem("Subtitles", activeTab == SettingsTab.SUBTITLES, onClick = { onTabSelected(SettingsTab.SUBTITLES) }, modifier = Modifier.weight(1f))
                SettingsTabItem("Video/HW", activeTab == SettingsTab.VIDEO, onClick = { onTabSelected(SettingsTab.VIDEO) }, modifier = Modifier.weight(1f))
            }

            // Content according to tab
            when (activeTab) {
                SettingsTab.AUDIO -> {
                    DelayAdjusterRow(
                        label = "Audio Delay Sync",
                        delayMs = audioDelayMs,
                        onStep = onAdjustAudioDelay,
                        onReset = onResetDelays
                    )
                }
                SettingsTab.SUBTITLES -> {
                    DelayAdjusterRow(
                        label = "Subtitle Delay Sync",
                        delayMs = subtitleDelayMs,
                        onStep = onAdjustSubtitleDelay,
                        onReset = onResetDelays
                    )
                }
                SettingsTab.VIDEO, SettingsTab.PLAYBACK -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingToggleRow(
                            label = "Decoder Engine",
                            value = decoderMode.name,
                            description = if (decoderMode == DecoderMode.HW) "Hardware Accelerated (GPU/VPU)" else "Software CPU (FFmpeg Fallback)",
                            onClick = onToggleDecoderMode
                        )
                        SettingToggleRow(
                            label = "Aspect Ratio",
                            value = aspectRatioMode.label,
                            description = "Cycle scaling mode to eliminate black bars",
                            onClick = onCycleAspectRatio
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0x336366F1) else Color(0x0FFFFFFF))
            .border(1.dp, if (isActive) Color(0xFF818CF8) else Color(0x14FFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = if (isActive) Color.White else Color(0xFF8E95A5), fontSize = 11.5.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun DelayAdjusterRow(
    label: String,
    delayMs: Long,
    onStep: (Long) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0FFFFFFF))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Text(text = "Reset 0ms", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onReset))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0x1AFFFFFF)).clickable { onStep(-50L) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Remove, "-50ms", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Text(
                text = "${if (delayMs > 0) "+" else ""}${delayMs} ms",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0x1AFFFFFF)).clickable { onStep(50L) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, "+50ms", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    value: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0FFFFFFF))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
            Text(text = description, color = Color(0xFF8E95A5), fontSize = 10.5.sp)
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0x2E6366F1))
                .border(1.dp, Color(0x596366F1), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = value, color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
