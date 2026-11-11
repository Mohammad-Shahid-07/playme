package com.playme.player.feature.player.ui

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Job
import kotlin.math.abs

internal enum class DragAxis {
    NONE,
    VERTICAL_BRIGHTNESS,
    VERTICAL_VOLUME,
    HORIZONTAL_SEEK
}

/**
 * Mutable state for a single pointer gesture. Lives for the whole pointer-input
 * session so ongoing swipes are never restarted by recomposition.
 */
internal class PlayerGestureRuntime {

    var longPressJob: Job? = null
    var singleTapJob: Job? = null

    var pointerDown = false
    var moved = false
    var boostActive = false
    var dragging = false
    var axis = DragAxis.NONE

    var downX = 0f
    var downY = 0f
    var width = 1f
    var height = 1f

    var startVolume = 0f
    var startBrightness = 0f
    var startPositionMs = 0L
    var seekTargetMs = 0L

    var lastTapUpTime = 0L

    var boostBaseIndex = 0
    var boostIndex = 0

    fun onDown(position: Offset, values: PlayerGestureValues, viewWidth: Float, viewHeight: Float) {
        singleTapJob?.cancel()
        longPressJob?.cancel()

        pointerDown = true
        moved = false
        boostActive = false
        dragging = false
        axis = DragAxis.NONE

        downX = position.x
        downY = position.y
        width = viewWidth.coerceAtLeast(1f)
        height = viewHeight.coerceAtLeast(1f)

        startVolume = values.volume
        startBrightness = values.brightness
        startPositionMs = values.positionMs
        seekTargetMs = values.positionMs

        boostBaseIndex = nearestBoostIndex(values.boostSpeed)
        boostIndex = boostBaseIndex
    }
}

internal fun nearestBoostIndex(speed: Float): Int =
    BOOST_PRESETS.indices.minByOrNull { abs(BOOST_PRESETS[it] - speed) } ?: 0
