package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PLX132Theme(
    accentColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val selectedAccent = ACCENT_THEMES.getOrElse(accentColorIndex) { ACCENT_THEMES[0] }

    val darkColorScheme = darkColorScheme(
        primary = selectedAccent.primary,
        onPrimary = Color.Black,
        primaryContainer = selectedAccent.glow,
        onPrimaryContainer = selectedAccent.primary,
        secondary = selectedAccent.primary,
        onSecondary = Color.Black,
        tertiary = PLXCyberCyan,
        onTertiary = Color.Black,
        background = PLXBlack,
        onBackground = PLXTextPrimary,
        surface = PLXSurface,
        onSurface = PLXTextPrimary,
        surfaceVariant = PLXSurfaceVariant,
        onSurfaceVariant = PLXTextSecondary,
        outline = PLXBorder,
        outlineVariant = PLXBorderSubtle
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography,
        content = content
    )
}
