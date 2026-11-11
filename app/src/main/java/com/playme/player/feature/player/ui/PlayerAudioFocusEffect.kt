package com.playme.player.feature.player.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel

/**
 * Keeps the player a good citizen of the device's audio stack.
 *
 * Two separate concerns, both required of any shipping player:
 *
 *  - **Audio focus** — another app (a call, a navigation prompt, music) takes
 *    the audio stream, so we pause instead of talking over it, and resume only
 *    if we were the one who paused.
 *  - **Becoming noisy** — headphones are unplugged or a Bluetooth headset
 *    disconnects; without this the audio jumps to the loudspeaker at full
 *    volume.
 */
@Composable
fun PlayerAudioFocusEffect(viewModel: PlayerViewModel, isPlaying: Boolean) {
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    // Read by the focus callback without restarting the effect on every tick.
    val playingState by rememberUpdatedState(isPlaying)
    val pausedByFocus = remember { mutableStateOf(false) }

    DisposableEffect(audioManager) {
        val am = audioManager ?: return@DisposableEffect onDispose { }

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .build()

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        pausedByFocus.value = playingState
                        viewModel.onAction(PlayerAction.Pause)
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        pausedByFocus.value = playingState
                        viewModel.onAction(PlayerAction.Pause)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        // Only resume what we stopped — never start playback the
                        // user had already paused.
                        if (pausedByFocus.value) {
                            pausedByFocus.value = false
                            viewModel.onAction(PlayerAction.Play)
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Unit
                }
            }
            .build()

        am.requestAudioFocus(focusRequest)
        onDispose { am.abandonAudioFocusRequest(focusRequest) }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    pausedByFocus.value = false
                    viewModel.onAction(PlayerAction.Pause)
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
}
