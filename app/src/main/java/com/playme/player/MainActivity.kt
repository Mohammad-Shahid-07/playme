package com.playme.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.playme.player.core.designsystem.PlayMeColors
import com.playme.player.core.designsystem.PlayMeTheme
import com.playme.player.feature.library.ui.LibraryScreen
import com.playme.player.feature.navigation.BottomNavBar
import com.playme.player.feature.navigation.MainNavTab
import com.playme.player.feature.navigation.MainTabPlaceholderView

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Scans auto-trigger in LibraryViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        checkAndRequestStoragePermissions()

        setContent {
            PlayMeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PlayMeColors.Workbench)
                ) {
                    var currentTab by remember { mutableStateOf(MainNavTab.VIDEO) }
                    var activeVideo by remember { mutableStateOf<com.playme.player.core.model.VideoItem?>(null) }
                    var activePlaylist by remember { mutableStateOf<List<com.playme.player.core.model.VideoItem>>(emptyList()) }
                    val playerViewModel: com.playme.player.feature.player.vm.PlayerViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel()

                    val video = activeVideo
                    if (video != null) {
                        key(video.id) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val playerEngine = remember(video.id) {
                                com.playme.player.core.player.UniversalPlayerEngine(context)
                            }

                            androidx.compose.runtime.DisposableEffect(video.id, playerEngine) {
                                playerViewModel.attachEngine(playerEngine)
                                playerViewModel.setInitialVideo(video, activePlaylist.ifEmpty { listOf(video) })
                                onDispose {
                                    playerEngine.release()
                                }
                            }

                            com.playme.player.feature.player.ui.PlayerScreen(
                                viewModel = playerViewModel,
                                onClose = {
                                    playerViewModel.onAction(com.playme.player.feature.player.model.PlayerAction.Pause)
                                    activeVideo = null
                                }
                            )
                        }
                    } else {
                        when (currentTab) {
                            MainNavTab.VIDEO -> {
                                LibraryScreen(
                                    onVideoClick = { clickedVideo, playlist ->
                                        activePlaylist = playlist
                                        playerViewModel.prepareVideo(clickedVideo, playlist)
                                        activeVideo = clickedVideo
                                    }
                                )
                            }
                            else -> {
                                MainTabPlaceholderView(tab = currentTab)
                            }
                        }

                        // Floating Glass Bottom Navigation Dock
                        BottomNavBar(
                            activeTab = currentTab,
                            onTabSelected = { currentTab = it },
                            onBigPlayClick = { /* Quick play */ },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
