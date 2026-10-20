package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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

@Composable
fun SettingsRootView(
    selectedAudio: String,
    audioDelayMs: Long,
    isDialogueClarity: Boolean,
    volumeBoost: Int,
    selectedSubtitle: String,
    subtitleDelayMs: Long,
    playbackSpeed: Float,
    decoderMode: DecoderMode,
    sleepTimerMinutes: Int,
    backgroundPlay: Boolean,
    aspectRatioMode: AspectRatioMode,
    ambientGlow: Boolean,
    brightnessPercent: Float,
    onNavigate: (String) -> Unit,
    onToggleDialogueClarity: () -> Unit,
    onVolumeBoostChange: (Int) -> Unit,
    onToggleBackgroundPlay: () -> Unit,
    onToggleAmbientGlow: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // GROUP 1: AUDIO
        SettingsSection(title = "AUDIO") {
            SettingsMobileRow(Icons.Rounded.Headphones, "Audio Track", selectedAudio, onClick = { onNavigate("audio-track") })
            SettingsMobileRow(Icons.Rounded.Sync, "Audio Sync", formatDelay(audioDelayMs), onClick = { onNavigate("audio-sync") })
            SettingsSwitchRow(Icons.Rounded.GraphicEq, "Dialogue Clarity", isDialogueClarity, onCheckedChange = { onToggleDialogueClarity() })
            SettingsSliderRow(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                label = "Volume Boost",
                currentText = "$volumeBoost%",
                value = volumeBoost.toFloat(),
                range = 100f..200f,
                onValueChange = { onVolumeBoostChange(it.toInt()) }
            )
        }

        // GROUP 2: SUBTITLES
        SettingsSection(title = "SUBTITLES") {
            SettingsMobileRow(Icons.Rounded.Subtitles, "Subtitles", selectedSubtitle, onClick = { onNavigate("subtitles") })
            SettingsMobileRow(Icons.Rounded.Tune, "Subtitle Delay", formatDelay(subtitleDelayMs), onClick = { onNavigate("subtitle-sync") })
        }

        // GROUP 3: PLAYBACK & ENGINE
        SettingsSection(title = "PLAYBACK & ENGINE") {
            SettingsMobileRow(Icons.Rounded.Speed, "Playback Speed", "${playbackSpeed}x", onClick = { onNavigate("speed") })
            SettingsMobileRow(Icons.Rounded.Bolt, "Decoder Engine", decoderMode.name, onClick = { onNavigate("decoder") })
            SettingsMobileRow(Icons.Rounded.Timer, "Sleep Timer", if (sleepTimerMinutes == 0) "Off" else "${sleepTimerMinutes}m", onClick = { onNavigate("sleep-timer") })
            SettingsSwitchRow(Icons.Rounded.PictureInPictureAlt, "Background Audio", backgroundPlay, onCheckedChange = { onToggleBackgroundPlay() })
        }

        // GROUP 4: DISPLAY
        SettingsSection(title = "DISPLAY") {
            SettingsMobileRow(Icons.Rounded.AspectRatio, "Aspect Ratio", aspectRatioMode.label, onClick = { onNavigate("aspect") })
            SettingsSwitchRow(Icons.Rounded.Flare, "Ambient Edge Glow", ambientGlow, onCheckedChange = { onToggleAmbientGlow() })
            SettingsSliderRow(
                icon = Icons.Rounded.BrightnessMedium,
                label = "Screen Brightness",
                currentText = "${(brightnessPercent * 100).toInt()}%",
                value = brightnessPercent * 100f,
                range = 10f..100f,
                onValueChange = { onBrightnessChange(it / 100f) }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = Color(0xFF64748B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
        SettingsMobileCard(content = content)
    }
}

@Composable
private fun SettingsSliderRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    currentText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                }
                Text(label, color = Color(0xFFF1F5F9), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x14FFFFFF))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = currentText,
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth().height(20.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF6366F1),
                inactiveTrackColor = Color(0x14FFFFFF)
            )
        )
    }
}

private fun formatDelay(delayMs: Long): String {
    return if (delayMs == 0L) "0.0s" else String.format("%.1fs", delayMs / 1000f)
}
