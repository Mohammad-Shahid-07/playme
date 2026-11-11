package com.playme.player.core.player

import android.content.Context
import android.view.MotionEvent
import org.videolan.libvlc.util.VLCVideoLayout

/**
 * LibVLC's layout consumes touch events, so a tap inside the video bounds never
 * reaches the Compose gesture layer above it — the HUD appears only when
 * tapping the letterbox bars. Video input belongs to the player, so this
 * variant declines every touch and lets Compose handle it.
 */
class NonConsumingVlcLayout(context: Context) : VLCVideoLayout(context) {

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = false
}
