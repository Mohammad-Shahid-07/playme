package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

internal fun formatBoostSpeed(s: Float): String {
    val hundredths = (s * 100f).roundToInt()
    return when {
        hundredths % 100 == 0 -> String.format(Locale.US, "%.1fx", hundredths / 100f)
        hundredths % 10 == 0 -> String.format(Locale.US, "%.1fx", hundredths / 100f)
        else -> String.format(Locale.US, "%.2fx", hundredths / 100f)
    }
}

@Composable
internal fun QuickSpeedBoostPresetsRow(
    activeIndex: Int,
    onSelectBoostSpeed: (Float) -> Unit
) {
    val latestOnSelect by rememberUpdatedState(onSelectBoostSpeed)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(30.dp)
    ) {
        BOOST_PRESETS.forEachIndexed { idx, preset ->
            val isSelected = idx == activeIndex
            val leftFrac = idx.toFloat() / (BOOST_PRESETS.size - 1)

            Box(
                modifier = Modifier
                    .offset(x = (maxWidth * leftFrac) - 20.dp)
                    .width(40.dp)
                    .clickable { latestOnSelect(preset) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                        .then(
                            if (isSelected) Modifier.shadow(12.dp, CircleShape, spotColor = Color(0xFF2563EB))
                            else Modifier
                        )
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatBoostSpeed(preset),
                        color = if (isSelected) Color.White else Color(0xD1FFFFFF),
                        fontSize = if (isSelected) 11.sp else 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuickSpeedBoostTrack(
    activeIndex: Int,
    activeFraction: Float,
    onSelectBoostSpeed: (Float) -> Unit
) {
    val latestOnSelect by rememberUpdatedState(onSelectBoostSpeed)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(20.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width.coerceAtLeast(1).toFloat()
                    val frac = (offset.x / width).coerceIn(0f, 1f)
                    val targetIdx = (frac * (BOOST_PRESETS.size - 1)).roundToInt()
                    latestOnSelect(BOOST_PRESETS[targetIdx.coerceIn(0, BOOST_PRESETS.size - 1)])
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.5.dp)
                .clip(CircleShape)
                .background(Color(0x29FFFFFF))
        )

        Box(
            modifier = Modifier
                .width(maxWidth * activeFraction)
                .height(3.5.dp)
                .clip(CircleShape)
                .background(Color(0xFF2563EB))
        )

        BOOST_PRESETS.forEachIndexed { idx, _ ->
            val posFrac = idx.toFloat() / (BOOST_PRESETS.size - 1)
            val isPassed = idx <= activeIndex
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth * posFrac) - 3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isPassed) Color(0xFF3B82F6) else Color(0x3DFFFFFF))
            )
        }

        Box(
            modifier = Modifier
                .offset(x = (maxWidth * activeFraction) - 7.dp)
                .size(14.dp)
                .shadow(12.dp, CircleShape, spotColor = Color(0xFF38BDF8))
                .clip(CircleShape)
                .background(Color(0xFF38BDF8))
                .border(2.dp, Color.White, CircleShape)
        )
    }
}
