package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel
import kotlin.math.abs

@Composable
fun LandscapePlayerScreen(
    viewModel: PlayerViewModel = viewModel(),
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var dragAccumulatorX by remember { mutableFloatStateOf(0f) }
    var showLockedToast by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayMeColors.ObsidianDark)
    ) {
        // 1. Gesture Surface / Video Viewport Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.isControlsLocked) {
                    detectTapGestures(
                        onTap = {
                            if (state.isControlsLocked) {
                                showLockedToast = true
                            } else if (state.isSpeedMenuOpen) {
                                viewModel.onAction(PlayerAction.ToggleSpeedMenu(false))
                            } else if (state.isQueueDrawerOpen) {
                                viewModel.onAction(PlayerAction.ToggleQueueDrawer(false))
                            } else if (state.isSettingsOpen) {
                                viewModel.onAction(PlayerAction.ToggleSettings(false))
                            } else {
                                viewModel.onAction(PlayerAction.ToggleControlsVisible)
                            }
                        },
                        onLongPress = {
                            if (!state.isControlsLocked) {
                                viewModel.onAction(PlayerAction.StartQuickBoost(2.0f))
                            }
                        }
                    )
                }
                .pointerInput(state.isControlsLocked) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulatorX = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (!state.isControlsLocked) {
                                dragAccumulatorX += dragAmount.x
                                // 8dp drag deadzone to avoid false triggers
                                if (abs(dragAccumulatorX) > 8f) {
                                    val deltaSeconds = (dragAccumulatorX * 0.35f).toInt()
                                    viewModel.onAction(PlayerAction.UpdateGestureSeek(deltaSeconds))
                                }
                            }
                        },
                        onDragEnd = {
                            if (!state.isControlsLocked && abs(dragAccumulatorX) > 8f) {
                                viewModel.onAction(PlayerAction.EndGestureSeek(apply = true))
                            }
                            dragAccumulatorX = 0f
                        },
                        onDragCancel = {
                            viewModel.onAction(PlayerAction.EndGestureSeek(apply = false))
                            dragAccumulatorX = 0f
                        }
                    )
                }
        )

        // 2. Gesture Seek HUD
        ScreenGestureSeekHUD(
            isSeeking = state.gestureSeek.isSeeking,
            deltaSeconds = state.gestureSeek.deltaSeconds,
            targetPositionMs = state.gestureSeek.targetPositionMs,
            durationMs = state.durationMs,
            showLockedToast = showLockedToast,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp)
        )

        // 3. Quick Speed Boost HUD (Long-press 2x)
        QuickSpeedBoostHUD(
            isActive = state.isQuickBoosting,
            currentBoostSpeed = state.quickBoostSpeed,
            onSelectSpeed = { speed ->
                viewModel.onAction(PlayerAction.StartQuickBoost(speed))
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        )

        // 4. Main HUD Controls (Top Bar + Center Play + Bottom Timeline)
        PlayerControlsHud(
            state = state,
            onAction = { action ->
                if (action is PlayerAction.ClosePlayer) {
                    onClose()
                } else {
                    viewModel.onAction(action)
                }
            }
        )

        // 5. Playback Speed Selector Modal
        PlaybackSpeedMenu(
            isOpen = state.isSpeedMenuOpen,
            currentSpeed = state.playbackSpeed,
            onSpeedChange = { viewModel.onAction(PlayerAction.SetPlaybackSpeed(it)) },
            onClose = { viewModel.onAction(PlayerAction.ToggleSpeedMenu(false)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
        )

        // 6. Up Next / Playback Queue Drawer
        QueueDrawer(
            isOpen = state.isQueueDrawerOpen,
            queue = state.queue,
            onSelectQueueItem = { id -> viewModel.onAction(PlayerAction.SelectQueueItem(id)) },
            onClose = { viewModel.onAction(PlayerAction.ToggleQueueDrawer(false)) },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // 7. Quick Settings Sidebar
        PlayerSettingsSidebar(
            isOpen = state.isSettingsOpen,
            activeTab = state.activeSettingsTab,
            scaleMode = state.scaleMode,
            onTabSelected = { tab -> viewModel.onAction(PlayerAction.SelectSettingsTab(tab)) },
            onScaleModeSelected = { mode -> viewModel.onAction(PlayerAction.CycleScaleMode) },
            onClose = { viewModel.onAction(PlayerAction.ToggleSettings(false)) },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
