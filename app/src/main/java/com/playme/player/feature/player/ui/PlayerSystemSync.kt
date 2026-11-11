package com.playme.player.feature.player.ui

import android.app.Activity
import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel
import kotlin.math.roundToInt

@Composable
fun PlayerSystemSyncEffect(
    viewModel: PlayerViewModel,
    brightnessPercent: Float,
    volumePercent: Float
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val activity = context as? Activity
    val audioManager = remember(appContext) {
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }
    var systemVolumeSynced by remember(audioManager) { mutableStateOf(false) }
    var lastObservedStep by remember(audioManager) { mutableIntStateOf(-1) }

    // Read device STREAM_MUSIC level before any writeback and observe hardware volume changes.
    DisposableEffect(appContext, audioManager) {
        val am = audioManager
        if (am == null) {
            onDispose {}
        } else {
            val readAndSync = {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC).coerceIn(0, max)
                if (cur != lastObservedStep || !systemVolumeSynced) {
                    lastObservedStep = cur
                    systemVolumeSynced = true
                    viewModel.onAction(PlayerAction.SyncSystemVolume(cur.toFloat() / max.toFloat()))
                }
            }
            readAndSync()

            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    readAndSync()
                }
            }
            appContext.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                observer
            )
            onDispose {
                appContext.contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    // Sync explicit in-player volume updates to device system volume.
    LaunchedEffect(volumePercent, systemVolumeSynced) {
        if (!systemVolumeSynced || volumePercent < 0f) return@LaunchedEffect
        val am = audioManager ?: return@LaunchedEffect
        if (am.isVolumeFixed) return@LaunchedEffect
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val target = (volumePercent * max).roundToInt().coerceIn(0, max)
        if (target == lastObservedStep) return@LaunchedEffect
        lastObservedStep = target
        try {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (_: SecurityException) {
        }
    }

    // Sync screen brightness only after the user overrides auto-brightness (brightnessPercent >= 0f).
    LaunchedEffect(brightnessPercent) {
        activity?.window?.let { window ->
            val lp = window.attributes
            val nextBrightness = if (brightnessPercent < 0f) {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                brightnessPercent.coerceIn(0.05f, 1f)
            }
            if (lp.screenBrightness != nextBrightness) {
                lp.screenBrightness = nextBrightness
                window.attributes = lp
            }
        }
    }

    // Reset window brightness override on player exit.
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.let { window ->
                val lp = window.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = lp
            }
        }
    }
}
