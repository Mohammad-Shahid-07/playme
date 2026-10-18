package com.playme.player.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom vector icons matching ui_prototype/src/components/icons/NavIcons.tsx
 */

@Composable
fun VideoNavIcon(
    active: Boolean,
    size: Dp = 21.dp,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF38BDF8) // Clean cyan matching Image 1
    val inactiveColor = Color(0x80FFFFFF)

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val rectTopLeft = Offset(2.5f * s, 4.5f * s)
        val rectSize = Size(19f * s, 15f * s)
        val corner = CornerRadius(4f * s, 4f * s)

        if (active) {
            // Solid cyan screen
            drawRoundRect(
                color = activeColor,
                topLeft = rectTopLeft,
                size = rectSize,
                cornerRadius = corner,
                style = Fill
            )
            // Dark play triangle inside
            val playPath = Path().apply {
                moveTo(10f * s, 8.5f * s)
                lineTo(16f * s, 12f * s)
                lineTo(10f * s, 15.5f * s)
                close()
            }
            drawPath(path = playPath, color = Color(0xFF0C0E18), style = Fill)
        } else {
            // Outline screen
            drawRoundRect(
                color = inactiveColor,
                topLeft = rectTopLeft,
                size = rectSize,
                cornerRadius = corner,
                style = Stroke(width = 1.8f * s)
            )
            val playPath = Path().apply {
                moveTo(10f * s, 8.5f * s)
                lineTo(16f * s, 12f * s)
                lineTo(10f * s, 15.5f * s)
                close()
            }
            drawPath(path = playPath, color = inactiveColor, style = Fill)
        }
    }
}

@Composable
fun MusicNavIcon(
    active: Boolean,
    size: Dp = 21.dp,
    modifier: Modifier = Modifier
) {
    val color = if (active) Color(0xFF38BDF8) else Color(0x80FFFFFF)

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val stroke = Stroke(width = 1.8f * s, cap = StrokeCap.Round)

        // Headband arc
        val arcPath = Path().apply {
            moveTo(3f * s, 14f * s)
            cubicTo(3f * s, 9f * s, 7f * s, 5f * s, 12f * s, 5f * s)
            cubicTo(17f * s, 5f * s, 21f * s, 9f * s, 21f * s, 14f * s)
        }
        drawPath(path = arcPath, color = color, style = Stroke(width = 2f * s, cap = StrokeCap.Round))

        // Ear cups
        val cupCorner = CornerRadius(2.5f * s, 2.5f * s)
        drawRoundRect(
            color = color,
            topLeft = Offset(2f * s, 13f * s),
            size = Size(5f * s, 8f * s),
            cornerRadius = cupCorner,
            style = if (active) Fill else stroke
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(17f * s, 13f * s),
            size = Size(5f * s, 8f * s),
            cornerRadius = cupCorner,
            style = if (active) Fill else stroke
        )
    }
}

@Composable
fun BrowseNavIcon(
    active: Boolean,
    size: Dp = 21.dp,
    modifier: Modifier = Modifier
) {
    val color = if (active) Color(0xFF38BDF8) else Color(0x80FFFFFF)

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val path = Path().apply {
            moveTo(2.5f * s, 7.5f * s)
            lineTo(2.5f * s, 16.5f * s)
            lineTo(18.5f * s, 16.5f * s)
            lineTo(18.5f * s, 10.25f * s)
            lineTo(14.62f * s, 10.25f * s)
            lineTo(12.5f * s, 6.38f * s)
            lineTo(9.38f * s, 6.38f * s)
            lineTo(5.5f * s, 6.38f * s)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = if (active) Fill else Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun SettingsNavIcon(
    active: Boolean,
    size: Dp = 21.dp,
    modifier: Modifier = Modifier
) {
    val color = if (active) Color(0xFF38BDF8) else Color(0x80FFFFFF)

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val stroke = Stroke(width = 1.8f * s)
        val corner = CornerRadius(2f * s, 2f * s)
        val rectSize = Size(6.5f * s, 6.5f * s)

        drawRoundRect(color = color, topLeft = Offset(3.5f * s, 3.5f * s), size = rectSize, cornerRadius = corner, style = if (active) Fill else stroke)
        drawRoundRect(color = color, topLeft = Offset(14f * s, 3.5f * s), size = rectSize, cornerRadius = corner, style = stroke)
        drawRoundRect(color = color, topLeft = Offset(3.5f * s, 14f * s), size = rectSize, cornerRadius = corner, style = stroke)
        drawRoundRect(color = color, topLeft = Offset(14f * s, 14f * s), size = rectSize, cornerRadius = corner, style = if (active) Fill else stroke)
    }
}

@Composable
fun SortSlidersIcon(
    color: Color = Color(0xFFCBD5E1),
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val strokeWidth = 1.8f * s

        drawLine(color = color, start = Offset(4f * s, 7f * s), end = Offset(20f * s, 7f * s), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawCircle(color = Color(0xFF0C0E18), radius = 2.5f * s, center = Offset(8f * s, 7f * s))
        drawCircle(color = color, radius = 2.5f * s, center = Offset(8f * s, 7f * s), style = Stroke(width = strokeWidth))

        drawLine(color = color, start = Offset(4f * s, 17f * s), end = Offset(20f * s, 17f * s), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawCircle(color = Color(0xFF0C0E18), radius = 2.5f * s, center = Offset(16f * s, 17f * s))
        drawCircle(color = color, radius = 2.5f * s, center = Offset(16f * s, 17f * s), style = Stroke(width = strokeWidth))
    }
}

@Composable
fun GridViewIcon(
    color: Color = Color(0xFFCBD5E1),
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val style = Stroke(width = 1.8f * s)
        val corner = CornerRadius(2f * s, 2f * s)

        drawRoundRect(color = color, topLeft = Offset(3.5f * s, 3.5f * s), size = Size(6.5f * s, 6.5f * s), cornerRadius = corner, style = style)
        drawRoundRect(color = color, topLeft = Offset(14f * s, 3.5f * s), size = Size(6.5f * s, 6.5f * s), cornerRadius = corner, style = style)
        drawRoundRect(color = color, topLeft = Offset(3.5f * s, 14f * s), size = Size(6.5f * s, 6.5f * s), cornerRadius = corner, style = style)
        drawRoundRect(color = color, topLeft = Offset(14f * s, 14f * s), size = Size(6.5f * s, 6.5f * s), cornerRadius = corner, style = style)
    }
}

@Composable
fun ListViewIcon(
    color: Color = Color(0xFFCBD5E1),
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val strokeWidth = 1.8f * s

        drawLine(color = color, start = Offset(8.5f * s, 6f * s), end = Offset(20.5f * s, 6f * s), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(8.5f * s, 12f * s), end = Offset(20.5f * s, 12f * s), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(8.5f * s, 18f * s), end = Offset(20.5f * s, 18f * s), strokeWidth = strokeWidth, cap = StrokeCap.Round)

        drawCircle(color = color, radius = 1.2f * s, center = Offset(4.5f * s, 6f * s))
        drawCircle(color = color, radius = 1.2f * s, center = Offset(4.5f * s, 12f * s))
        drawCircle(color = color, radius = 1.2f * s, center = Offset(4.5f * s, 18f * s))
    }
}

@Composable
fun CheckmarkIcon(
    color: Color = Color(0xFF818CF8),
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val path = Path().apply {
            moveTo(4f * s, 12f * s)
            lineTo(9f * s, 17f * s)
            lineTo(20f * s, 6f * s)
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
