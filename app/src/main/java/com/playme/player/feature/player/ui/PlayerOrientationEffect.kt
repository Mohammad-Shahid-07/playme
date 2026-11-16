package com.playme.player.feature.player.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.OrientationEventListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private const val PORTRAIT_TOLERANCE_DEG = 25
private const val LANDSCAPE_MIN_DEG = 65
private const val LANDSCAPE_MAX_DEG = 115
private const val REV_LANDSCAPE_MIN_DEG = 245
private const val REV_LANDSCAPE_MAX_DEG = 295

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Synchronizes hardware sensor orientation, manual Rotate/Fullscreen button overrides,
 * and transient system bars for [PlayerScreen].
 */
@Composable
internal fun PlayerOrientationEffect(
    isFullscreen: Boolean,
    orientationOverride: Boolean?,
    onSyncConfigurationOrientation: (isLandscape: Boolean) -> Unit,
    onClearOrientationOverride: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context.findActivity()
    val isConfigLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val latestSync by rememberUpdatedState(onSyncConfigurationOrientation)
    val latestClear by rememberUpdatedState(onClearOrientationOverride)
    val latestOverride by rememberUpdatedState(orientationOverride)

    LaunchedEffect(isConfigLandscape) {
        latestSync(isConfigLandscape)
    }

    LaunchedEffect(isFullscreen, orientationOverride, isConfigLandscape, activity, view) {
        val act = activity ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(act.window, view)

        if (!act.isInPictureInPictureMode) {
            val desiredOrientation = when (orientationOverride) {
                true -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                false -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                null -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
            if (act.requestedOrientation != desiredOrientation) {
                act.requestedOrientation = desiredOrientation
            }
        }

        if (isFullscreen || isConfigLandscape) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Release a manual Rotate/Back lock once the user physically rotates to match it.
    val hasOverride = orientationOverride != null
    DisposableEffect(context, hasOverride) {
        if (!hasOverride) {
            onDispose {}
        } else {
            val listener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(angle: Int) {
                    if (angle == ORIENTATION_UNKNOWN) return
                    val physicalLandscape = when {
                        angle in LANDSCAPE_MIN_DEG..LANDSCAPE_MAX_DEG ||
                            angle in REV_LANDSCAPE_MIN_DEG..REV_LANDSCAPE_MAX_DEG -> true
                        angle <= PORTRAIT_TOLERANCE_DEG ||
                            angle >= 360 - PORTRAIT_TOLERANCE_DEG -> false
                        else -> return
                    }
                    val target = latestOverride ?: return
                    if (physicalLandscape == target) {
                        latestClear()
                    }
                }
            }
            if (listener.canDetectOrientation()) {
                listener.enable()
            }
            onDispose {
                listener.disable()
            }
        }
    }

    DisposableEffect(activity, view) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}
