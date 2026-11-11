package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.playme.player.core.util.formatDelaySeconds

@Composable
fun SettingsSyncSubView(
    title: String,
    delayMs: Long,
    isAudio: Boolean,
    onAdjustDelay: (Long) -> Unit,
    onReset: () -> Unit = { onAdjustDelay(-delayMs) },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val delayFormatted = formatDelaySeconds(delayMs, includePlus = true, spaceBeforeUnit = true)
    val hintText = when {
        delayMs == 0L && isAudio -> "Audio & Video are in perfect sync"
        delayMs == 0L -> "Subtitles are in perfect sync"
        delayMs > 0L && isAudio -> "Audio is delayed behind video"
        delayMs > 0L -> "Subtitles are delayed"
        isAudio -> "Audio plays ahead of video"
        else -> "Subtitles appear early"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSubViewHeader(title = title, onBack = onBack)

        Column(modifier = Modifier.padding(12.dp)) {
            // Big Visual Sync Stepper Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x09FFFFFF))
                    .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isAudio) "CURRENT DELAY" else "CURRENT SUBTITLE SYNC",
                        color = Color(0xFF818CF8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = delayFormatted,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = hintText,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Steppers Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAudio) {
                            SyncStepButton(label = "-0.5s", onClick = { onAdjustDelay(-500L) }, modifier = Modifier.weight(1f))
                            SyncStepButton(label = "-0.1s", onClick = { onAdjustDelay(-100L) }, modifier = Modifier.weight(1f))
                            SyncResetButton(onClick = onReset, modifier = Modifier.weight(1.2f))
                            SyncStepButton(label = "+0.1s", onClick = { onAdjustDelay(100L) }, modifier = Modifier.weight(1f))
                            SyncStepButton(label = "+0.5s", onClick = { onAdjustDelay(500L) }, modifier = Modifier.weight(1f))
                        } else {
                            SyncStepButton(label = "-0.2s", onClick = { onAdjustDelay(-200L) }, modifier = Modifier.weight(1f))
                            SyncResetButton(onClick = onReset, modifier = Modifier.weight(1.2f))
                            SyncStepButton(label = "+0.2s", onClick = { onAdjustDelay(200L) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStepButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x12FFFFFF))
            .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SyncResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x336366F1))
            .border(1.dp, Color(0x666366F1), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Reset",
            color = Color(0xFFC7D2FE),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}
