package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.FolderItem

@Composable
fun FolderCardItem(
    folder: FolderItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Exact match for FolderList.css: padding 8px 10px, radius 11px, rgba(255, 255, 255, 0.035), 1px border rgba(255, 255, 255, 0.07)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0x09FFFFFF)) // rgba(255, 255, 255, 0.035)
            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(11.dp)) // rgba(255, 255, 255, 0.07)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Folder Icon Preview: 38px x 38px, radius 9px, rgba(99, 102, 241, 0.12), border rgba(99, 102, 241, 0.22)
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0x1F6366F1))
                .border(1.dp, Color(0x386366F1), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(20.dp)
            )
        }

        // Folder Info Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Text(
                text = "${folder.videoCount} videos • ${folder.totalSizeFormatted}",
                color = Color(0xFF8E95A5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 13.sp
            )
        }

        // Options Button: 3-dots matching prototype
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onMoreClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Folder Options",
                tint = Color(0x61FFFFFF), // rgba(255, 255, 255, 0.38)
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
