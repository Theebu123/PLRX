package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppItem
import com.example.ui.theme.PLXBlack
import com.example.ui.theme.PLXGlassBackdrop

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PLXDock(
    dockApps: List<AppItem>,
    accentColor: Color,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenDrawerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PLXGlassBackdrop,
                            PLXBlack.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            Color(0x224B5569),
                            accentColor.copy(alpha = 0.35f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("plx_dock_bar"),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render first 2 dock apps
            val leftApps = dockApps.take(2)
            for (app in leftApps) {
                DockAppSlot(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }

            // Fill empty left slot if needed
            for (i in 0 until (2 - leftApps.size)) {
                EmptyDockSlot(accentColor = accentColor)
            }

            // Center App Drawer Trigger Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.28f),
                                Color(0x331E212A)
                            )
                        )
                    )
                    .border(1.5.dp, accentColor, CircleShape)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenDrawerClick()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenDrawerClick()
                        }
                    )
                    .testTag("dock_app_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Open App Drawer",
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Render remaining dock apps (up to 2 or 3)
            val rightApps = dockApps.drop(2).take(2)
            for (app in rightApps) {
                DockAppSlot(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }

            // Fill empty right slots
            for (i in 0 until (2 - rightApps.size)) {
                EmptyDockSlot(accentColor = accentColor)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockAppSlot(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Throwable) {}
                    onClick()
                },
                onLongClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Throwable) {}
                    onLongClick()
                }
            )
            .testTag("dock_slot_${app.packageName}")
    ) {
        if (app.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = app.label,
                modifier = Modifier.size(46.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF232733))
            )
        }
    }
}

@Composable
private fun EmptyDockSlot(accentColor: Color) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(accentColor.copy(alpha = 0.3f), CircleShape)
        )
    }
}
