package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.designsystem.PlayMeColors
import kotlin.math.abs

@Composable
fun ScreenGestureSeekHUD(
    isSeeking: Boolean,
    deltaSeconds: Int,
    targetPositionMs: Long,
    durationMs: Long,
    showLockedToast: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Locked Notice
        AnimatedVisibility(
            visible = showLockedToast,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PlayMeColors.GlassElevated)
                    .border(1.dp, PlayMeColors.AmberBoost.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = PlayMeColors.AmberBoost,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Controls Locked",
                    color = PlayMeColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 2. Floating Seek Pill
        AnimatedVisibility(
            visible = isSeeking,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            val isForward = deltaSeconds >= 0
            val sign = if (isForward) "+" else "-"
            val absSeconds = abs(deltaSeconds)
            val mins = absSeconds / 60
            val secs = absSeconds % 60
            val formattedDelta = if (mins > 0) "$sign${mins}m ${secs}s" else "$sign${secs}s"

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PlayMeColors.GlassElevated)
                    .border(1.dp, PlayMeColors.GlassBorderHover, CircleShape)
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isForward) Icons.Rounded.FastForward else Icons.Rounded.FastRewind,
                    contentDescription = null,
                    tint = if (isForward) PlayMeColors.ElectricViolet else PlayMeColors.LavenderText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = formattedDelta,
                    color = if (isForward) PlayMeColors.ElectricViolet else PlayMeColors.LavenderText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(14.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(PlayMeColors.GlassBorder)
                )
                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = formatMs(targetPositionMs),
                    color = PlayMeColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " / ${formatMs(durationMs)}",
                    color = PlayMeColors.TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
