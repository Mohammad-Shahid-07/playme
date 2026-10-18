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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom vector icons matching ui_prototype/src/components/icons/NavIcons.tsx
 */

@Composable
fun SortSlidersIcon(
    color: Color = Color(0xFFCBD5E1),
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        val strokeWidth = 1.8f * s

        // Top line
        drawLine(
            color = color,
            start = Offset(4f * s, 7f * s),
            end = Offset(20f * s, 7f * s),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Top slider thumb circle at (8, 7)
        drawCircle(
            color = Color(0xFF0C0E18),
            radius = 2.5f * s,
            center = Offset(8f * s, 7f * s)
        )
        drawCircle(
            color = color,
            radius = 2.5f * s,
            center = Offset(8f * s, 7f * s),
            style = Stroke(width = strokeWidth)
        )

        // Bottom line
        drawLine(
            color = color,
            start = Offset(4f * s, 17f * s),
            end = Offset(20f * s, 17f * s),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Bottom slider thumb circle at (16, 17)
        drawCircle(
            color = Color(0xFF0C0E18),
            radius = 2.5f * s,
            center = Offset(16f * s, 17f * s)
        )
        drawCircle(
            color = color,
            radius = 2.5f * s,
            center = Offset(16f * s, 17f * s),
            style = Stroke(width = strokeWidth)
        )
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
        val strokeWidth = 1.8f * s
        val corner = CornerRadius(2f * s, 2f * s)
        val style = Stroke(width = strokeWidth)

        // Top-left rect
        drawRoundRect(
            color = color,
            topLeft = Offset(3.5f * s, 3.5f * s),
            size = Size(6.5f * s, 6.5f * s),
            cornerRadius = corner,
            style = style
        )
        // Top-right rect
        drawRoundRect(
            color = color,
            topLeft = Offset(14f * s, 3.5f * s),
            size = Size(6.5f * s, 6.5f * s),
            cornerRadius = corner,
            style = style
        )
        // Bottom-left rect
        drawRoundRect(
            color = color,
            topLeft = Offset(3.5f * s, 14f * s),
            size = Size(6.5f * s, 6.5f * s),
            cornerRadius = corner,
            style = style
        )
        // Bottom-right rect
        drawRoundRect(
            color = color,
            topLeft = Offset(14f * s, 14f * s),
            size = Size(6.5f * s, 6.5f * s),
            cornerRadius = corner,
            style = style
        )
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

        // 3 horizontal bars
        drawLine(
            color = color,
            start = Offset(8.5f * s, 6f * s),
            end = Offset(20.5f * s, 6f * s),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(8.5f * s, 12f * s),
            end = Offset(20.5f * s, 12f * s),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(8.5f * s, 18f * s),
            end = Offset(20.5f * s, 18f * s),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // 3 bullet points
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
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.2f * s,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
