package com.playme.player.feature.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.util.formatDelaySeconds
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode
import java.util.Locale
import kotlin.math.roundToInt

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
    val speedLabel = formatSpeed(playbackSpeed)
    val isAutoBrightness = brightnessPercent < 0f
    val brightnessSliderValue = if (isAutoBrightness) 50f else (brightnessPercent * 100f).coerceIn(10f, 100f)

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
            SettingsMobileRow(Icons.Rounded.Sync, "Audio Sync", formatDelaySeconds(audioDelayMs), onClick = { onNavigate("audio-sync") })
            SettingsSwitchRow(Icons.Rounded.GraphicEq, "Dialogue Clarity", isDialogueClarity, onCheckedChange = { onToggleDialogueClarity() })
            SettingsSliderRow(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                label = "Volume Boost",
                value = volumeBoost.toFloat(),
                range = 100f..200f,
                valueFormatter = { "${it.roundToInt()}%" },
                onValueChange = { onVolumeBoostChange(it.roundToInt()) },
                commitOnFinish = true
            )
        }

        // GROUP 2: SUBTITLES
        SettingsSection(title = "SUBTITLES") {
            SettingsMobileRow(Icons.Rounded.Subtitles, "Subtitles", selectedSubtitle, onClick = { onNavigate("subtitles") })
            SettingsMobileRow(Icons.Rounded.Tune, "Subtitle Delay", formatDelaySeconds(subtitleDelayMs), onClick = { onNavigate("subtitle-sync") })
        }

        // GROUP 3: PLAYBACK & ENGINE
        SettingsSection(title = "PLAYBACK & ENGINE") {
            SettingsMobileRow(Icons.Rounded.Speed, "Playback Speed", speedLabel, onClick = { onNavigate("speed") })
            SettingsMobileRow(Icons.Rounded.Bolt, "Decoder Engine", decoderMode.name, onClick = { onNavigate("decoder") })
            SettingsMobileRow(
                Icons.Rounded.Timer,
                "Sleep Timer",
                if (sleepTimerMinutes == 0) "Off" else "${sleepTimerMinutes}m",
                onClick = { onNavigate("sleep-timer") }
            )
            SettingsSwitchRow(Icons.Rounded.PictureInPictureAlt, "Background Audio", backgroundPlay, onCheckedChange = { onToggleBackgroundPlay() })
        }

        // GROUP 4: DISPLAY
        SettingsSection(title = "DISPLAY") {
            SettingsMobileRow(Icons.Rounded.AspectRatio, "Aspect Ratio", aspectRatioMode.label, onClick = { onNavigate("aspect") })
            SettingsSwitchRow(Icons.Rounded.Flare, "Ambient Edge Glow", ambientGlow, onCheckedChange = { onToggleAmbientGlow() })
            SettingsSliderRow(
                icon = Icons.Rounded.BrightnessMedium,
                label = "Screen Brightness",
                value = brightnessSliderValue,
                range = 10f..100f,
                valueFormatter = { "${it.roundToInt()}%" },
                onValueChange = { onBrightnessChange((it / 100f).coerceIn(0.1f, 1f)) },
                idleTextOverride = if (isAutoBrightness) "Auto" else null
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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

private fun formatSpeed(speed: Float): String {
    val hundredths = (speed * 100f).roundToInt()
    return if (hundredths % 10 == 0) {
        String.format(Locale.US, "%.1fx", hundredths / 100f)
    } else {
        String.format(Locale.US, "%.2fx", hundredths / 100f)
    }
}
