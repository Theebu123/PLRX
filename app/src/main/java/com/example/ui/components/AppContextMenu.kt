package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.PLXBlack
import com.example.ui.theme.PLXBorder
import com.example.ui.theme.PLXSurface
import com.example.ui.theme.PLXTextMuted
import com.example.ui.theme.PLXTextPrimary
import com.example.ui.theme.PLXTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppItem,
    isPinnedToHome: Boolean,
    isPinnedToDock: Boolean,
    isHidden: Boolean = false,
    accentColor: Color,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onTogglePinHome: () -> Unit,
    onTogglePinDock: () -> Unit,
    onToggleGame: () -> Unit,
    onToggleHide: () -> Unit = {},
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PLXSurface,
        scrimColor = PLXBlack.copy(alpha = 0.75f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x55FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .padding(bottom = 24.dp)
                .testTag("app_context_menu")
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E222D)),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.label,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        color = PLXTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.packageName,
                        color = PLXTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                // Quick Launch Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor)
                        .clickable { onLaunch() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("menu_action_launch"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Open",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OPEN",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Options List
            MenuActionItem(
                icon = if (isPinnedToHome) Icons.Default.PushPin else Icons.Default.PushPin,
                title = if (isPinnedToHome) "Unpin from Home" else "Pin to Home Screen",
                subtitle = "Place on main launcher page",
                iconTint = if (isPinnedToHome) accentColor else PLXTextSecondary,
                onClick = onTogglePinHome,
                tag = "menu_action_pin_home"
            )

            MenuActionItem(
                icon = if (isPinnedToDock) Icons.Default.Star else Icons.Default.StarOutline,
                title = if (isPinnedToDock) "Remove from Dock" else "Pin to Bottom Dock",
                subtitle = "Keep in quick-access bar",
                iconTint = if (isPinnedToDock) accentColor else PLXTextSecondary,
                onClick = onTogglePinDock,
                tag = "menu_action_pin_dock"
            )

            MenuActionItem(
                icon = Icons.Default.SportsEsports,
                title = if (app.isGame) "Remove from Gaming Hub" else "Add to PLX Gaming Hub",
                subtitle = "Optimize & categorize in gaming section",
                iconTint = if (app.isGame) accentColor else PLXTextSecondary,
                onClick = onToggleGame,
                tag = "menu_action_toggle_game"
            )

            MenuActionItem(
                icon = if (isHidden) Icons.Default.Visibility else Icons.Default.Security,
                title = if (isHidden) "Restore from Isolation" else "System Restrict (Root ADB)",
                subtitle = if (isHidden) "Restore launcher visibility" else "Conceal under Root ADB bootloader protocol",
                iconTint = if (isHidden) accentColor else PLXTextSecondary,
                onClick = onToggleHide,
                tag = "menu_action_toggle_hide"
            )

            MenuActionItem(
                icon = Icons.Default.Info,
                title = "App Info",
                subtitle = "Manage permissions, storage, and notifications",
                iconTint = PLXTextSecondary,
                onClick = onAppInfo,
                tag = "menu_action_app_info"
            )

            MenuActionItem(
                icon = Icons.Default.Delete,
                title = "Uninstall",
                subtitle = "Remove app from device",
                iconTint = Color(0xFFFF3D00),
                textColor = Color(0xFFFF5252),
                onClick = onUninstall,
                tag = "menu_action_uninstall"
            )
        }
    }
}

@Composable
private fun MenuActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    textColor: Color = PLXTextPrimary,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E222D))
                .border(1.dp, PLXBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = PLXTextMuted,
                fontSize = 11.sp
            )
        }
    }
}
