package com.playme.player.feature.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

val BOOST_PRESETS = listOf(0.25f, 0.5f, 0.75f, 1.5f, 2.0f, 3.0f, 4.0f)

@Composable
fun QuickSpeedBoostHUD(
    isActive: Boolean,
    boostSpeed: Float,
    onSelectBoostSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeIndex = BOOST_PRESETS.indices.minByOrNull {
        abs(BOOST_PRESETS[it] - boostSpeed)
    } ?: 4
    val activeFraction = activeIndex.toFloat() / (BOOST_PRESETS.size - 1)
    val hudShape = RoundedCornerShape(20.dp)

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 390.dp)
                .fillMaxWidth(0.92f)
                .shadow(28.dp, hudShape, spotColor = Color(0x88000000))
                .clip(hudShape)
                .background(Color(0xDF0A0D18))
                .border(1.dp, Color(0x333B82F6), hudShape)
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Header: Lightning Bolt + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Playback Speed",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                }

                // 2. Presets Row
                QuickSpeedBoostPresetsRow(
                    activeIndex = activeIndex,
                    onSelectBoostSpeed = onSelectBoostSpeed
                )

                // 3. Slider Track Zone with Dots & Glowing Thumb
                QuickSpeedBoostTrack(
                    activeIndex = activeIndex,
                    activeFraction = activeFraction,
                    onSelectBoostSpeed = onSelectBoostSpeed
                )

                // 4. Footer: Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${formatBoostSpeed(boostSpeed)} speed playing",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
