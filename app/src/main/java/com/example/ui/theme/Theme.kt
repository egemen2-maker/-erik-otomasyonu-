package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassmorphicColorScheme = darkColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = StudioPrimaryLight,
    onPrimaryContainer = Color.White,
    secondary = StudioSecondary,
    onSecondary = Color.Black,
    secondaryContainer = StudioSecondaryLight,
    onSecondaryContainer = Color.White,
    tertiary = StudioTertiary,
    onTertiary = Color.White,
    tertiaryContainer = StudioTertiaryLight,
    onTertiaryContainer = Color.White,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = StudioBorderSubtle
)

@Composable
fun AutoReelTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassmorphicColorScheme,
        typography = Typography,
        content = content
    )
}

