package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.data.model.SystemTelemetry
import com.example.ui.components.AppIconItem
import com.example.ui.components.PLXClockWidget
import com.example.ui.components.PLXDock
import com.example.ui.components.PLXTelemetryCard
import com.example.ui.components.PLXTopStatusBar
import com.example.ui.theme.PLXGlassBackdrop
import com.example.ui.theme.PLXTextMuted
import com.example.ui.theme.PLXTextPrimary
import com.example.ui.theme.PLXTextSecondary

@Composable
fun HomeScreen(
    pinnedApps: List<AppItem>,
    dockApps: List<AppItem>,
    telemetry: SystemTelemetry,
    settings: LauncherSettings,
    accentColor: Color,
    isDefaultLauncher: Boolean,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenGamingHub: () -> Unit,
    onOpenSettings: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {},
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount < -30) {
                            change.consume()
                            onOpenDrawer()
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Status Bar
            PLXTopStatusBar(
                telemetry = telemetry,
                accentColor = accentColor,
                onLogoClick = onOpenSettings,
                showBattery = settings.showBattery,
                showNetwork = settings.showNetwork
            )

            // Quick Nav Chips (Gaming Hub, Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gaming Hub Quick Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PLXGlassBackdrop)
                        .border(1.dp, Color(0x224B5569), RoundedCornerShape(8.dp))
                        .clickable { onOpenGamingHub() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("home_quick_gaming_pill")
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Gaming Hub",
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GAMING HUB",
                        color = PLXTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PLXTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Settings Icon Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PLXGlassBackdrop)
                        .border(1.dp, Color(0x224B5569), RoundedCornerShape(8.dp))
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Launcher Settings",
                        tint = PLXTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Default Launcher Recommendation Banner (if not default)
            AnimatedVisibility(visible = !isDefaultLauncher) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF2438))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onSetDefaultLauncher() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("set_default_launcher_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Set PLX132 as default launcher",
                                color = PLXTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "SET",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Clock Widget
            if (settings.showClock || settings.showDate) {
                PLXClockWidget(
                    accentColor = accentColor,
                    showClock = settings.showClock,
                    showDate = settings.showDate
                )
            }

            // Hardware Telemetry Panel
            if (settings.showPerformancePanel) {
                PLXTelemetryCard(
                    telemetry = telemetry,
                    accentColor = accentColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Grid of Pinned / Favorite Apps
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (pinnedApps.isEmpty()) {
                    // Empty state guide
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SWIPE UP FOR ALL APPS",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Long-press any app in the drawer to pin it here",
                            color = PLXTextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridColumns),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pinnedApps, key = { it.packageName }) { app ->
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

            // Swipe-up indicator handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDrawer() }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Swipe up for apps",
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "APPS",
                        color = PLXTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bottom Dock
            PLXDock(
                dockApps = dockApps,
                accentColor = accentColor,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                onOpenDrawerClick = onOpenDrawer
            )
        }
    }
}
