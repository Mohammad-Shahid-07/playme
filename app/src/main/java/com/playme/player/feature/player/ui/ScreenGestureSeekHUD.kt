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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.util.formatDuration
import kotlin.math.abs

private fun formatDelta(deltaSec: Long): String {
    val absSec = abs(deltaSec)
    val sign = if (deltaSec >= 0) "+" else "-"
    val hours = absSec / 3600L
    val mins = (absSec % 3600L) / 60L
    val secs = absSec % 60L
    return when {
        hours > 0L -> "${sign}${hours}h ${mins}m ${secs}s"
        mins > 0L -> "${sign}${mins}m ${secs}s"
        else -> "${sign}${secs}s"
    }
}

@Composable
fun ScreenGestureSeekHUD(
    isActive: Boolean,
    deltaMs: Long,
    targetPosMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier
) {
    val isForward = deltaMs >= 0
    val deltaSec = deltaMs / 1000L
    val safeDurationMs = durationMs.coerceAtLeast(0L)
    val clampedTargetMs = targetPosMs.coerceIn(0L, safeDurationMs)

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + scaleIn(initialScale = 0.94f),
        exit = fadeOut() + scaleOut(targetScale = 0.94f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .height(42.dp)
                .clip(CircleShape)
                .background(Color(0xCC0A0D18))
                .border(1.dp, Color(0x336366F1), CircleShape)
                .shadow(24.dp, CircleShape, spotColor = Color(0x88000000))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isForward) Icons.Rounded.FastForward else Icons.Rounded.FastRewind,
                    contentDescription = null,
                    tint = if (isForward) Color(0xFF818CF8) else Color(0xFFC084FC),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = formatDelta(deltaSec),
                    color = if (isForward) Color(0xFFA5B4FC) else Color(0xFFE9D5FF),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-0.2).sp
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Color(0x29FFFFFF))
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = formatDuration(clampedTargetMs),
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "/ ${formatDuration(safeDurationMs)}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun GestureLockedToast(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xCC0A0D18))
                .border(1.dp, Color(0x59EF4444), CircleShape)
                .shadow(16.dp, CircleShape, spotColor = Color(0x66EF4444))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Controls Locked",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
