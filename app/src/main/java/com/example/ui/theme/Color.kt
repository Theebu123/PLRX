package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// PLX132 Base Dark Cybernetic Palette
val PLXBlack = Color(0xFF090A0C)
val PLXDeepGraphite = Color(0xFF101216)
val PLXSurface = Color(0xFF16181F)
val PLXSurfaceVariant = Color(0xFF1D212A)
val PLXGlassBackdrop = Color(0xD9141720)
val PLXBorder = Color(0xFF282E3B)
val PLXBorderSubtle = Color(0x334B5569)

// Accent Palette
val PLXCrimsonRed = Color(0xFFFF2438)       // Signature REDMAGIC / PLX Crimson
val PLXCrimsonGlow = Color(0x33FF2438)

val PLXCyberCyan = Color(0xFF00E5FF)
val PLXCyberCyanGlow = Color(0x3300E5FF)

val PLXAmberGold = Color(0xFFFFB300)
val PLXAmberGoldGlow = Color(0x33FFB300)

val PLXToxicLime = Color(0xFF00FF77)
val PLXToxicLimeGlow = Color(0x3300FF77)

val PLXStealthSilver = Color(0xFFE2E4E9)
val PLXStealthSilverGlow = Color(0x33E2E4E9)

// System & Text
val PLXTextPrimary = Color(0xFFF3F4F7)
val PLXTextSecondary = Color(0xFF9AA2B2)
val PLXTextMuted = Color(0xFF5E6778)

val PLXSuccess = Color(0xFF00E676)
val PLXWarning = Color(0xFFFFAB00)
val PLXError = Color(0xFFFF3D00)
val PLXCardHighlight = Color(0x1AFFFFFF)

data class AccentTheme(
    val name: String,
    val primary: Color,
    val glow: Color
)

val ACCENT_THEMES = listOf(
    AccentTheme("Crimson Core", PLXCrimsonRed, PLXCrimsonGlow),
    AccentTheme("Cyber Cyan", PLXCyberCyan, PLXCyberCyanGlow),
    AccentTheme("Amber Surge", PLXAmberGold, PLXAmberGoldGlow),
    AccentTheme("Matrix Lime", PLXToxicLime, PLXToxicLimeGlow),
    AccentTheme("Stealth Chrome", PLXStealthSilver, PLXStealthSilverGlow)
)
