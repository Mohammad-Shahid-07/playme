package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerSideDock(
    isMuted: Boolean,
    isLocked: Boolean,
    isAbActive: Boolean,
    onToggleMute: () -> Unit,
    onToggleLock: () -> Unit,
    onSnapshot: () -> Unit,
    onToggleAbLoop: () -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = CircleShape, spotColor = Color(0x99000000))
            .clip(CircleShape)
            .background(Color(0xD10E111D))
            .border(1.dp, Color(0x17FFFFFF), CircleShape)
            .padding(vertical = 5.dp, horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 1. Mute Button
            SideDockIconButton(
                icon = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                isActive = isMuted,
                onClick = onToggleMute
            )

            DockDivider()

            // 2. Lock Button
            SideDockIconButton(
                icon = if (isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                contentDescription = if (isLocked) "Unlock" else "Lock",
                isActive = isLocked,
                onClick = onToggleLock
            )

            DockDivider()

            // 3. Snapshot Button
            SideDockIconButton(
                icon = Icons.Rounded.PhotoCamera,
                contentDescription = "Capture Snapshot",
                isActive = false,
                onClick = onSnapshot
            )

            DockDivider()

            // 4. A-B Loop Button (Text Button)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleAbLoop),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A-B",
                    color = if (isAbActive) Color(0xFF818CF8) else Color(0xCCFFFFFF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            DockDivider()

            // 5. Rotate / Fullscreen Button
            SideDockIconButton(
                icon = Icons.Rounded.ScreenRotation,
                contentDescription = "Rotate",
                isActive = false,
                onClick = onRotate
            )
        }
    }
}

@Composable
private fun SideDockIconButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) Color(0xFF818CF8) else Color(0xCCFFFFFF),
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun DockDivider() {
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(1.dp)
            .background(Color(0x14FFFFFF))
    )
}
