package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlaybackSpeedSheet(
    isOpen: Boolean,
    onClose: () -> Unit,
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color(0xF20B0D18),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Speed, "Speed", tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                    Text("Playback Speed", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color(0x99FFFFFF), modifier = Modifier.size(18.dp))
                }
            }

            // Current Speed Pill Display
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x336366F1))
                    .border(1.dp, Color(0x666366F1), RoundedCornerShape(999.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text("${currentSpeed}x", color = Color(0xFFC7D2FE), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Slider
            Slider(
                value = currentSpeed,
                onValueChange = { onSpeedSelected((kotlin.math.round(it * 20f) / 20f).coerceIn(0.25f, 4.0f)) },
                valueRange = 0.25f..4.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF818CF8),
                    activeTrackColor = Color(0xFF6366F1),
                    inactiveTrackColor = Color(0x29FFFFFF)
                )
            )

            // Presets Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { speed ->
                    val isSelected = currentSpeed == speed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF6366F1) else Color(0x1AFFFFFF))
                            .border(1.dp, if (isSelected) Color(0xFF818CF8) else Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { onSpeedSelected(speed) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
