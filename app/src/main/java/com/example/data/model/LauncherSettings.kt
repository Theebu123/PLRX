package com.example.data.model

data class LauncherSettings(
    val accentColorIndex: Int = 0, // 0=Crimson, 1=Cyan, 2=Amber, 3=Lime, 4=Silver
    val gridColumns: Int = 4, // 4 or 5
    val iconScale: Float = 1.0f, // 0.85f - 1.15f
    val showLabels: Boolean = true,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showBattery: Boolean = true,
    val showNetwork: Boolean = true,
    val showPerformancePanel: Boolean = true,
    val wallpaperIndex: Int = 0, // 0 = PLX Cyber Dark asset, 1 = Minimal Obsidian Hex, 2 = Deep OLED Black
    val appSortOrder: AppSortOrder = AppSortOrder.ALPHABETICAL_ASC,
    val pinnedDockPackages: List<String> = emptyList(),
    val pinnedHomePackages: List<String> = emptyList(),
    val customGamePackages: Set<String> = emptySet(),
    val hiddenPackages: Set<String> = emptySet(),
    val autoDetectGames: Boolean = true
)
