package com.playme.player.feature.player.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.feature.player.model.GestureType
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    videoSurface: @Composable (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    BackHandler {
        if (state.isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            viewModel.onAction(PlayerAction.ToggleFullscreen)
        } else {
            onClose()
        }
    }

    DisposableEffect(state.isFullscreen) {
        if (state.isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
    ) {
        // 1. Gesture Detection Layer
        PlayerGestureBox(
            isLocked = state.isLocked,
            currentVolume = state.volumePercent,
            currentBrightness = state.brightnessPercent,
            currentPositionMs = state.currentPositionMs,
            durationMs = state.durationMs,
            onSingleTap = { viewModel.onAction(PlayerAction.ToggleControls) },
            onDoubleTapSeek = { delta -> viewModel.onAction(PlayerAction.SeekBy(delta)) },
            onTogglePlay = { viewModel.onAction(PlayerAction.TogglePlay) },
            onSpeedBoost = { enabled -> viewModel.onAction(PlayerAction.SetSpeedBoost(enabled)) },
            onGestureStart = { type, value -> viewModel.onAction(PlayerAction.StartGesture(type, value)) },
            onGestureUpdate = { value ->
                when (state.activeGesture) {
                    GestureType.VOLUME -> viewModel.onAction(PlayerAction.SetVolume(value))
                    GestureType.BRIGHTNESS -> viewModel.onAction(PlayerAction.SetBrightness(value))
                    else -> viewModel.onAction(PlayerAction.UpdateGesture(value))
                }
            },
            onGestureEnd = { viewModel.onAction(PlayerAction.EndGesture) },
            onSeekCommit = { targetMs -> viewModel.onAction(PlayerAction.SeekTo(targetMs)) },
            onLockedTouchAlert = { viewModel.onAction(PlayerAction.ShowToast("Controls Locked")) }
        ) {
            // 2. Video Stage Canvas with Ambient Glow & Subtitles
            VideoStageCanvas(
                video = state.currentVideo,
                aspectRatioMode = state.aspectRatioMode,
                isControlsVisible = state.isControlsVisible && !state.isLocked,
                currentSubtitle = state.currentSubtitle,
                videoSurface = videoSurface ?: {
                    VideoSurfaceView(
                        engine = viewModel.engine,
                        aspectRatioMode = state.aspectRatioMode
                    )
                }
            )
        }

        // 3. Gesture Indicator Overlays (Volume, Brightness, Scrub, 2x Speed Pill)
        GestureOverlayHUD(
            activeGesture = state.activeGesture,
            gestureValue = state.gestureValue,
            isSpeedBoostActive = state.isSpeedBoostActive,
            currentPosMs = state.currentPositionMs,
            durationMs = state.durationMs,
            onSelectBoostSpeed = { speed -> viewModel.onAction(PlayerAction.SetSpeedBoostValue(speed)) },
            showLockedAlert = state.isLocked && state.activeGesture != GestureType.NONE,
            boostSpeed = state.boostSpeed
        )

        // 4. Top HUD Bar
        AnimatedVisibility(
            visible = state.isControlsVisible && !state.isLocked,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            PlayerTopBar(
                title = state.currentVideo?.title ?: "Unknown Video",
                resolution = state.currentVideo?.resolution ?: "1080p",
                fileSize = state.currentVideo?.fileSizeFormatted ?: "",
                format = state.currentVideo?.format ?: "MP4",
                onBackClick = onClose,
                onQueueClick = { viewModel.onAction(PlayerAction.SetQueueOpen(true)) },
                onOpenSettings = { tab -> viewModel.onAction(PlayerAction.SetSettingsOpen(true, tab)) },
                modifier = Modifier.statusBarsPadding()
            )
        }

        // 5. Floating Side Action Dock (Right Capsule)
        AnimatedVisibility(
            visible = state.isControlsVisible || state.isLocked,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            PlayerSideDock(
                isMuted = state.isMuted,
                isLocked = state.isLocked,
                isAbActive = state.isAbLoopActive,
                onToggleMute = { viewModel.onAction(PlayerAction.ToggleMute) },
                onToggleLock = { viewModel.onAction(PlayerAction.ToggleLock) },
                onSnapshot = { viewModel.onAction(PlayerAction.ShowToast("Snapshot Saved")) },
                onToggleAbLoop = { viewModel.onAction(PlayerAction.ToggleAbLoop) },
                onRotate = { viewModel.onAction(PlayerAction.ToggleFullscreen) },
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        // 6. Bottom HUD Bar (Scrubber & Hero Transport Deck)
        AnimatedVisibility(
            visible = state.isControlsVisible && !state.isLocked,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerBottomBar(
                currentPositionMs = state.currentPositionMs,
                durationMs = state.durationMs,
                bufferedPositionMs = state.bufferedPositionMs,
                isPlaying = state.isPlaying,
                speed = state.playbackSpeed,
                onSeekTo = { posMs -> viewModel.onAction(PlayerAction.SeekTo(posMs)) },
                onTogglePlay = { viewModel.onAction(PlayerAction.TogglePlay) },
                onCycleSpeed = { viewModel.onAction(PlayerAction.SetSpeedPickerOpen(true)) },
                onPlayPrevious = { viewModel.onAction(PlayerAction.PlayPrevious) },
                onPlayNext = { viewModel.onAction(PlayerAction.PlayNext) },
                onFullscreenToggle = { viewModel.onAction(PlayerAction.ToggleFullscreen) },
                modifier = Modifier.navigationBarsPadding()
            )
        }

        // 7. Floating Feedback Toast Pill
        state.toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp)
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

        // 8. Slide-Up Playlist Queue Drawer
        QueueBottomSheet(
            isOpen = state.isQueueOpen,
            onClose = { viewModel.onAction(PlayerAction.SetQueueOpen(false)) },
            playlist = state.playlist,
            currentVideo = state.currentVideo,
            currentIndex = state.currentIndex,
            isShuffle = state.isShuffle,
            repeatMode = state.repeatMode,
            isAutoplay = state.isAutoplay,
            onToggleShuffle = { viewModel.onAction(PlayerAction.ToggleShuffle) },
            onCycleRepeatMode = { viewModel.onAction(PlayerAction.CycleRepeatMode) },
            onToggleAutoplay = { viewModel.onAction(PlayerAction.ToggleAutoplay) },
            onSelectTrack = { video -> viewModel.onAction(PlayerAction.PlayTrack(video)) },
            onRemoveTrack = { id -> viewModel.onAction(PlayerAction.RemoveFromQueue(id)) },
            onClearQueue = { viewModel.onAction(PlayerAction.ClearQueue) }
        )

        // 9. Side/Bottom Settings Drawer (Delays, Decoder, Aspect Ratio, Audio FX)
        PlayerSettingsSidebar(
            isOpen = state.isSettingsOpen,
            onClose = { viewModel.onAction(PlayerAction.SetSettingsOpen(false)) },
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
            initialTab = when (state.activeSettingsTab) {
                com.playme.player.feature.player.model.SettingsTab.SUBTITLES -> "subtitles"
                com.playme.player.feature.player.model.SettingsTab.AUDIO -> "audio"
                else -> "root"
            },
            onAdjustAudioDelay = { delta -> viewModel.onAction(PlayerAction.AdjustAudioDelay(delta)) },
            onAdjustSubtitleDelay = { delta -> viewModel.onAction(PlayerAction.AdjustSubtitleDelay(delta)) },
            onResetDelays = { viewModel.onAction(PlayerAction.ResetDelays) },
            onSelectDecoderMode = { mode -> viewModel.onAction(PlayerAction.SetDecoderMode(mode)) },
            onSelectAspectRatio = { mode -> viewModel.onAction(PlayerAction.SetAspectRatio(mode)) },
            onToggleDialogueClarity = { viewModel.onAction(PlayerAction.ToggleDialogueClarity) },
            onVolumeBoostChange = { boost -> viewModel.onAction(PlayerAction.SetVolumeBoost(boost)) },
            onToggleAmbientGlow = { viewModel.onAction(PlayerAction.ToggleAmbientGlow) },
            onBrightnessChange = { b -> viewModel.onAction(PlayerAction.SetBrightness(b)) },
            onToggleBackgroundPlay = { viewModel.onAction(PlayerAction.ToggleBackgroundPlay) },
            onSelectSpeed = { speed -> viewModel.onAction(PlayerAction.SetSpeed(speed)) },
            onSelectSleepTimer = { minutes -> viewModel.onAction(PlayerAction.SetSleepTimer(minutes)) },
            onSelectAudioTrack = { track -> viewModel.onAction(PlayerAction.SelectAudioTrack(track)) },
            onSelectSubtitleTrack = { track -> viewModel.onAction(PlayerAction.SelectSubtitleTrack(track)) },
            onSelectSubtitleSize = { size -> viewModel.onAction(PlayerAction.SetSubtitleSize(size)) }
        )

        // 10. Playback Speed Selector Studio Dock
        PlaybackSpeedStudioDock(
            isOpen = state.isSpeedPickerOpen,
            onClose = { viewModel.onAction(PlayerAction.SetSpeedPickerOpen(false)) },
            speed = state.playbackSpeed,
            onSpeedChange = { speed -> viewModel.onAction(PlayerAction.SetSpeed(speed)) }
        )
    }
}
