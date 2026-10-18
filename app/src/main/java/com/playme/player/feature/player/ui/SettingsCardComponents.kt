package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsGroupCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = Color(0xFF818CF8),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x1F141724))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0x2E6366F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, label, tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
            }
            Text(label, color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = Color(0xFF94A3B8), fontSize = 12.sp)
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color(0x66FFFFFF), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0x2E6366F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, label, tint = Color(0xFFA5B4FC), modifier = Modifier.size(16.dp))
            }
            Text(label, color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF6366F1),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

@Composable
fun DelayAdjusterRow(
    label: String,
    delayMs: Long,
    onStep: (Long) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(if (delayMs == 0L) "0ms (In Sync)" else "${delayMs}ms", color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { onStep(-50L) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Remove, "-50ms", tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0x2E6366F1)).clickable(onClick = onReset).padding(horizontal = 6.dp, vertical = 3.dp)) {
                Text("Reset", color = Color(0xFFC7D2FE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0x1FFFFFFF)).clickable { onStep(50L) }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, "+50ms", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
