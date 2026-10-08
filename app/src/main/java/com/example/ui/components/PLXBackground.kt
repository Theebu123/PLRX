package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.PLXBlack
import com.example.ui.theme.PLXDeepGraphite

@Composable
fun PLXBackground(
    wallpaperIndex: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(PLXBlack)) {
        when (wallpaperIndex) {
            0 -> {
                // High-resolution generated PLX Cyber wallpaper with dark scrim
                AsyncImage(
                    model = R.drawable.plx_wallpaper,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark glass scrim for contrast and legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PLXBlack.copy(alpha = 0.55f),
                                    Color.Black.copy(alpha = 0.45f),
                                    PLXBlack.copy(alpha = 0.82f)
                                )
                            )
                        )
                )
            }
            1 -> {
                // Procedural Cyber Carbon Matrix
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Base gradient
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PLXDeepGraphite,
                                PLXBlack
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.35f),
                            radius = size.width * 1.2f
                        )
                    )

                    // Subtle geometric circuit grid lines
                    val step = 60f
                    val lineColor = Color(0x12FFFFFF)
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = lineColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }

                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += step
                    }

                    // Subtle accent glow orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.15f),
                            radius = size.width * 0.5f
                        ),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * 0.85f, size.height * 0.15f)
                    )
                }
            }
            else -> {
                // Pure OLED Minimalist Black with subtle bottom sheen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF060709),
                                    Color(0xFF0B0C0E),
                                    Color(0xFF040506)
                                )
                            )
                        )
                )
            }
        }
    }
}
