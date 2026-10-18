package com.playme.player.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PlayMeColors.ElectricViolet,
    onPrimary = Color.White,
    primaryContainer = PlayMeColors.ElectricIndigo,
    onPrimaryContainer = Color.White,
    secondary = PlayMeColors.DeepViolet,
    onSecondary = Color.White,
    background = PlayMeColors.ObsidianDark,
    onBackground = PlayMeColors.TextPrimary,
    surface = PlayMeColors.ObsidianChassis,
    onSurface = PlayMeColors.TextPrimary,
    surfaceVariant = PlayMeColors.GlassElevated,
    onSurfaceVariant = PlayMeColors.LavenderText,
    error = PlayMeColors.CrimsonAlert,
    onError = Color.White
)

@Composable
fun PlayMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
