package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.LocalBackgroundStyle
import com.example.ui.theme.LocalCustomBackgroundUri
import com.example.ui.theme.LocalOctThemePalette

@Composable
fun ScrapbookBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalOctThemePalette.current
    val backgroundStyle = LocalBackgroundStyle.current
    val customBackgroundUri = LocalCustomBackgroundUri.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        if (backgroundStyle.equals("custom", ignoreCase = true) && !customBackgroundUri.isNullOrBlank()) {
            AsyncImage(
                model = customBackgroundUri,
                contentDescription = "Custom Background Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Soft overlay to maintain UI contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.background.copy(alpha = 0.72f))
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                when (backgroundStyle.lowercase()) {
                    "grid" -> {
                        val stepPx = 32.dp.toPx()
                        val gridColor = if (palette.isDark) Color(0xFF2E2A44).copy(alpha = 0.4f)
                        else palette.primary.copy(alpha = 0.08f)

                        var x = 0f
                        while (x < size.width) {
                            drawLine(
                                color = gridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            x += stepPx
                        }

                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                            y += stepPx
                        }
                    }

                    "scrapbook" -> {
                        val checkSize = 36.dp.toPx()
                        val checkColor = if (palette.isDark) Color(0xFF2D2942).copy(alpha = 0.3f)
                        else palette.surface.copy(alpha = 0.45f)

                        var row = 0
                        var y = 0f
                        while (y < size.height) {
                            var col = 0
                            var x = 0f
                            while (x < size.width) {
                                if ((row + col) % 2 == 0) {
                                    drawRect(
                                        color = checkColor,
                                        topLeft = Offset(x, y),
                                        size = Size(checkSize, checkSize)
                                    )
                                }
                                x += checkSize
                                col++
                            }
                            y += checkSize
                            row++
                        }
                    }

                    "dots", "polka" -> {
                        val dotSpacing = 28.dp.toPx()
                        val dotRadius = 2.5.dp.toPx()
                        val dotColor = if (palette.isDark) palette.primary.copy(alpha = 0.2f)
                        else palette.primary.copy(alpha = 0.12f)

                        var y = 14.dp.toPx()
                        var row = 0
                        while (y < size.height) {
                            val xOffset = if (row % 2 == 0) 0f else (dotSpacing / 2f)
                            var x = xOffset
                            while (x < size.width) {
                                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                                x += dotSpacing
                            }
                            y += dotSpacing
                            row++
                        }
                    }

                    "clouds" -> {
                        // Soft cloudy ambient shapes
                        val cloudColor1 = palette.surface.copy(alpha = 0.4f)
                        val cloudColor2 = palette.accent.copy(alpha = 0.15f)
                        drawCircle(color = cloudColor1, radius = size.width * 0.35f, center = Offset(size.width * 0.2f, size.height * 0.15f))
                        drawCircle(color = cloudColor2, radius = size.width * 0.4f, center = Offset(size.width * 0.85f, size.height * 0.35f))
                        drawCircle(color = cloudColor1, radius = size.width * 0.45f, center = Offset(size.width * 0.15f, size.height * 0.75f))
                    }

                    "stars" -> {
                        val starColor = if (palette.isDark) palette.accent.copy(alpha = 0.4f)
                        else Color(0xFFFFB703).copy(alpha = 0.25f)
                        val sparkleColor = palette.primary.copy(alpha = 0.25f)

                        val starCoords = listOf(
                            Offset(size.width * 0.15f, size.height * 0.12f),
                            Offset(size.width * 0.85f, size.height * 0.08f),
                            Offset(size.width * 0.75f, size.height * 0.22f),
                            Offset(size.width * 0.1f, size.height * 0.35f),
                            Offset(size.width * 0.9f, size.height * 0.48f),
                            Offset(size.width * 0.2f, size.height * 0.65f),
                            Offset(size.width * 0.8f, size.height * 0.78f),
                            Offset(size.width * 0.12f, size.height * 0.88f)
                        )

                        for (coord in starCoords) {
                            drawCircle(color = sparkleColor, radius = 3.dp.toPx(), center = coord)
                            drawLine(
                                color = starColor,
                                start = Offset(coord.x - 7.dp.toPx(), coord.y),
                                end = Offset(coord.x + 7.dp.toPx(), coord.y),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawLine(
                                color = starColor,
                                start = Offset(coord.x, coord.y - 7.dp.toPx()),
                                end = Offset(coord.x, coord.y + 7.dp.toPx()),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    else -> {
                        // "Pastel" - Smooth subtle ambient gradient
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    palette.surface.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.8f, size.height * 0.2f),
                                radius = size.width * 0.9f
                            )
                        )
                    }
                }
            }
        }

        content()
    }
}
