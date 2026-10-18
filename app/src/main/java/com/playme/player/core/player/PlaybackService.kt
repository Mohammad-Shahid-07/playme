package com.playme.player.core.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.playme.player.MainActivity
import com.playme.player.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private val playerController: PlayMePlayerController by inject()
    private var mediaSession: MediaSession? = null

    private var cachedThumbnailUri: Uri? = null
    private var cachedThumbnailBitmap: Bitmap? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val playerRebuiltListener: (ExoPlayer) -> Unit = { newPlayer ->
        try {
            mediaSession?.player = newPlayer
            newPlayer.addListener(playerListener)
            updateNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val uri = playerController.uiState.value.currentVideo?.uri
            fetchThumbnailAsync(uri)
            updateNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                if (!playerController.hasNextVideo()) {
                    stopForegroundCompat(true)
                    stopSelf()
                }
            } else {
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        mediaSession = MediaSession.Builder(this, playerController.exoPlayer)
            .setId("PlayMeMediaSession")
            .setSessionActivity(pendingIntent)
            .build()

        // Configure Media3 DefaultMediaNotificationProvider with PlayMe notification icon & channel
        try {
            val provider = DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.playback_channel_name)
                .setNotificationId(NOTIFICATION_ID)
                .build()
            provider.setSmallIcon(R.drawable.ic_playme_notification)
            setMediaNotificationProvider(provider)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        playerController.exoPlayer.addListener(playerListener)
        playerController.addOnPlayerRebuiltListener(playerRebuiltListener)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle media control actions from notification buttons
        when (intent?.action) {
            ACTION_TOGGLE_PLAY_PAUSE -> {
                if (playerController.exoPlayer.isPlaying) {
                    playerController.exoPlayer.pause()
                } else {
                    playerController.exoPlayer.play()
                }
                updateNotification()
                return START_STICKY
            }
            ACTION_NEXT -> {
                playerController.playNext()
                updateNotification()
                return START_STICKY
            }
            ACTION_PREV -> {
                playerController.playPrevious()
                updateNotification()
                return START_STICKY
            }
            ACTION_REWIND -> {
                playerController.seekBy(-10000L)
                updateNotification()
                return START_STICKY
            }
            ACTION_FORWARD -> {
                playerController.seekBy(10000L)
                updateNotification()
                return START_STICKY
            }
            ACTION_STOP -> {
                playerController.exoPlayer.pause()
                stopForegroundCompat(true)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // VLC Pattern (force startForeground with MediaStyle notification)
        forceForeground()

        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun forceForeground() {
        createNotificationChannel()
        val notification = buildForegroundNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNotification() {
        try {
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchThumbnailAsync(videoUri: Uri?) {
        if (videoUri == null || videoUri == cachedThumbnailUri) return
        serviceScope.launch(Dispatchers.IO) {
            val bm = loadVideoThumbnail(videoUri)
            withContext(Dispatchers.Main) {
                cachedThumbnailUri = videoUri
                cachedThumbnailBitmap = bm
                updateNotification()
            }
        }
    }

    private fun loadVideoThumbnail(videoUri: Uri?): Bitmap? {
        if (videoUri == null) return null
        if (videoUri == cachedThumbnailUri && cachedThumbnailBitmap != null) {
            return cachedThumbnailBitmap
        }
        val bitmap = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.loadThumbnail(videoUri, android.util.Size(512, 512), null)
            } else {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(this, videoUri)
                val bm = retriever.getFrameAtTime(1000000, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()
                bm
            }
        } catch (e: Exception) {
            null
        }
        return bitmap
    }

    private fun buildForegroundNotification(): Notification {
        val currentVideo = playerController.uiState.value.currentVideo
        val title = currentVideo?.displayName ?: "PlayMe Media Player"
        val subtitle = if (currentVideo != null && currentVideo.folderName.isNotBlank()) {
            currentVideo.folderName
        } else {
            "PlayMe Player"
        }
        val isPlaying = playerController.exoPlayer.isPlaying

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_PREV },
            flags
        )
        val rewindIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_REWIND },
            flags
        )
        val playPauseIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY_PAUSE },
            flags
        )
        val forwardIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_FORWARD },
            flags
        )
        val nextIntent = PendingIntent.getService(
            this,
            5,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_NEXT },
            flags
        )
        val stopIntent = PendingIntent.getService(
            this,
            6,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_STOP },
            flags
        )

        // VLC MediaStyle Pattern with 5 actions and compact view (0 = prev, 2 = play/pause, 4 = next)
        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 2, 4)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopIntent)

        mediaSession?.let { session ->
            try {
                mediaStyle.setMediaSession(session.sessionCompatToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val coverBitmap = cachedThumbnailBitmap
        if (coverBitmap == null && currentVideo != null) {
            fetchThumbnailAsync(currentVideo.uri)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_playme_notification)
            .setColor(0xFF757BF8.toInt())
            .setColorized(true)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setLargeIcon(coverBitmap)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(stopIntent)
            .setOngoing(isPlaying)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setSilent(true)
            .setStyle(mediaStyle)
            .addAction(R.drawable.ic_notif_prev, "Previous", prevIntent) // 0
            .addAction(R.drawable.ic_notif_rewind_10, "Rewind 10s", rewindIntent) // 1
            .addAction(
                if (isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            ) // 2
            .addAction(R.drawable.ic_notif_forward_10, "Forward 10s", forwardIntent) // 3
            .addAction(R.drawable.ic_notif_next, "Next", nextIntent) // 4

        return builder.build()
    }

    private fun stopForegroundCompat(removeNotification: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(if (removeNotification) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(removeNotification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.playback_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.playback_channel_desc)
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopForegroundCompat(true)
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        playerController.removeOnPlayerRebuiltListener(playerRebuiltListener)
        playerController.exoPlayer.removeListener(playerListener)
        mediaSession?.run {
            release()
            mediaSession = null
        }
        stopForegroundCompat(true)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "playme_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_TOGGLE_PLAY_PAUSE = "com.playme.player.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_NEXT = "com.playme.player.ACTION_NEXT"
        const val ACTION_PREV = "com.playme.player.ACTION_PREV"
        const val ACTION_REWIND = "com.playme.player.ACTION_REWIND"
        const val ACTION_FORWARD = "com.playme.player.ACTION_FORWARD"
        const val ACTION_STOP = "com.playme.player.ACTION_STOP"
    }
}
