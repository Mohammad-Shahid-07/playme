package com.playme.player.feature.player.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    videoSurface: @Composable (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    var lockedAlertTick by remember { mutableIntStateOf(0) }
    var showLockedAlert by remember { mutableStateOf(false) }

    LaunchedEffect(lockedAlertTick, state.isLocked) {
        if (!state.isLocked || lockedAlertTick == 0) {
            showLockedAlert = false
        } else {
            showLockedAlert = true
            delay(1400L)
            showLockedAlert = false
        }
    }

    BackHandler {
        when {
            state.isLocked -> viewModel.onAction(PlayerAction.ShowToast("Controls Locked"))
            state.isSpeedPickerOpen -> viewModel.onAction(PlayerAction.SetSpeedPickerOpen(false))
            state.isSettingsOpen -> viewModel.onAction(PlayerAction.SetSettingsOpen(false))
            state.isQueueOpen -> viewModel.onAction(PlayerAction.SetQueueOpen(false))
            state.isFullscreen -> viewModel.onAction(PlayerAction.ToggleFullscreen)
            else -> onClose()
        }
    }

    PlayerFullscreenSystemBarsEffect(isFullscreen = state.isFullscreen)

    // Android System Volume & Window Brightness Sync
    PlayerSystemSyncEffect(
        viewModel = viewModel,
        brightnessPercent = state.brightnessPercent,
        volumePercent = state.volumePercent
    )

    PlayerAudioFocusEffect(viewModel = viewModel, isPlaying = state.isPlaying)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
    ) {
        // 1. Video Stage Canvas with Ambient Glow & Subtitles
        VideoStageCanvas(
            video = state.currentVideo,
            aspectRatioMode = state.aspectRatioMode,
            isControlsVisible = state.isControlsVisible && !state.isLocked,
            currentSubtitle = state.currentSubtitle,
            ambientGlowEnabled = state.ambientGlowEnabled,
            subtitleSize = state.subtitleSize,
            videoSurface = videoSurface ?: {
                VideoSurfaceView(
                    engine = viewModel.engine,
                    aspectRatioMode = state.aspectRatioMode,
                    activeEngineType = state.activeEngineType
                )
            }
        )

        // 2. Gesture Detection Layer (above the native surface, below the HUD)
        PlayerGestureBox(
            isLocked = state.isLocked,
            currentVolume = state.volumePercent,
            currentBrightness = state.brightnessPercent,
            currentPositionMs = state.currentPositionMs,
            durationMs = state.durationMs,
            boostSpeed = state.boostSpeed,
            onSingleTap = { viewModel.onAction(PlayerAction.ToggleControls) },
            onDoubleTapSeek = { delta -> viewModel.onAction(PlayerAction.SeekBy(delta)) },
            onTogglePlay = { viewModel.onAction(PlayerAction.TogglePlay) },
            onSpeedBoost = { enabled -> viewModel.onAction(PlayerAction.SetSpeedBoost(enabled)) },
            onBoostSpeedChange = { speed -> viewModel.onAction(PlayerAction.SetSpeedBoostValue(speed)) },
            onGestureStart = { type, value -> viewModel.onAction(PlayerAction.StartGesture(type, value)) },
            onGestureUpdate = { type, value -> viewModel.onAction(PlayerAction.UpdateGesture(type, value)) },
            onGestureEnd = { viewModel.onAction(PlayerAction.EndGesture) },
            onSeekCommit = { targetMs -> viewModel.onAction(PlayerAction.SeekTo(targetMs)) },
            onLockedTouchAlert = { lockedAlertTick++ }
        )

        // 3. Gesture Indicator Overlays (Volume, Brightness, Scrub, 2x Speed Pill)
        GestureOverlayHUD(
            activeGesture = state.activeGesture,
            gestureValue = state.gestureValue,
            isSpeedBoostActive = state.isSpeedBoostActive,
            seekTargetPosMs = state.seekPreviewPositionMs,
            durationMs = state.durationMs,
            onSelectBoostSpeed = { speed -> viewModel.onAction(PlayerAction.SetSpeedBoostValue(speed)) },
            showLockedAlert = showLockedAlert && state.isLocked,
            boostSpeed = state.boostSpeed
        )

        // 3b. Buffering Indicator (only after 1.5s of stalling)
        PlayerBufferingOverlay(
            isBuffering = state.isBuffering,
            modifier = Modifier.align(Alignment.Center)
        )

        // 4. Top HUD Bar (bar owns statusBars + displayCutout padding inside its gradient)
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
                onOpenSettings = { tab -> viewModel.onAction(PlayerAction.SetSettingsOpen(true, tab)) }
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
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.systemBars
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.End)
                    )
                    .padding(end = 12.dp)
            )
        }

        // 6. Bottom HUD Bar (bar owns navigationBars + displayCutout padding inside its gradient)
        AnimatedVisibility(
            visible = state.isControlsVisible && !state.isLocked,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerBottomBar(
                currentPositionMs = state.currentPositionMs,
                durationMs = state.durationMs,
                isPlaying = state.isPlaying,
                speed = state.playbackSpeed,
                onSeekTo = { posMs -> viewModel.onAction(PlayerAction.SeekTo(posMs)) },
                onTogglePlay = { viewModel.onAction(PlayerAction.TogglePlay) },
                onCycleSpeed = { viewModel.onAction(PlayerAction.SetSpeedPickerOpen(true)) },
                onPlayPrevious = { viewModel.onAction(PlayerAction.PlayPrevious) },
                onPlayNext = { viewModel.onAction(PlayerAction.PlayNext) },
                onFullscreenToggle = { viewModel.onAction(PlayerAction.ToggleFullscreen) },
                videoId = state.currentVideo?.id,
                filePath = state.currentVideo?.path
            )
        }

        // 7. Floating Feedback Toast Pill
        PlayerToastPill(
            message = state.toastMessage,
            modifier = Modifier.align(Alignment.Center)
        )

        // 8-10. Queue Drawer, Settings Sidebar & Speed Studio Dock
        PlayerModalOverlays(
            state = state,
            onAction = viewModel::onAction
        )
    }
}
