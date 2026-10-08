package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NetworkType
import com.example.data.model.SystemTelemetry
import com.example.ui.theme.PLXGlassBackdrop
import com.example.ui.theme.PLXTextMuted
import com.example.ui.theme.PLXTextPrimary
import com.example.ui.theme.PLXTextSecondary

@Composable
fun PLXTopStatusBar(
    telemetry: SystemTelemetry,
    accentColor: Color,
    onLogoClick: () -> Unit = {},
    showBattery: Boolean = true,
    showNetwork: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PLX132 Cyber Brand Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onLogoClick() }
                .background(PLXGlassBackdrop)
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .testTag("plx_brand_badge")
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "PLX",
                color = PLXTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "132",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // System Telemetry Pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display refresh rate badge
            if (telemetry.refreshRateHz > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x331E212A))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${telemetry.refreshRateHz}Hz",
                        color = PLXTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Real Network Indicator
            if (showNetwork) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x331E212A))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    val networkIcon = when (telemetry.networkType) {
                        NetworkType.WIFI -> Icons.Default.Wifi
                        NetworkType.CELLULAR -> Icons.Default.NetworkCell
                        NetworkType.ETHERNET -> Icons.Default.NetworkCheck
                        NetworkType.OFFLINE -> Icons.Default.WifiOff
                    }
                    val netColor = if (telemetry.isOnline) accentColor else PLXTextMuted
                    Icon(
                        imageVector = networkIcon,
                        contentDescription = "Network status",
                        tint = netColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (telemetry.networkType) {
                            NetworkType.WIFI -> "Wi-Fi"
                            NetworkType.CELLULAR -> "LTE"
                            NetworkType.ETHERNET -> "ETH"
                            NetworkType.OFFLINE -> "OFF"
                        },
                        color = if (telemetry.isOnline) PLXTextPrimary else PLXTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Real Battery Indicator
            if (showBattery) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x331E212A))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    val batteryIcon = when {
                        telemetry.isCharging -> Icons.Default.BatteryChargingFull
                        telemetry.batteryPercent <= 15 -> Icons.Default.BatteryAlert
                        else -> Icons.Default.BatteryFull
                    }
                    val batColor = when {
                        telemetry.isCharging -> accentColor
                        telemetry.batteryPercent <= 15 -> Color(0xFFFF3D00)
                        else -> PLXTextPrimary
                    }
                    Icon(
                        imageVector = batteryIcon,
                        contentDescription = "Battery ${telemetry.batteryPercent}%",
                        tint = batColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${telemetry.batteryPercent}%",
                        color = PLXTextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
