package com.playme.player.feature.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainTabPlaceholderView(
    tab: MainNavTab,
    modifier: Modifier = Modifier
) {
    val (icon, title, desc) = when (tab) {
        MainNavTab.MUSIC -> Triple(
            Icons.Rounded.Headphones,
            "Music Player",
            "Hi-Res Lossless Audio renderer with 5-band Equalizer and hardware Bass Boost."
        )
        MainNavTab.BROWSE -> Triple(
            Icons.Rounded.FolderOpen,
            "Media Library",
            "Browse videos, folders, internal storage, DLNA/SMB shares, and Private Vault."
        )
        MainNavTab.SETTINGS -> Triple(
            Icons.Rounded.Tune,
            "Settings & Audio FX",
            "Configure decoders, +20dB Super Volume Boost, subtitle styling, and gestures."
        )
        MainNavTab.VIDEO -> Triple(
            Icons.Rounded.Headphones,
            "",
            ""
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080910))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x8C0E101C))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0x336366F1), Color(0x33A855F7))
                        )
                    )
                    .border(1.dp, Color(0x66757BF8), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF757BF8),
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = desc,
                color = Color(0xA6FFFFFF),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x2E6366F1))
                    .border(1.dp, Color(0x596366F1), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVE SCREEN: ${tab.name}",
                    color = Color(0xFFA5B4FC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
