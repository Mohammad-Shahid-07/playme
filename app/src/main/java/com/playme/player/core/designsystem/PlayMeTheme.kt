package com.playme.player.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Obsidian Dark Glass & Electric Violet Design System for PlayMe.
 * Faithfully replicated from the finalized web prototype.
 */
object PlayMeDesign {

    // --- Core Backgrounds & Glass Tokens ---
    val ObsidianDark = Color(0xFF0C0E18)
    val ObsidianChassis = Color(0xFF0C0D16)
    val DarkGlassBackground = Color(0xD90C0E18)       // ~85% dark glass
    val DarkGlassElevated = Color(0xE60C0E18)         // ~90% dark glass
    val DarkGlassCard = Color(0x660E101C)             // ~40% frosted card
    val DarkGlassCardHover = Color(0xCC141727)        // ~80% hover card
    val DarkGlassBorder = Color(0x14FFFFFF)           // 8% white border
    val DarkGlassBorderHover = Color(0x26FFFFFF)      // 15% white border
    val BottomNavBackground = Color(0xE60C0E18)       // ~90% dark glass for bottom bar

    // --- Signature Electric Violet Palette ---
    val ElectricIndigo = Color(0xFF6366F1)
    val ElectricViolet = Color(0xFF757BF8)
    val DeepViolet = Color(0xFF8B5CF6)
    val Ultraviolet = Color(0xFFA855F7)
    val LavenderThumb = Color(0xFF9B9EFF)
    val LavenderGlow = Color(0x99757BF8)

    // --- Signature Protruding Play Button Colors ---
    val PlayButtonRingDark = Color(0xFF352F75)
    val PlayButtonRingMid = Color(0xFF526DFF)
    val PlayButtonRingLight = Color(0xFF8066FF)
    val PlayButtonInnerTop = Color(0xFF181A32)
    val PlayButtonInnerBottom = Color(0xFF0A0B14)

    // --- Curated Media Spec Badges ---
    val BadgeIndigoBg = Color(0x266366F1)
    val BadgeIndigoText = Color(0xFFA5B4FC)
    val BadgeIndigoBorder = Color(0x4D6366F1)

    val BadgeVioletBg = Color(0x33A855F7)
    val BadgeVioletText = Color(0xFFC084FC)
    val BadgeVioletBorder = Color(0x59A855F7)

    val BadgeSkyBg = Color(0x260284C7)
    val BadgeSkyText = Color(0xFF7DD3FC)
    val BadgeSkyBorder = Color(0x4D0284C7)

    val BadgePurpleBg = Color(0x269333EA)
    val BadgePurpleText = Color(0xFFD8B4FE)
    val BadgePurpleBorder = Color(0x4D9333EA)

    val BadgeCodecBg = Color(0x0FFFFFFF)
    val BadgeCodecText = Color(0xFFD4D4D8)
    val BadgeCodecBorder = Color(0x1AFFFFFF)

    // --- Vibrant Red NEW Badge & Highlight Tokens (Exact match to reference screenshots) ---
    val BadgeNewGreenBg = Color(0xFF00D287)
    val BadgeNewGreenText = Color(0xFF000000)
    val BadgeNewRedBg = Color(0xFFFF334B)
    val BadgeNewRedText = Color(0xFFFFFFFF)
    val HighlightLastPlayed = Color(0xFF38BDF8) // Vibrant Sky Blue/Cyan from reference screenshot

    // --- Category Folder Colors ---
    val FolderCameraBg = Color(0x266366F1)
    val FolderCameraText = Color(0xFFA5B4FC)

    val FolderDownloadsBg = Color(0x268B5CF6)
    val FolderDownloadsText = Color(0xFFC084FC)

    val FolderMoviesBg = Color(0x26EC4899)
    val FolderMoviesText = Color(0xFFF472B6)

    val FolderRecordsBg = Color(0x2606B6D4)
    val FolderRecordsText = Color(0xFF22D3EE)

    // --- Brushes & Gradients ---
    val ElectricGradient = Brush.horizontalGradient(
        listOf(ElectricIndigo, ElectricViolet, DeepViolet)
    )

    val LogoGradient = Brush.horizontalGradient(
        listOf(ElectricIndigo, ElectricViolet, Ultraviolet)
    )

    val PlayButtonRingBrush = Brush.sweepGradient(
        listOf(PlayButtonRingDark, PlayButtonRingMid, PlayButtonRingLight, PlayButtonRingDark)
    )

    val PlayButtonInnerBrush = Brush.verticalGradient(
        listOf(PlayButtonInnerTop, PlayButtonInnerBottom)
    )

    val TopBarVignette = Brush.verticalGradient(
        listOf(Color.Black.copy(alpha = 0.90f), Color.Black.copy(alpha = 0.50f), Color.Transparent)
    )

    val BottomBarVignette = Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.60f), Color.Black.copy(alpha = 0.95f))
    )
}

// Reusable Glass Modifiers
fun Modifier.darkGlass(
    shape: Shape = CircleShape,
    borderWidth: Float = 1f
): Modifier = this
    .clip(shape)
    .background(PlayMeDesign.DarkGlassBackground)
    .border(borderWidth.dp, PlayMeDesign.DarkGlassBorder, shape)

fun Modifier.darkGlassElevated(
    shape: Shape = CircleShape,
    borderWidth: Float = 1f
): Modifier = this
    .clip(shape)
    .background(PlayMeDesign.DarkGlassElevated)
    .border(borderWidth.dp, PlayMeDesign.DarkGlassBorder, shape)

fun Modifier.darkGlassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Float = 1f
): Modifier = this
    .clip(shape)
    .background(PlayMeDesign.DarkGlassCard)
    .border(borderWidth.dp, PlayMeDesign.DarkGlassBorder, shape)

@Composable
fun PlayMeTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = PlayMeDesign.ElectricViolet,
        secondary = PlayMeDesign.DeepViolet,
        background = PlayMeDesign.ObsidianDark,
        surface = PlayMeDesign.ObsidianDark,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}
