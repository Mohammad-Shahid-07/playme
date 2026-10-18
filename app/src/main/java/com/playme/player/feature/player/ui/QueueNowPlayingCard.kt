package com.playme.player.feature.player.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun QueueNowPlayingCard(
    video: VideoItem?,
    modifier: Modifier = Modifier
) {
    if (video == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "h1")
    val h2 by infiniteTransition.animateFloat(0.7f, 0.3f, infiniteRepeatable(tween(550, easing = LinearEasing), RepeatMode.Reverse), label = "h2")
    val h3 by infiniteTransition.animateFloat(0.3f, 0.9f, infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse), label = "h3")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x2E6366F1))
            .border(1.dp, Color(0x406366F1), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        // Tag + Equalizer Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF00D287)))
                Text("NOW PLAYING", color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(14.dp)) {
                Box(modifier = Modifier.width(3.dp).height((h1 * 14).dp).background(Color(0xFF818CF8), CircleShape))
                Box(modifier = Modifier.width(3.dp).height((h2 * 14).dp).background(Color(0xFF818CF8), CircleShape))
                Box(modifier = Modifier.width(3.dp).height((h3 * 14).dp).background(Color(0xFF818CF8), CircleShape))
            }
        }

        // Hero Content
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(width = 68.dp, height = 44.dp).clip(RoundedCornerShape(8.dp))) {
                VideoThumbnailImage(video = video, contentScale = ContentScale.Crop)
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp).background(Color(0xD9000000), RoundedCornerShape(3.dp)).padding(horizontal = 3.dp, vertical = 1.dp)) {
                    Text(video.durationFormatted, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(video.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${video.parentFolder.ifEmpty { "Videos" }} · ${video.resolution}", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }
    }
}
