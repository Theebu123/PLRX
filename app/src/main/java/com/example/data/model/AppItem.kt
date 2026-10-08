package com.example.data.model

import androidx.compose.ui.graphics.ImageBitmap

enum class AppSortOrder {
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC,
    MOST_USED,
    RECENTLY_INSTALLED
}

enum class DrawerCategory {
    ALL,
    GAMES,
    FREQUENT
}

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isGame: Boolean = false,
    val installTime: Long = 0L,
    val launchCount: Int = 0,
    val iconBitmap: ImageBitmap? = null
)
