package com.playme.player.feature.player

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.provider.OpenableColumns
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.content.ContentValues
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.playme.player.core.model.AspectRatioMode
import com.playme.player.core.player.DecoderMode
import com.playme.player.core.player.PlayMePlayerController
import com.playme.player.core.repository.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    onPipRequest: () -> Unit,
    onFloatingWindowRequest: () -> Unit,
    playerController: PlayMePlayerController = koinInject(),
    videoRepository: VideoRepository = koinInject()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by playerController.uiState.collectAsState()

    var isHudVisible by remember { mutableStateOf(true) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }

    fun takeScreenshot() {
        val pv = playerViewRef ?: return
        val surfaceView = pv.videoSurfaceView as? SurfaceView
        val textureView = pv.videoSurfaceView as? TextureView

        val onBitmapReady: (Bitmap) -> Unit = { bitmap ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fileName = "PlayMe_${System.currentTimeMillis()}.jpg"
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PlayMe")
                        }
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Saved snapshot to Pictures/PlayMe", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (textureView != null) {
            val bm = textureView.bitmap
            if (bm != null) onBitmapReady(bm)
        } else if (surfaceView != null && surfaceView.holder.surface.isValid) {
            val bm = Bitmap.createBitmap(surfaceView.width.coerceAtLeast(1), surfaceView.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            PixelCopy.request(
                surfaceView,
                bm,
                { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        onBitmapReady(bm)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
    }

    // Hardware and gesture Back navigation
    BackHandler(enabled = uiState.isQueueDrawerOpen) {
        playerController.closeQueueDrawer()
    }

    BackHandler(enabled = uiState.isSidebarOpen) {
        playerController.closeSidebar()
    }

    BackHandler(enabled = uiState.isScreenLocked) {
        Toast.makeText(context, "Screen is locked • Tap lock to unlock", Toast.LENGTH_SHORT).show()
    }

    BackHandler(enabled = !uiState.isSidebarOpen && !uiState.isQueueDrawerOpen && !uiState.isScreenLocked) {
        if (!uiState.isBackgroundPlayEnabled) {
            playerController.exoPlayer.pause()
        }
        onBackClick()
    }

    // Display error message toast if playback fails
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Auto-hide HUD controls after 3.5 seconds of user inactivity (unless paused or sidebar is open)
    LaunchedEffect(isHudVisible, uiState.isPlaying, uiState.isSidebarOpen) {
        if (isHudVisible && uiState.isPlaying && !uiState.isScreenLocked && !uiState.isSidebarOpen) {
            delay(3500)
            isHudVisible = false
        }
    }

    // Register BecomingNoisyReceiver to auto-pause when headphones disconnect (VLC-style)
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    if (playerController.exoPlayer.isPlaying) {
                        playerController.exoPlayer.pause()
                        Toast.makeText(context, "Headphones disconnected • Paused", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(receiver, filter)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Enter immersive fullscreen & sensor orientation
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        onDispose {
            // Restore portrait & system bars on exit
            val currentWindow = activity?.window
            if (currentWindow != null) {
                val insetsController = WindowCompat.getInsetsController(currentWindow, currentWindow.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // Stop playback on exit unless background play is explicitly enabled
            if (!uiState.isBackgroundPlayEnabled) {
                playerController.exoPlayer.pause()
            } else {
                // VLC Pattern (vlcVout.detachViews): detach video surface when continuing in background audio mode
                playerController.detachVideoSurface()
            }

            // Save last watched position to database
            uiState.currentVideo?.let { v ->
                CoroutineScope(Dispatchers.IO).launch {
                    videoRepository.saveProgress(
                        uri = v.contentUri,
                        title = v.displayName,
                        positionMs = uiState.currentPositionMs,
                        durationMs = uiState.durationMs
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Layer 1: Player Surface with Mirror Mode transform
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerController.exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = when (uiState.aspectRatioMode) {
                        AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                    }
                    playerViewRef = this
                }
            },
            update = { playerView ->
                playerViewRef = playerView
                if (playerView.player == null) {
                    playerView.player = playerController.exoPlayer
                }
                playerView.resizeMode = when (uiState.aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                }
            },
            onRelease = { playerView ->
                playerViewRef = null
                playerView.player = null
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (uiState.isMirrorMode) -1f else 1f
                }
        )

        // Night Mode Amber Eye-Comfort Filter
        if (uiState.isNightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD97706).copy(alpha = 0.18f))
            )
        }

        // Layer 2: Gesture Overlay Layer (Touches on video surface only)
        GestureOverlay(
            isLocked = uiState.isScreenLocked,
            isSidebarOpen = uiState.isSidebarOpen,
            isHudVisible = isHudVisible,
            currentPositionMs = uiState.currentPositionMs,
            durationMs = uiState.durationMs,
            isFastPlaying = uiState.isFastPlaying,
            fastPlaySpeed = uiState.fastPlaySpeed,
            onSingleTap = { isHudVisible = !isHudVisible },
            onDoubleTapSeek = { deltaMs -> playerController.seekBy(deltaMs) },
            onDoubleTapLeft = { playerController.seekBy(-10000) },
            onDoubleTapRight = { playerController.seekBy(10000) },
            onSeekConfirm = { targetMs -> playerController.seekTo(targetMs) },
            onVolumeBoostChange = { boost -> playerController.setVolumeBoost(boost) },
            onFastPlayStart = { playerController.startFastPlay() },
            onFastPlaySpeedChange = { newSpeed -> playerController.setFastPlaySpeed(newSpeed) },
            onFastPlayEnd = { playerController.stopFastPlay() },
            onPinchZoom = { isZoomIn ->
                if (isZoomIn && uiState.aspectRatioMode == AspectRatioMode.FIT) {
                    playerController.setAspectRatioMode(AspectRatioMode.ZOOM)
                } else if (!isZoomIn && uiState.aspectRatioMode != AspectRatioMode.FIT) {
                    playerController.setAspectRatioMode(AspectRatioMode.FIT)
                }
            },
            onScreenshot = { takeScreenshot() },
            onUserInteraction = { playerController.onUserInteraction() }
        )

        // Layer 3: HUD Controls Layer (Top Bar, Bottom Dock, Center Play/Pause)
        PlayerControlsHud(
            isVisible = isHudVisible,
            uiState = uiState,
            controller = playerController,
            onBackgroundClick = { isHudVisible = false },
            onBackClick = {
                if (!uiState.isBackgroundPlayEnabled) {
                    playerController.exoPlayer.pause()
                }
                onBackClick()
            },
            onPlayPauseClick = { playerController.togglePlayPause() },
            onSeekTo = { pos -> playerController.seekTo(pos) },
            onSkipForward = { playerController.seekBy(10000) },
            onSkipBackward = { playerController.seekBy(-10000) },
            onToggleLock = { playerController.toggleScreenLock() },
            onCycleAspectRatio = { playerController.cycleAspectRatio() },
            onPipClick = onPipRequest,
            onFloatingWindowClick = onFloatingWindowRequest,
            onToggleBackgroundPlay = { playerController.toggleBackgroundPlay() },
            onToggleDecoder = {
                val newMode = if (uiState.decoderMode == DecoderMode.HW) DecoderMode.SW else DecoderMode.HW
                playerController.setDecoderMode(newMode)
            },
            onScreenshotClick = { takeScreenshot() },
            onRotateClick = {
                if (activity != null) {
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ||
                        activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    ) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                }
            },
            onNextUpClick = {
                playerController.openQueueDrawer()
            }
        )

        // Layer 4: Playback Queue Drawer (Left-hand side - real thumbnails, playing indicator, reorder)
        PlaybackQueueDrawer(
            isOpen = uiState.isQueueDrawerOpen,
            uiState = uiState,
            controller = playerController,
            onClose = { playerController.closeQueueDrawer() }
        )

        // Layer 5: Settings Sidebar Drawer (Right-hand side - audio, subtitles, equalizer, speed)
        PlayerSettingsSidebar(
            isOpen = uiState.isSidebarOpen,
            activeSubView = uiState.sidebarActiveSubView,
            uiState = uiState,
            controller = playerController,
            onClose = { playerController.closeSidebar() },
            onScreenshotClick = { takeScreenshot() }
        )
    }
}
