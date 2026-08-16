package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighDensityColorScheme = lightColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = StudioPrimaryLight,
    onPrimaryContainer = StudioPrimaryDark,
    secondary = StudioSecondary,
    onSecondary = Color.White,
    secondaryContainer = StudioSurfaceVariant,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = StudioTertiary,
    onTertiary = Color.White,
    tertiaryContainer = StudioTertiaryLight,
    onTertiaryContainer = Color(0xFF31111D),
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = Color(0xFFE6E1E5)
)

@Composable
fun AutoReelTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HighDensityColorScheme,
        typography = Typography,
        content = content
    )
}
