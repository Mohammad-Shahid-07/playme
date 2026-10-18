package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.feature.player.model.GestureType

@Composable
fun GestureOverlayHUD(
    activeGesture: GestureType,
    gestureValue: Float,
    isSpeedBoostActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. 2x Speed Boost Pill (Center Top)
        AnimatedVisibility(
            visible = isSpeedBoostActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF6366F1))
                    .clip(CircleShape)
                    .background(Color(0xE60E111D))
                    .border(1.dp, Color(0x4D6366F1), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "2X FASTPLAY",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 2. Brightness Pill (Left Edge)
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

        // 3. Volume Pill (Right Edge)
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

        // 4. Scrub Seek Badge (Center)
        AnimatedVisibility(
            visible = activeGesture == GestureType.SEEK,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .shadow(20.dp, RoundedCornerShape(14.dp), spotColor = Color(0x99000000))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xF00E111D))
                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val sign = if (gestureValue >= 0) "+" else ""
                    Text(
                        text = "$sign${gestureValue.toInt()}s",
                        color = Color(0xFF818CF8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Swipe to Seek",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
