package com.playme.player.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object PlayMeColors {
    // Obsidian Surfaces
    val ObsidianDark = Color(0xFF0C0E18)
    val ObsidianChassis = Color(0xFF0C0D16)
    val Workbench = Color(0xFF080910)

    // Glass Overlays & Borders
    val GlassSurface = Color(0x0DFFFFFF) // rgba(255, 255, 255, 0.05)
    val GlassElevated = Color(0xEB0B0D17) // rgba(11, 13, 23, 0.92)
    val GlassCard = Color(0x660E101C) // rgba(14, 16, 28, 0.40)
    val GlassBorder = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
    val GlassBorderHover = Color(0x29FFFFFF) // rgba(255, 255, 255, 0.16)

    // Signature Electric Violet Palette
    val ElectricIndigo = Color(0xFF6366F1)
    val ElectricViolet = Color(0xFF757BF8)
    val DeepViolet = Color(0xFF8B5CF6)
    val UltraViolet = Color(0xFFA855F7)
    val LavenderThumb = Color(0xFF9B9EFF)
    val LavenderText = Color(0xFFA5B4FC)
    val LavenderGlow = Color(0x80757BF8)

    // Functional Accents
    val AmberBoost = Color(0xFFF59E0B)
    val AmberLight = Color(0xFFFCD34D)
    val EmeraldActive = Color(0xFF00D287)
    val CrimsonAlert = Color(0xFFFF334B)
    val CyanDiagnostics = Color(0xFF38BDF8)

    // Text & Neutrals
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
    val TextTertiary = Color(0xFF64748B)

    // Signature Gradients
    val VioletGradient = Brush.horizontalGradient(
        colors = listOf(ElectricIndigo, ElectricViolet, DeepViolet)
    )
    val GlassOverlayGradient = Brush.verticalGradient(
        colors = listOf(Color(0xCC0C0E18), Color(0x000C0E18), Color(0xF00C0E18))
    )
}
