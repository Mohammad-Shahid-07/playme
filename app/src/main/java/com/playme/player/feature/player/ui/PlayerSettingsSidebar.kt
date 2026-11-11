package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.playme.player.core.model.MediaTrackInfo
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
    availableAudioTracks: List<MediaTrackInfo> = emptyList(),
    availableSubtitleTracks: List<MediaTrackInfo> = emptyList(),
    initialTab: String = "root",
    onAdjustAudioDelay: (Long) -> Unit,
    onAdjustSubtitleDelay: (Long) -> Unit,
    @Suppress("UNUSED_PARAMETER") onResetDelays: () -> Unit = {},
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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 1. Clickable Dimmed Backdrop (animated so closed drawer does not block touches)
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x59000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
            )
        }

        // 2. Right Sliding Drawer Panel
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val drawerShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight()
                    .shadow(24.dp, drawerShape)
                    .background(Color(0xF00C0E18), drawerShape)
                    .border(1.dp, Color(0x1FFFFFFF), drawerShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .windowInsetsPadding(
                        WindowInsets.systemBars
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    )
            ) {
                if (currentView == "root") {
                    SettingsNavHeader(title = "Player Settings", onClose = onClose)
                    SettingsRootView(
                        selectedAudio = resolveDisplayAudioLabel(selectedAudio, availableAudioTracks),
                        selectedSubtitle = resolveDisplaySubtitleLabel(selectedSubtitle, availableSubtitleTracks),
                        audioDelayMs = audioDelayMs,
                        subtitleDelayMs = subtitleDelayMs,
                        decoderMode = decoderMode,
                        aspectRatioMode = aspectRatioMode,
                        isDialogueClarity = isDialogueClarity,
                        volumeBoost = volumeBoost,
                        ambientGlow = isAmbientGlow,
                        brightnessPercent = brightnessPercent,
                        sleepTimerMinutes = sleepTimerMinutes,
                        backgroundPlay = backgroundPlay,
                        playbackSpeed = playbackSpeed,
                        onNavigate = { currentView = it },
                        onToggleDialogueClarity = onToggleDialogueClarity,
                        onVolumeBoostChange = onVolumeBoostChange,
                        onToggleAmbientGlow = onToggleAmbientGlow,
                        onBrightnessChange = onBrightnessChange,
                        onToggleBackgroundPlay = onToggleBackgroundPlay
                    )
                } else {
                    PlayerSettingsSubViewContent(
                        currentView = currentView,
                        audioDelayMs = audioDelayMs,
                        subtitleDelayMs = subtitleDelayMs,
                        decoderMode = decoderMode,
                        aspectRatioMode = aspectRatioMode,
                        sleepTimerMinutes = sleepTimerMinutes,
                        playbackSpeed = playbackSpeed,
                        selectedAudio = selectedAudio,
                        selectedSubtitle = selectedSubtitle,
                        subtitleSize = subtitleSize,
                        availableAudioTracks = availableAudioTracks,
                        availableSubtitleTracks = availableSubtitleTracks,
                        onBack = { currentView = "root" },
                        onAdjustAudioDelay = onAdjustAudioDelay,
                        onAdjustSubtitleDelay = onAdjustSubtitleDelay,
                        onSelectDecoderMode = onSelectDecoderMode,
                        onSelectAspectRatio = onSelectAspectRatio,
                        onSelectSpeed = onSelectSpeed,
                        onSelectSleepTimer = onSelectSleepTimer,
                        onSelectAudioTrack = onSelectAudioTrack,
                        onSelectSubtitleTrack = onSelectSubtitleTrack,
                        onSelectSubtitleSize = onSelectSubtitleSize
                    )
                }
            }
        }
    }
}
