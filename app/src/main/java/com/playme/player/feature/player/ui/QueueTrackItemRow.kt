package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.VideoItem
import com.playme.player.feature.library.ui.VideoThumbnailImage

@Composable
fun QueueTrackItemRow(
    video: VideoItem,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isPlaying) Color(0x336366F1) else Color(0x1A141724)
    val borderCol = if (isPlaying) Color(0x4D6366F1) else Color(0x14FFFFFF)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Thumbnail with Duration & Play overlay
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 40.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            VideoThumbnailImage(video = video, contentScale = ContentScale.Crop)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .background(Color(0xD9000000), RoundedCornerShape(3.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(video.durationFormatted, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = if (isPlaying) Color(0xFFC7D2FE) else Color(0xFFE2E8F0),
                fontSize = 12.5.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${video.parentFolder.ifEmpty { "Videos" }} · ${video.resolution}",
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                maxLines = 1
            )
        }

        // Quick Remove
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Close, "Remove", tint = Color(0x80FFFFFF), modifier = Modifier.size(14.dp))
        }

        // Drag Handle
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "Drag to reorder",
            tint = Color(0x40FFFFFF),
            modifier = Modifier.size(16.dp)
        )
    }
}
