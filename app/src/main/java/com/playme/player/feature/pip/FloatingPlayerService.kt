package com.playme.player.feature.pip

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.playme.player.MainActivity
import com.playme.player.R
import com.playme.player.core.player.PlayMePlayerController
import org.koin.android.ext.android.inject

@OptIn(UnstableApi::class)
class FloatingPlayerService : Service() {

    private val playerController: PlayMePlayerController by inject()
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var playerView: PlayerView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        initFloatingWindow()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val screenWidth = resources.displayMetrics.widthPixels
        val windowWidth = (screenWidth * 0.70f).toInt()
        val windowHeight = (windowWidth * 9 / 16)

        val params = WindowManager.LayoutParams(
            windowWidth,
            windowHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // Programmatically build the mini floating player layout
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val pView = PlayerView(this).apply {
            player = playerController.exoPlayer
            useController = false
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        playerView = pView
        rootLayout.addView(pView)

        // Overlay control bar for Close, Play/Pause, and Fullscreen
        val controlsBar = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.argb(120, 0, 0, 0))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
            }
        }

        val closeBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setColorFilter(android.graphics.Color.WHITE)
            setOnClickListener {
                playerController.exoPlayer.pause()
                stopSelf()
            }
            layoutParams = FrameLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
        }
        controlsBar.addView(closeBtn)

        val fullscreenBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_crop)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setColorFilter(android.graphics.Color.WHITE)
            setOnClickListener {
                val intent = Intent(this@FloatingPlayerService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                stopSelf()
            }
            layoutParams = FrameLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
        }
        controlsBar.addView(fullscreenBtn)

        rootLayout.addView(controlsBar)

        // Draggable touch handling
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        rootLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(rootLayout, params)
                    true
                }
                else -> false
            }
        }

        floatingView = rootLayout
        windowManager?.addView(rootLayout, params)
    }

    private fun createNotification(): Notification {
        val channelId = "floating_player_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PlayMe Floating Window",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("PlayMe Pop-up Player")
            .setContentText("Playing in floating window")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        playerView?.player = null
        playerView = null
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
    }
}
