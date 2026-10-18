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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.FolderItem
import com.playme.player.core.model.ViewMode

@Composable
fun FolderCardItem(
    folder: FolderItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {},
    viewMode: ViewMode = ViewMode.LIST,
    modifier: Modifier = Modifier
) {
    if (viewMode == ViewMode.GRID) {
        // Grid Layout matching FolderList.css (.folder-card-item is-grid-layout)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x09FFFFFF))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(11.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${folder.videoCount} videos • ${folder.totalSizeFormatted}",
                color = Color(0xFF8E95A5),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // List Layout matching FolderList.css (.folder-card-item)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x09FFFFFF))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(11.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
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

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Folder Options",
                    tint = Color(0x61FFFFFF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
