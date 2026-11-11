package com.playme.player.feature.player.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.model.PlayerState
import com.playme.player.feature.player.model.SettingsTab

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
internal fun PlayerFullscreenSystemBarsEffect(isFullscreen: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context.findActivity()

    DisposableEffect(isFullscreen, activity, view) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
internal fun PlayerToastPill(
    message: String?,
    modifier: Modifier = Modifier
) {
    message?.let { msg ->
        Box(
            modifier = modifier
                .padding(bottom = 60.dp, start = 24.dp, end = 24.dp)
                .shadow(16.dp, RoundedCornerShape(999.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xE60C0E18))
                .border(1.dp, Color(0x33757BF8), RoundedCornerShape(999.dp))
                .padding(horizontal = 20.dp, vertical = 9.dp)
        ) {
            Text(
                text = msg,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
internal fun PlayerModalOverlays(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit
) {
    QueueBottomSheet(
        isOpen = state.isQueueOpen,
        onClose = { onAction(PlayerAction.SetQueueOpen(false)) },
        playlist = state.playlist,
        currentVideo = state.currentVideo,
        currentIndex = state.currentIndex,
        isShuffle = state.isShuffle,
        repeatMode = state.repeatMode,
        isAutoplay = state.isAutoplay,
        onToggleShuffle = { onAction(PlayerAction.ToggleShuffle) },
        onCycleRepeatMode = { onAction(PlayerAction.CycleRepeatMode) },
        onToggleAutoplay = { onAction(PlayerAction.ToggleAutoplay) },
        onSelectTrack = { video -> onAction(PlayerAction.PlayTrack(video)) },
        onRemoveTrack = { id -> onAction(PlayerAction.RemoveFromQueue(id)) },
        onClearQueue = { onAction(PlayerAction.ClearQueue) }
    )

    PlayerSettingsSidebar(
        isOpen = state.isSettingsOpen,
        onClose = { onAction(PlayerAction.SetSettingsOpen(false)) },
        audioDelayMs = state.audioDelayMs,
        subtitleDelayMs = state.subtitleDelayMs,
        decoderMode = state.decoderMode,
        aspectRatioMode = state.aspectRatioMode,
        isDialogueClarity = state.isDialogueClarityActive,
        isAmbientGlow = state.ambientGlowEnabled,
        sleepTimerMinutes = state.sleepTimerMinutes,
        backgroundPlay = state.backgroundPlayEnabled,
        playbackSpeed = state.playbackSpeed,
        selectedAudio = state.selectedAudioTrack,
        selectedSubtitle = state.selectedSubtitleTrack,
        subtitleSize = state.subtitleSize,
        volumeBoost = state.volumeBoostPercent,
        brightnessPercent = state.brightnessPercent,
        availableAudioTracks = state.availableAudioTracks,
        availableSubtitleTracks = state.availableSubtitleTracks,
        initialTab = when (state.activeSettingsTab) {
            SettingsTab.SUBTITLES -> "subtitles"
            SettingsTab.AUDIO -> "audio"
            else -> "root"
        },
        onAdjustAudioDelay = { delta -> onAction(PlayerAction.AdjustAudioDelay(delta)) },
        onAdjustSubtitleDelay = { delta -> onAction(PlayerAction.AdjustSubtitleDelay(delta)) },
        onResetDelays = { onAction(PlayerAction.ResetDelays) },
        onSelectDecoderMode = { mode -> onAction(PlayerAction.SetDecoderMode(mode)) },
        onSelectAspectRatio = { mode -> onAction(PlayerAction.SetAspectRatio(mode)) },
        onToggleDialogueClarity = { onAction(PlayerAction.ToggleDialogueClarity) },
        onVolumeBoostChange = { boost -> onAction(PlayerAction.SetVolumeBoost(boost)) },
        onToggleAmbientGlow = { onAction(PlayerAction.ToggleAmbientGlow) },
        onBrightnessChange = { b -> onAction(PlayerAction.SetBrightness(b)) },
        onToggleBackgroundPlay = { onAction(PlayerAction.ToggleBackgroundPlay) },
        onSelectSpeed = { speed -> onAction(PlayerAction.SetSpeed(speed)) },
        onSelectSleepTimer = { minutes -> onAction(PlayerAction.SetSleepTimer(minutes)) },
        onSelectAudioTrack = { track -> onAction(PlayerAction.SelectAudioTrack(track)) },
        onSelectSubtitleTrack = { track -> onAction(PlayerAction.SelectSubtitleTrack(track)) },
        onSelectSubtitleSize = { size -> onAction(PlayerAction.SetSubtitleSize(size)) }
    )

    PlaybackSpeedStudioDock(
        isOpen = state.isSpeedPickerOpen,
        onClose = { onAction(PlayerAction.SetSpeedPickerOpen(false)) },
        speed = state.playbackSpeed,
        onSpeedChange = { speed -> onAction(PlayerAction.SetSpeed(speed)) }
    )
}
