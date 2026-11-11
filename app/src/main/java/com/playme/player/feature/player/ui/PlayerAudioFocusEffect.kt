package com.playme.player.feature.player.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.playme.player.feature.player.model.PlayerAction
import com.playme.player.feature.player.vm.PlayerViewModel

/**
 * Single audio-focus and noisy-headset owner for both ExoPlayer and LibVLC.
 *
 * Uses API-26+ AudioFocusRequest with an explicit main-thread Handler so focus
 * callbacks safely read Compose state and emit MVI actions without conflicting
 * with ExoPlayer's internal AudioFocusManager.
 */
@Composable
fun PlayerAudioFocusEffect(viewModel: PlayerViewModel, isPlaying: Boolean) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val audioManager = remember(appContext) {
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    val playingState by rememberUpdatedState(isPlaying)
    val latestViewModel by rememberUpdatedState(viewModel)
    val hasAudioFocus = remember { mutableStateOf(false) }
    val pausedByFocus = remember { mutableStateOf(false) }

    val focusRequest = remember(audioManager) {
        audioManager?.let {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()

            val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        hasAudioFocus.value = false
                        pausedByFocus.value = false
                        latestViewModel.onAction(PlayerAction.Pause)
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        hasAudioFocus.value = false
                        if (playingState) pausedByFocus.value = true
                        latestViewModel.onAction(PlayerAction.Pause)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        hasAudioFocus.value = true
                        if (pausedByFocus.value) {
                            pausedByFocus.value = false
                            latestViewModel.onAction(PlayerAction.Play)
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Unit
                }
            }

            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener(focusListener, Handler(Looper.getMainLooper()))
                .build()
        }
    }

    fun requestFocusIfNeeded(am: AudioManager, request: AudioFocusRequest) {
        if (hasAudioFocus.value) return
        val granted = am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        hasAudioFocus.value = granted
        if (!granted) {
            pausedByFocus.value = false
            latestViewModel.onAction(PlayerAction.Pause)
        }
    }

    DisposableEffect(audioManager, focusRequest) {
        val am = audioManager
        val request = focusRequest
        if (am == null || request == null) return@DisposableEffect onDispose { }

        if (playingState) {
            pausedByFocus.value = false
            requestFocusIfNeeded(am, request)
        }

        onDispose {
            hasAudioFocus.value = false
            pausedByFocus.value = false
            runCatching { am.abandonAudioFocusRequest(request) }
        }
    }

    LaunchedEffect(audioManager, focusRequest, isPlaying) {
        val am = audioManager ?: return@LaunchedEffect
        val request = focusRequest ?: return@LaunchedEffect
        if (isPlaying && !hasAudioFocus.value) {
            pausedByFocus.value = false
            requestFocusIfNeeded(am, request)
        }
    }

    DisposableEffect(appContext) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    pausedByFocus.value = false
                    latestViewModel.onAction(PlayerAction.Pause)
                }
            }
        }
        // ContextCompat handles RECEIVER_NOT_EXPORTED compatibly on API 26-32 and enforces it on 33+.
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { runCatching { appContext.unregisterReceiver(receiver) } }
    }
}
