package com.playme.player

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.playme.player.core.designsystem.PlayMeDesign
import com.playme.player.core.designsystem.PlayMeTheme
import com.playme.player.core.model.VideoItem
import com.playme.player.core.player.PlayMePlayerController
import com.playme.player.feature.library.LibraryScreen
import com.playme.player.feature.pip.FloatingPlayerService
import com.playme.player.feature.pip.PipHelper
import com.playme.player.feature.player.PlayerScreen
import org.koin.android.ext.android.inject

@OptIn(UnstableApi::class)
class MainActivity : ComponentActivity() {

    private val playerController: PlayMePlayerController by inject()
    private var isCurrentlyInPlayerScreen: Boolean = false
    private val openPlayerEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        // VLC pattern (BaseActivity.kt:87): enableEdgeToEdge() eliminates letterboxing
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestNotificationPermission()

        setContent {
            PlayMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PlayMeDesign.ObsidianDark
                ) {
                    val navController = rememberNavController()

                    // Check if opened from outside via Intent (e.g. WhatsApp / Browser / File Manager)
                    LaunchedEffect(Unit) {
                        intent?.data?.let { uri ->
                            val displayName = resolveDisplayName(uri)
                            playerController.playUri(uri, displayName)
                            navController.navigate("player")
                        }
                    }

                    LaunchedEffect(Unit) {
                        openPlayerEvent.collect {
                            navController.navigate("player") {
                                launchSingleTop = true
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "library",
                        enterTransition = {
                            slideInHorizontally(tween(320)) { it } + fadeIn(tween(240))
                        },
                        exitTransition = {
                            slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(200))
                        },
                        popEnterTransition = {
                            slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(200))
                        },
                        popExitTransition = {
                            slideOutHorizontally(tween(320)) { it } + fadeOut(tween(240))
                        }
                    ) {
                        composable("library") {
                            isCurrentlyInPlayerScreen = false
                            LibraryScreen(
                                onVideoClick = { video, playlist ->
                                    val idx = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
                                    val resumePos = if (video.lastPlayedPositionMs > 2000L && (video.durationMs <= 0L || video.durationMs - video.lastPlayedPositionMs > 5000L)) {
                                        video.lastPlayedPositionMs
                                    } else {
                                        0L
                                    }
                                    playerController.playQueue(playlist, idx, startPositionMs = resumePos)
                                    navController.navigate("player")
                                }
                            )
                        }

                        composable("player") {
                            isCurrentlyInPlayerScreen = true
                            PlayerScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onPipRequest = {
                                    PipHelper.enterPipMode(this@MainActivity)
                                },
                                onFloatingWindowRequest = {
                                    launchFloatingWindow()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter PiP when home is pressed while video is playing (unless background audio play is selected)
        try {
            if (isCurrentlyInPlayerScreen && playerController.exoPlayer.isPlaying && !playerController.uiState.value.isBackgroundPlayEnabled) {
                PipHelper.enterPipMode(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission for pop-up player", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            val intent = Intent(this, FloatingPlayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            val displayName = resolveDisplayName(uri)
            playerController.playUri(uri, displayName)
            openPlayerEvent.tryEmit(Unit)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun resolveDisplayName(uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = cursor.getString(index)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return name ?: uri.lastPathSegment ?: "Media Playback"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up player when application activity finishes (unless background audio play is selected)
        if (isFinishing && !playerController.uiState.value.isBackgroundPlayEnabled) {
            playerController.release()
        }
    }
}

