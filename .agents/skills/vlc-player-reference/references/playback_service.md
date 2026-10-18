# VLC Background Playback Service & Audio Focus Reference

Reference source: [`PlaybackService.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/PlaybackService.kt)

---

## 1. Foreground Service Architecture (Android 14+ / SDK 34–35)

Under modern Android requirements:
* Service must declare `android:foregroundServiceType="mediaPlayback"` in `AndroidManifest.xml`.
* When entering background playback, invoke:
  ```kotlin
  ServiceCompat.startForeground(
      this,
      NOTIFICATION_ID,
      notification,
      if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
  )
  ```

---

## 2. Audio Focus Management

VLC uses `AudioManager.OnAudioFocusChangeListener` to pause/resume playback during phone calls or other audio apps:

```kotlin
val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .build()
    )
    .setOnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Another app took full focus (e.g. phone call) -> Pause playback
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporary loss (e.g. notification chime) -> Pause or duck volume
                pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Focus returned -> Resume playback if was playing
                play()
            }
        }
    }
    .build()
```

---

## 3. Headset Unplug / Bluetooth Disconnect (`AUDIO_BECOMING_NOISY`)

Register a BroadcastReceiver for `AudioManager.ACTION_AUDIO_BECOMING_NOISY` so video playback pauses automatically when headphones are disconnected:

```kotlin
private val becomingNoisyReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            pause()
        }
    }
}
```

---

## 4. Media Notification Controls

Notification should include:
* Current video title and artist/channel.
* Media session token (`MediaSessionCompat.token`).
* Action buttons:
  - Rewind 10s
  - Play / Pause (toggles dynamically)
  - Forward 10s
  - Next track
* Style: `androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2)`.
