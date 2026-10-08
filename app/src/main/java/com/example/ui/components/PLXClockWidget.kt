package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PLXGlassBackdrop
import com.example.ui.theme.PLXTextMuted
import com.example.ui.theme.PLXTextPrimary
import com.example.ui.theme.PLXTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PLXClockWidget(
    accentColor: Color,
    showClock: Boolean = true,
    showDate: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!showClock && !showDate) return

    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val secondsFormat = remember { SimpleDateFormat("ss", Locale.getDefault()) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }

    val formattedTime = timeFormat.format(currentTime)
    val formattedSeconds = secondsFormat.format(currentTime)
    val formattedDay = dayOfWeekFormat.format(currentTime).uppercase(Locale.getDefault())
    val formattedDate = dateFormat.format(currentTime).uppercase(Locale.getDefault())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PLXGlassBackdrop,
                        Color(0xD0101218)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.45f),
                        Color(0x22FFFFFF),
                        accentColor.copy(alpha = 0.15f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable {
                launchClockApp(context)
            }
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .testTag("plx_clock_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Top mini indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(accentColor, RoundedCornerShape(1.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYS_CLOCK // READY",
                        color = accentColor.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "UTC ${SimpleDateFormat("Z", Locale.getDefault()).format(currentTime)}",
                    color = PLXTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Time Display
            if (showClock) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 58.sp,
                        lineHeight = 60.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = PLXTextPrimary,
                        letterSpacing = (-1.5).sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = ":$formattedSeconds",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor
                        )
                        Text(
                            text = "SEC",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = PLXTextMuted
                        )
                    }
                }
            }

            // Date line
            if (showDate) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDay,
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "  •  $formattedDate",
                        color = PLXTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun launchClockApp(context: Context) {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback or ignore
    }
}
