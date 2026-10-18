package com.playme.player.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.playme.player.core.model.VideoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    isOpen: Boolean,
    onClose: () -> Unit,
    playlist: List<VideoItem>,
    currentVideo: VideoItem?,
    currentIndex: Int,
    isShuffle: Boolean,
    repeatMode: String,
    isAutoplay: Boolean,
    onToggleShuffle: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onToggleAutoplay: () -> Unit,
    onSelectTrack: (VideoItem) -> Unit,
    onRemoveTrack: (String) -> Unit,
    onClearQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Header: Title, Count, Tools, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Queue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x336366F1))
                            .border(1.dp, Color(0x666366F1), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("${playlist.size}", color = Color(0xFFC7D2FE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Shuffle Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isShuffle) Color(0x336366F1) else Color(0x14FFFFFF))
                            .border(1.dp, if (isShuffle) Color(0xFF6366F1) else Color.Transparent, CircleShape)
                            .clickable(onClick = onToggleShuffle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Shuffle, "Shuffle", tint = if (isShuffle) Color(0xFF818CF8) else Color(0x99FFFFFF), modifier = Modifier.size(16.dp))
                    }

                    // Repeat Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (repeatMode != "off") Color(0x336366F1) else Color(0x14FFFFFF))
                            .border(1.dp, if (repeatMode != "off") Color(0xFF6366F1) else Color.Transparent, CircleShape)
                            .clickable(onClick = onCycleRepeatMode),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = if (repeatMode == "one") Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                        Icon(icon, "Repeat", tint = if (repeatMode != "off") Color(0xFF818CF8) else Color(0x99FFFFFF), modifier = Modifier.size(16.dp))
                    }

                    // Close Button
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, "Close", tint = Color(0x99FFFFFF), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // 2. NOW PLAYING Section
            QueueNowPlayingCard(video = currentVideo ?: playlist.getOrNull(currentIndex))

            // 3. UP NEXT Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("UP NEXT", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    if (playlist.size > 1) {
                        Text(
                            text = "Clear All",
                            color = Color(0xFF818CF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onClearQueue)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Autoplay", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Switch(
                        checked = isAutoplay,
                        onCheckedChange = { onToggleAutoplay() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF6366F1),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.size(width = 36.dp, height = 20.dp)
                    )
                }
            }

            // 4. Up Next Track List
            val upcoming = playlist.filterIndexed { index, _ -> index != currentIndex }
            if (upcoming.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text("Queue is empty. Videos you add will appear here.", color = Color(0x66FFFFFF), fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(upcoming, key = { _, item -> item.id }) { _, item ->
                        QueueTrackItemRow(
                            video = item,
                            isPlaying = false,
                            onSelect = { onSelectTrack(item) },
                            onRemove = { onRemoveTrack(item.id) }
                        )
                    }
                }
            }
        }
    }
}
