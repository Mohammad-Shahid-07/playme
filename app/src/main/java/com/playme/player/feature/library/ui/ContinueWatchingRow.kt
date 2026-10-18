package com.playme.player.feature.library.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.playme.player.core.model.ContinueWatchingItem

@Composable
fun ContinueWatchingRow(
    items: List<ContinueWatchingItem>,
    onItemClick: (ContinueWatchingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        // Header Row: Title + Count Badge on left, Clear button on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Continue Watching",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.01).sp
                )

                // Count Badge
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .clip(CircleShape)
                        .background(Color(0x2E6366F1)) // rgba(99, 102, 241, 0.18)
                        .border(1.dp, Color(0x4D6366F1), CircleShape) // rgba(99, 102, 241, 0.3)
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items.size.toString(),
                        color = Color(0xFFA5B4FC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 10.sp
                    )
                }
            }

            Text(
                text = "Clear",
                color = Color(0xFF8E95A5),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clickable { /* Clear history */ }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Carousel: 10px gap matching prototype
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                ContinueWatchingCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(136.dp)
            .clickable(onClick = onClick)
    ) {
        // Thumbnail Container: 136px x 72px, radius 9px, #11131E with 1px border
        Box(
            modifier = Modifier
                .width(136.dp)
                .height(72.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF11131E))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(9.dp))
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(136.dp, 72.dp)
            )

            // Duration Badge: bottom 4px, right 4px, rgba(0,0,0,0.85), 9.5px font
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xD9000000))
                    .padding(horizontal = 4.5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = item.durationFormatted,
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 11.sp
                )
            }

            // Watch Progress Bar: 2.5px track at bottom with #6366F1 -> #818CF8
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0x80000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (item.progressPercent / 100f).coerceIn(0f, 1f))
                        .height(2.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                            )
                        )
                )
            }
        }

        // Title: 13px, font-weight 500, white, margin-top 5px
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}
