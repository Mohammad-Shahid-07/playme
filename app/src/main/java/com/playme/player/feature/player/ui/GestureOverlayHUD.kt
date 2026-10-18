package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.feature.player.model.GestureType

@Composable
fun GestureOverlayHUD(
    activeGesture: GestureType,
    gestureValue: Float,
    isSpeedBoostActive: Boolean,
    currentPosMs: Long,
    durationMs: Long,
    onSelectBoostSpeed: (Float) -> Unit,
    showLockedAlert: Boolean = false,
    boostSpeed: Float = 2.0f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Locked Alert Toast
        GestureLockedToast(
            visible = showLockedAlert,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
        )

        // 2. Quick Speed Boost Card (Center Top)
        QuickSpeedBoostHUD(
            isActive = isSpeedBoostActive,
            boostSpeed = boostSpeed,
            onSelectBoostSpeed = onSelectBoostSpeed,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
        )

        // 3. Brightness Pill (Left Edge)
        AnimatedVisibility(
            visible = activeGesture == GestureType.BRIGHTNESS,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
        ) {
            VerticalGestureCapsule(
                icon = Icons.Rounded.BrightnessMedium,
                valuePercent = gestureValue,
                label = "${(gestureValue * 100).toInt()}%"
            )
        }

        // 4. Volume Pill (Right Edge)
        AnimatedVisibility(
            visible = activeGesture == GestureType.VOLUME,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
        ) {
            VerticalGestureCapsule(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                valuePercent = gestureValue,
                label = "${(gestureValue * 100).toInt()}%"
            )
        }

        // 5. Screen Gesture Seek Floating Pill (Center)
        ScreenGestureSeekHUD(
            isActive = activeGesture == GestureType.SEEK,
            deltaMs = gestureValue.toLong(),
            currentPosMs = currentPosMs,
            durationMs = durationMs,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun VerticalGestureCapsule(
    icon: ImageVector,
    valuePercent: Float,
    label: String
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(140.dp)
            .shadow(16.dp, RoundedCornerShape(21.dp), spotColor = Color(0x99000000))
            .clip(RoundedCornerShape(21.dp))
            .background(Color(0xD90E111D))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(21.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            // Vertical Track Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(valuePercent.coerceIn(0f, 1f))
                        .clip(CircleShape)
                        .background(Color(0xFF818CF8))
                )
            }

            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}
