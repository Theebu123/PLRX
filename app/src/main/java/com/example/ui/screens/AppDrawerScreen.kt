package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.AppSortOrder
import com.example.data.model.DrawerCategory
import com.example.data.model.LauncherSettings
import com.example.ui.components.AppIconItem
import com.example.ui.theme.PLXBlack
import com.example.ui.theme.PLXBorder
import com.example.ui.theme.PLXGlassBackdrop
import com.example.ui.theme.PLXSurface
import com.example.ui.theme.PLXTextMuted
import com.example.ui.theme.PLXTextPrimary
import com.example.ui.theme.PLXTextSecondary

@Composable
fun AppDrawerScreen(
    apps: List<AppItem>,
    settings: LauncherSettings,
    accentColor: Color,
    searchQuery: String,
    activeCategory: DrawerCategory,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (DrawerCategory) -> Unit,
    onSortOrderChange: (AppSortOrder) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenSupportConsole: () -> Unit = {},
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToHome()
    }

    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PLXBlack.copy(alpha = 0.94f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Drawer Bar (Back, Search, Sort)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PLXGlassBackdrop)
                    .border(1.dp, PLXBorder, RoundedCornerShape(10.dp))
                    .testTag("drawer_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = PLXTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("app_search_field"),
                placeholder = {
                    Text(
                        text = "Search ${apps.size} apps…",
                        color = PLXTextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotEmpty()) accentColor else PLXTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = PLXTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PLXSurface,
                    unfocusedContainerColor = PLXSurface,
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = PLXBorder,
                    focusedTextColor = PLXTextPrimary,
                    unfocusedTextColor = PLXTextPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Menu Button
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PLXGlassBackdrop)
                        .border(1.dp, PLXBorder, RoundedCornerShape(10.dp))
                        .testTag("drawer_sort_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort apps",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(PLXSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Alphabetical (A → Z)", color = PLXTextPrimary) },
                        onClick = {
                            onSortOrderChange(AppSortOrder.ALPHABETICAL_ASC)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Alphabetical (Z → A)", color = PLXTextPrimary) },
                        onClick = {
                            onSortOrderChange(AppSortOrder.ALPHABETICAL_DESC)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Most Used", color = PLXTextPrimary) },
                        onClick = {
                            onSortOrderChange(AppSortOrder.MOST_USED)
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Recently Installed", color = PLXTextPrimary) },
                        onClick = {
                            onSortOrderChange(AppSortOrder.RECENTLY_INSTALLED)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // Category Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = "ALL APPS",
                count = apps.size,
                selected = activeCategory == DrawerCategory.ALL,
                accentColor = accentColor,
                onClick = { onCategoryChange(DrawerCategory.ALL) }
            )

            CategoryChip(
                label = "GAMES",
                count = apps.count { it.isGame },
                selected = activeCategory == DrawerCategory.GAMES,
                accentColor = accentColor,
                onClick = { onCategoryChange(DrawerCategory.GAMES) }
            )

            CategoryChip(
                label = "FREQUENT",
                count = apps.count { it.launchCount > 0 },
                selected = activeCategory == DrawerCategory.FREQUENT,
                accentColor = accentColor,
                onClick = { onCategoryChange(DrawerCategory.FREQUENT) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // App Grid
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (searchQuery.trim().equals("UHA-1", ignoreCase = true)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "SUPPORT CLEARANCE DETECTED",
                            color = accentColor,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Code UHA-1 authenticated. Tap below to access Root ADB bootloader(flash OS) console.",
                            color = PLXTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenSupportConsole,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Open Firmware Console",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO APPS MATCHED",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try another search term or filter",
                            color = PLXTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.gridColumns),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .testTag("app_drawer_grid"),
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppIconItem(
                        app = app,
                        iconScale = settings.iconScale,
                        showLabel = settings.showLabels,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    count: Int,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bg = if (selected) accentColor.copy(alpha = 0.2f) else PLXGlassBackdrop
    val borderCol = if (selected) accentColor else Color(0x224B5569)
    val textCol = if (selected) accentColor else PLXTextSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textCol,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "($count)",
            color = if (selected) accentColor else PLXTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
