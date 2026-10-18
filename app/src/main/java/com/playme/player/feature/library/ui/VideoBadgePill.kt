package com.playme.player.feature.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playme.player.core.model.BadgeVariant
import com.playme.player.core.model.VideoBadge

@Composable
fun VideoBadgePill(
    badge: VideoBadge,
    isOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = getBadgeBorderColor(badge.variant)
    val textColor = getBadgeTextColor(badge.variant)
    val bgColor = if (isOverlay) Color(0xB3000000) else Color.Transparent

    Box(
        modifier = modifier
            .height(if (isOverlay) 15.dp else 16.dp)
            .clip(RoundedCornerShape(3.5.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(3.5.dp))
            .padding(horizontal = if (isOverlay) 4.5.dp else 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badge.label,
            color = textColor,
            fontSize = if (isOverlay) 8.5.sp else 9.sp,
            fontWeight = if (isOverlay) FontWeight.Bold else FontWeight.SemiBold,
            lineHeight = 10.sp
        )
    }
}

fun getBadgeBorderColor(variant: BadgeVariant): Color = when (variant) {
    BadgeVariant.GOLD -> Color(0xFFD97706)
    BadgeVariant.BLUE -> Color(0x666366F1)
    BadgeVariant.GRAY -> Color(0x38FFFFFF)
    BadgeVariant.GREEN -> Color(0xB310B981)
}

fun getBadgeTextColor(variant: BadgeVariant): Color = when (variant) {
    BadgeVariant.GOLD -> Color(0xFFFACC15)
    BadgeVariant.BLUE -> Color(0xFFA5B4FC)
    BadgeVariant.GRAY -> Color(0xB8FFFFFF)
    BadgeVariant.GREEN -> Color(0xFF34D399)
}
