package com.playme.player.feature.player.ui

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel

@Composable
fun PlayerSystemSyncEffect(
    viewModel: PlayerViewModel,
    brightnessPercent: Float,
    volumePercent: Float
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    // Initialize volume from device system volume
    LaunchedEffect(Unit) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            viewModel.onAction(PlayerAction.SetVolume(cur.toFloat() / max.toFloat()))
        }
    }

    // Sync in-player volume updates to device system volume
    LaunchedEffect(volumePercent) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val target = (volumePercent * max).toInt().coerceIn(0, max)
            try {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            } catch (_: SecurityException) {}
        }
    }

    // Sync screen brightness to window
    LaunchedEffect(brightnessPercent) {
        activity?.window?.let { window ->
            val lp = window.attributes
            lp.screenBrightness = brightnessPercent.coerceIn(0.05f, 1f)
            window.attributes = lp
        }
    }

    // Reset window brightness to auto on player exit
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.let { window ->
                val lp = window.attributes
                lp.screenBrightness = -1f
                window.attributes = lp
            }
        }
    }
}
