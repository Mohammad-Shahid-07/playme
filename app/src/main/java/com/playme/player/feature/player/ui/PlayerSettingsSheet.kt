package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    isOpen: Boolean,
    onClose: () -> Unit,
    audioDelayMs: Long,
    subtitleDelayMs: Long,
    decoderMode: DecoderMode,
    aspectRatioMode: AspectRatioMode,
    isDialogueClarity: Boolean,
    isAmbientGlow: Boolean,
    sleepTimerMinutes: Int,
    backgroundPlay: Boolean,
    playbackSpeed: Float,
    selectedAudio: String,
    selectedSubtitle: String,
    onAdjustAudioDelay: (Long) -> Unit,
    onAdjustSubtitleDelay: (Long) -> Unit,
    onResetDelays: () -> Unit,
    onToggleDecoderMode: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onToggleDialogueClarity: () -> Unit,
    onToggleAmbientGlow: () -> Unit,
    onToggleBackgroundPlay: () -> Unit,
    onOpenSpeedPicker: () -> Unit,
    onCycleSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color(0xF20B0D18),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Tune, "Settings", tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                    Text("Player Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color(0x99FFFFFF), modifier = Modifier.size(18.dp))
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. AUDIO GROUP
                SettingsGroupCard(title = "AUDIO") {
                    SettingsActionRow(Icons.Rounded.Headphones, "Audio Track", selectedAudio, onClick = { /* Switch track */ })
                    DelayAdjusterRow("Audio Sync Delay", audioDelayMs, onStep = onAdjustAudioDelay, onReset = onResetDelays)
                    SettingsSwitchRow(Icons.Rounded.GraphicEq, "Dialogue Clarity", isDialogueClarity, onCheckedChange = { onToggleDialogueClarity() })
                }

                // 2. SUBTITLES GROUP
                SettingsGroupCard(title = "SUBTITLES") {
                    SettingsActionRow(Icons.Rounded.Subtitles, "Subtitle Track", selectedSubtitle, onClick = { /* Switch sub */ })
                    DelayAdjusterRow("Subtitle Delay Sync", subtitleDelayMs, onStep = onAdjustSubtitleDelay, onReset = onResetDelays)
                }

                // 3. PLAYBACK & ENGINE GROUP
                SettingsGroupCard(title = "PLAYBACK & ENGINE") {
                    SettingsActionRow(Icons.Rounded.Speed, "Playback Speed", "${playbackSpeed}x", onClick = onOpenSpeedPicker)
                    SettingsActionRow(
                        Icons.Rounded.Memory,
                        "Decoder Engine",
                        if (decoderMode == DecoderMode.HW) "HW (Hardware)" else "SW (Software)",
                        onClick = onToggleDecoderMode
                    )
                    SettingsActionRow(
                        Icons.Rounded.Nightlight,
                        "Sleep Timer",
                        if (sleepTimerMinutes == 0) "Off" else "${sleepTimerMinutes}m",
                        onClick = onCycleSleepTimer
                    )
                    SettingsSwitchRow(Icons.Rounded.Headphones, "Background Play", backgroundPlay, onCheckedChange = { onToggleBackgroundPlay() })
                }

                // 4. DISPLAY & AMBIENT GROUP
                SettingsGroupCard(title = "DISPLAY & AMBIENT") {
                    SettingsActionRow(Icons.Rounded.AspectRatio, "Aspect Ratio", aspectRatioMode.label, onClick = onCycleAspectRatio)
                    SettingsSwitchRow(Icons.Rounded.WbSunny, "Ambient Glow", isAmbientGlow, onCheckedChange = { onToggleAmbientGlow() })
                }
            }
        }
    }
}
