package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Buffering indicator that only appears when playback has been stalled for
 * longer than 1.5s. Fast seeks and codec switches should not flash a spinner.
 */
@Composable
fun PlayerBufferingOverlay(
    isBuffering: Boolean,
    modifier: Modifier = Modifier
) {
    var showIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(isBuffering) {
        if (isBuffering) {
            delay(1500)
            showIndicator = true
        } else {
            showIndicator = false
        }
    }

    AnimatedVisibility(
        visible = showIndicator,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xCC0C0E18))
                .border(1.dp, Color(0x33757BF8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF818CF8),
                strokeWidth = 2.dp
            )
        }
    }
}
