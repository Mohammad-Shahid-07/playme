package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.playme.player.feature.player.model.AspectRatioMode
import com.playme.player.feature.player.model.DecoderMode

@Composable
fun PlayerSettingsSidebar(
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
    subtitleSize: String,
    volumeBoost: Int,
    brightnessPercent: Float,
    initialTab: String = "root",
    onAdjustAudioDelay: (Long) -> Unit,
    onAdjustSubtitleDelay: (Long) -> Unit,
    onResetDelays: () -> Unit,
    onSelectDecoderMode: (DecoderMode) -> Unit,
    onSelectAspectRatio: (AspectRatioMode) -> Unit,
    onToggleDialogueClarity: () -> Unit,
    onVolumeBoostChange: (Int) -> Unit,
    onToggleAmbientGlow: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onToggleBackgroundPlay: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onSelectSleepTimer: (Int) -> Unit,
    onSelectAudioTrack: (String) -> Unit,
    onSelectSubtitleTrack: (String) -> Unit,
    onSelectSubtitleSize: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentView by remember { mutableStateOf("root") }

    LaunchedEffect(isOpen, initialTab) {
        if (isOpen) {
            currentView = when (initialTab) {
                "subtitles" -> "subtitles"
                "audio" -> "audio-track"
                else -> "root"
            }
        }
    }

    if (!isOpen) return

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 1. Clickable Dimmed Backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x59000000))
                .clickable(onClick = onClose)
        )

        // 2. Right Sliding Drawer Panel
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .width(295.dp)
                    .fillMaxHeight()
                    .background(Color(0xDF0A0D18))
                    .border(width = 1.dp, color = Color(0x1AFFFFFF), shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .shadow(32.dp, spotColor = Color.Black)
                    .clickable(enabled = false) {}
            ) {
                when (currentView) {
                    "root" -> {
                        SettingsNavHeader(title = "Settings", onClose = onClose)
                        SettingsRootView(
                            selectedAudio = selectedAudio,
                            audioDelayMs = audioDelayMs,
                            isDialogueClarity = isDialogueClarity,
                            volumeBoost = volumeBoost,
                            selectedSubtitle = selectedSubtitle,
                            subtitleDelayMs = subtitleDelayMs,
                            playbackSpeed = playbackSpeed,
                            decoderMode = decoderMode,
                            sleepTimerMinutes = sleepTimerMinutes,
                            backgroundPlay = backgroundPlay,
                            aspectRatioMode = aspectRatioMode,
                            ambientGlow = isAmbientGlow,
                            brightnessPercent = brightnessPercent,
                            onNavigate = { currentView = it },
                            onToggleDialogueClarity = onToggleDialogueClarity,
                            onVolumeBoostChange = onVolumeBoostChange,
                            onToggleBackgroundPlay = onToggleBackgroundPlay,
                            onToggleAmbientGlow = onToggleAmbientGlow,
                            onBrightnessChange = onBrightnessChange
                        )
                    }

                    "audio-track" -> {
                        SettingsChoiceSubView(
                            title = "Audio Track",
                            items = listOf(
                                ChoiceItem("English 5.1", "English", "Dolby Digital 5.1 (Surround) · Default"),
                                ChoiceItem("Hindi Stereo", "Hindi", "Stereo AAC 320 kbps"),
                                ChoiceItem("Japanese DTS", "Japanese", "Original DTS 5.1 Surround")
                            ),
                            selectedId = selectedAudio,
                            onSelect = { onSelectAudioTrack(it); currentView = "root" },
                            onBack = { currentView = "root" }
                        )
                    }

                    "audio-sync" -> {
                        SettingsSyncSubView(
                            title = "Audio Sync",
                            delayMs = audioDelayMs,
                            isAudio = true,
                            onAdjustDelay = onAdjustAudioDelay,
                            onReset = onResetDelays,
                            onBack = { currentView = "root" }
                        )
                    }

                    "subtitles" -> {
                        SettingsChoiceSubView(
                            title = "Subtitles",
                            items = listOf(
                                ChoiceItem("Off", "Off", "Captions disabled"),
                                ChoiceItem("English [CC]", "English [CC]", "Embedded SubRip"),
                                ChoiceItem("Spanish", "Spanish", "Embedded SubRip")
                            ),
                            selectedId = selectedSubtitle,
                            onSelect = { onSelectSubtitleTrack(it); currentView = "root" },
                            onBack = { currentView = "root" },
                            extraSectionTitle = "SUBTITLE FONT SIZE",
                            extraItems = listOf(
                                ChoiceItem("Normal", "Normal"),
                                ChoiceItem("Large", "Large"),
                                ChoiceItem("Extra Large", "Extra Large")
                            ),
                            extraSelectedId = subtitleSize,
                            onExtraSelect = onSelectSubtitleSize
                        )
                    }

                    "subtitle-sync" -> {
                        SettingsSyncSubView(
                            title = "Subtitle Sync",
                            delayMs = subtitleDelayMs,
                            isAudio = false,
                            onAdjustDelay = onAdjustSubtitleDelay,
                            onReset = onResetDelays,
                            onBack = { currentView = "root" }
                        )
                    }

                    "speed" -> {
                        SettingsChoiceSubView(
                            title = "Playback Speed",
                            items = listOf(
                                ChoiceItem("0.5", "0.5x"),
                                ChoiceItem("0.75", "0.75x"),
                                ChoiceItem("1.0", "1.0x (Normal)"),
                                ChoiceItem("1.25", "1.25x"),
                                ChoiceItem("1.5", "1.5x"),
                                ChoiceItem("1.75", "1.75x"),
                                ChoiceItem("2.0", "2.0x")
                            ),
                            selectedId = playbackSpeed.toString(),
                            onSelect = { onSelectSpeed(it.toFloatOrNull() ?: 1.0f); currentView = "root" },
                            onBack = { currentView = "root" }
                        )
                    }

                    "decoder" -> {
                        SettingsChoiceSubView(
                            title = "Hardware Decoder",
                            items = listOf(
                                ChoiceItem("HW+", "HW+ (Hardware Acceleration Plus)", "Custom optimized GPU rendering · Recommended"),
                                ChoiceItem("HW", "HW (Standard Hardware)", "Android native MediaCodec acceleration"),
                                ChoiceItem("SW", "SW (Software Rendering)", "CPU software fallback for rare unsupported codecs")
                            ),
                            selectedId = decoderMode.name,
                            onSelect = {
                                onSelectDecoderMode(if (it == "SW") DecoderMode.SW else DecoderMode.HW)
                                currentView = "root"
                            },
                            onBack = { currentView = "root" }
                        )
                    }

                    "sleep-timer" -> {
                        SettingsChoiceSubView(
                            title = "Sleep Timer",
                            items = listOf(
                                ChoiceItem("0", "Off"),
                                ChoiceItem("15", "15 Minutes"),
                                ChoiceItem("30", "30 Minutes"),
                                ChoiceItem("45", "45 Minutes"),
                                ChoiceItem("60", "60 Minutes")
                            ),
                            selectedId = sleepTimerMinutes.toString(),
                            onSelect = { onSelectSleepTimer(it.toIntOrNull() ?: 0); currentView = "root" },
                            onBack = { currentView = "root" }
                        )
                    }

                    "aspect" -> {
                        SettingsChoiceSubView(
                            title = "Aspect Ratio",
                            items = AspectRatioMode.entries.map {
                                ChoiceItem(
                                    id = it.name,
                                    title = it.label,
                                    desc = when (it) {
                                        AspectRatioMode.BEST_FIT -> "Preserves original aspect ratio with black bars"
                                        AspectRatioMode.FILL -> "Zooms and crops to fill display"
                                        AspectRatioMode.FIT_SCREEN -> "Stretches video to touch screen edges"
                                        AspectRatioMode.ORIGINAL -> "Displays exact native pixel resolution"
                                        AspectRatioMode.SIXTEEN_NINE -> "Locks to 16:9 widescreen ratio"
                                        AspectRatioMode.FOUR_THREE -> "Locks to 4:3 standard ratio"
                                    }
                                )
                            },
                            selectedId = aspectRatioMode.name,
                            onSelect = { name ->
                                AspectRatioMode.entries.find { it.name == name }?.let { onSelectAspectRatio(it) }
                                currentView = "root"
                            },
                            onBack = { currentView = "root" }
                        )
                    }
                }
            }
        }
    }
}
