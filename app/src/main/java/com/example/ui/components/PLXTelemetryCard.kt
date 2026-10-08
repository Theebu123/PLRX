package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.SystemTelemetry
import com.example.ui.theme.PLXGlassBackdrop
import com.example.ui.theme.PLXTextMuted
import com.example.ui.theme.PLXTextPrimary
import com.example.ui.theme.PLXTextSecondary
import java.util.Locale

@Composable
fun PLXTelemetryCard(
    telemetry: SystemTelemetry,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val totalRamGb = telemetry.totalRamBytes / (1024f * 1024f * 1024f)
    val usedRamGb = telemetry.usedRamBytes / (1024f * 1024f * 1024f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PLXGlassBackdrop)
            .border(1.dp, Color(0x224B5569), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("plx_telemetry_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accentColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HARDWARE TELEMETRY",
                        color = PLXTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "REAL-TIME",
                    color = accentColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Column Metric Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Metric 1: Real RAM
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RAM USAGE",
                        fontSize = 9.sp,
                        color = PLXTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${telemetry.usedRamPercent}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (telemetry.isLowRam) Color(0xFFFF3D00) else PLXTextPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f / %.1f GB", usedRamGb, totalRamGb),
                        fontSize = 9.sp,
                        color = PLXTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color(0x334B5569))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(telemetry.usedRamPercent / 100f)
                                .height(3.dp)
                                .background(accentColor)
                        )
                    }
                }

                // Metric 2: Battery Temp
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TEMP",
                        fontSize = 9.sp,
                        color = PLXTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f°C", telemetry.temperatureCelsius),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (telemetry.temperatureCelsius > 42f) Color(0xFFFF3D00) else PLXTextPrimary
                    )
                    Text(
                        text = telemetry.batteryHealth,
                        fontSize = 9.sp,
                        color = PLXTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val tempProgress = ((telemetry.temperatureCelsius - 20f) / 30f).coerceIn(0.1f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color(0x334B5569))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(tempProgress)
                                .height(3.dp)
                                .background(if (telemetry.temperatureCelsius > 40f) Color(0xFFFF3D00) else accentColor)
                        )
                    }
                }

                // Metric 3: Internal Storage
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STORAGE",
                        fontSize = 9.sp,
                        color = PLXTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "%.0f GB", telemetry.freeStorageGb),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PLXTextPrimary
                    )
                    Text(
                        text = "FREE SPACE",
                        fontSize = 9.sp,
                        color = PLXTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val usedStorageRatio = if (telemetry.totalStorageGb > 0) {
                        (telemetry.totalStorageGb - telemetry.freeStorageGb) / telemetry.totalStorageGb
                    } else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color(0x334B5569))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(usedStorageRatio.coerceIn(0f, 1f))
                                .height(3.dp)
                                .background(accentColor)
                        )
                    }
                }
            }
        }
    }
}
