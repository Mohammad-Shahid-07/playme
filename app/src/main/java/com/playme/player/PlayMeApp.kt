package com.playme.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.core.history.WatchProgressStore
import com.playme.player.core.model.VideoItem
import com.playme.player.core.player.UniversalPlayerEngine
import com.playme.player.feature.library.model.LibraryAction
import com.playme.player.feature.library.ui.LibraryScreen
import com.playme.player.feature.library.vm.LibraryViewModel
import com.playme.player.feature.navigation.BottomNavBar
import com.playme.player.feature.navigation.MainNavTab
import com.playme.player.feature.navigation.MainTabPlaceholderView
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.ui.PlayerScreen
import com.playme.player.feature.player.vm.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlayMeApp(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayMeColors.Workbench)
    ) {
        var currentTab by remember { mutableStateOf(MainNavTab.VIDEO) }
        var activeVideo by remember { mutableStateOf<VideoItem?>(null) }
        var activePlaylist by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
        val playerViewModel: PlayerViewModel = viewModel()
        val libraryViewModel: LibraryViewModel = viewModel()
        val context = LocalContext.current
        val progressStore = remember(context) { WatchProgressStore(context.applicationContext) }
        val persistScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
        val recordProgress = remember(progressStore, libraryViewModel, persistScope) {
            { played: VideoItem, positionMs: Long, durationMs: Long ->
                persistScope.launch {
                    progressStore.record(
                        videoId = played.id,
                        positionMs = positionMs,
                        durationMs = durationMs
                    )
                    withContext(Dispatchers.Main) {
                        libraryViewModel.onAction(LibraryAction.RefreshVideos)
                    }
                }
                Unit
            }
        }

        DisposableEffect(playerViewModel, recordProgress) {
            playerViewModel.setProgressRecorder(recordProgress)
            onDispose { playerViewModel.setProgressRecorder(null) }
        }

        val video = activeVideo
        if (video != null) {
            key(video.id) {
                val playerEngine = remember(video.id) { UniversalPlayerEngine(context) }

                DisposableEffect(video.id, playerEngine) {
                    playerViewModel.attachEngine(playerEngine)
                    playerViewModel.setInitialVideo(video, activePlaylist.ifEmpty { listOf(video) })
                    onDispose {
                        val playerState = playerViewModel.state.value
                        playerViewModel.attachEngine(null)
                        playerEngine.release()
                        playerState.currentVideo?.let { played ->
                            recordProgress(played, playerState.currentPositionMs, playerState.durationMs)
                        }
                    }
                }

                PlayerScreen(
                    viewModel = playerViewModel,
                    onClose = {
                        playerViewModel.onAction(PlayerAction.Pause)
                        activeVideo = null
                    }
                )
            }
        } else {
            when (currentTab) {
                MainNavTab.VIDEO -> LibraryScreen(
                    onVideoClick = { clickedVideo, playlist ->
                        activePlaylist = playlist
                        playerViewModel.prepareVideo(clickedVideo, playlist)
                        activeVideo = clickedVideo
                    }
                )
                else -> MainTabPlaceholderView(tab = currentTab)
            }

            BottomNavBar(
                activeTab = currentTab,
                onTabSelected = { currentTab = it },
                onBigPlayClick = { /* Quick play */ },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
