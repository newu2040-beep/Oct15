package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Lavender Dream
val LavenderPrimary = Color(0xFF8B72D6)
val LavenderSurface = Color(0xFFF0EAFE)
val LavenderBackground = Color(0xFFFFF9F1)
val LavenderOnPrimary = Color(0xFFFFFFFF)
val LavenderText = Color(0xFF292532)
val LavenderTextSecondary = Color(0xFF777181)

// Pastel Accents
val PastelPeach = Color(0xFFF4B69C)
val PastelMint = Color(0xFFBFE8D5)
val PastelPink = Color(0xFFFFB7B2)
val PastelYellow = Color(0xFFFFF3B0)
val PastelSky = Color(0xFFA2D2FF)
val PastelRose = Color(0xFFE8AEB7)

data class OctThemePalette(
    val name: String,
    val primary: Color,
    val surface: Color,
    val background: Color,
    val cardBackground: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isDark: Boolean = false
)

val OctThemes = listOf(
    OctThemePalette(
        name = "Lavender Dream",
        primary = Color(0xFF8B72D6),
        surface = Color(0xFFF4EFFF),
        background = Color(0xFFFFF9F2),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFF4B69C),
        textPrimary = Color(0xFF292532),
        textSecondary = Color(0xFF777181)
    ),
    OctThemePalette(
        name = "Warm Cream",
        primary = Color(0xFFD97706),
        surface = Color(0xFFFEF3C7),
        background = Color(0xFFFFFDF9),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFF59E0B),
        textPrimary = Color(0xFF292524),
        textSecondary = Color(0xFF78716C)
    ),
    OctThemePalette(
        name = "Soft Peach",
        primary = Color(0xFFE07A5F),
        surface = Color(0xFFFCEADE),
        background = Color(0xFFFFFBF7),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF81B29A),
        textPrimary = Color(0xFF3D2C2E),
        textSecondary = Color(0xFF8A7276)
    ),
    OctThemePalette(
        name = "Mint Garden",
        primary = Color(0xFF2A9D8F),
        surface = Color(0xFFE0F2E9),
        background = Color(0xFFF7FCF9),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFE76F51),
        textPrimary = Color(0xFF1D3532),
        textSecondary = Color(0xFF63837F)
    ),
    OctThemePalette(
        name = "Powder Blue",
        primary = Color(0xFF4A90E2),
        surface = Color(0xFFE3EFFF),
        background = Color(0xFFF9FBFF),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFA0C4FF),
        textPrimary = Color(0xFF1E293B),
        textSecondary = Color(0xFF64748B)
    ),
    OctThemePalette(
        name = "Rose Blush",
        primary = Color(0xFFD94680),
        surface = Color(0xFFFDE8EF),
        background = Color(0xFFFFF9FA),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFF472B6),
        textPrimary = Color(0xFF3B1C28),
        textSecondary = Color(0xFF8E6374)
    ),
    OctThemePalette(
        name = "Sage Green",
        primary = Color(0xFF606C38),
        surface = Color(0xFFEDF1E4),
        background = Color(0xFFFBFBF9),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFDDA15E),
        textPrimary = Color(0xFF283618),
        textSecondary = Color(0xFF6B705C)
    ),
    OctThemePalette(
        name = "Midnight Ink",
        primary = Color(0xFF818CF8),
        surface = Color(0xFF1E1B2E),
        background = Color(0xFF13111C),
        cardBackground = Color(0xFF262238),
        accent = Color(0xFFA5B4FC),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        isDark = true
    ),
    OctThemePalette(
        name = "Strawberry Milk",
        primary = Color(0xFFFF5D8F),
        surface = Color(0xFFFFE5EC),
        background = Color(0xFFFFF7F9),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFFFACC7),
        textPrimary = Color(0xFF4A1525),
        textSecondary = Color(0xFF9E5A6C)
    ),
    OctThemePalette(
        name = "Buttercup Honey",
        primary = Color(0xFFE09F3E),
        surface = Color(0xFFFFF3CD),
        background = Color(0xFFFFFDF5),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFFFD166),
        textPrimary = Color(0xFF3D2F14),
        textSecondary = Color(0xFF85724D)
    ),
    OctThemePalette(
        name = "Lilac Mist",
        primary = Color(0xFF7B2CBF),
        surface = Color(0xFFF3E8FF),
        background = Color(0xFFFAF5FF),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFC77DFF),
        textPrimary = Color(0xFF240046),
        textSecondary = Color(0xFF725192)
    ),
    OctThemePalette(
        name = "Matcha Latte",
        primary = Color(0xFF588157),
        surface = Color(0xFFE9F5E9),
        background = Color(0xFFF7FCF7),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFA3B18A),
        textPrimary = Color(0xFF1B2E1E),
        textSecondary = Color(0xFF596E5C)
    ),
    OctThemePalette(
        name = "Ocean Whisper",
        primary = Color(0xFF0077B6),
        surface = Color(0xFFE0F2FE),
        background = Color(0xFFF0F9FF),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF48CAE4),
        textPrimary = Color(0xFF0C2A3A),
        textSecondary = Color(0xFF4A7287)
    ),
    OctThemePalette(
        name = "Sunset Sorbet",
        primary = Color(0xFFE76F51),
        surface = Color(0xFFFFE8D6),
        background = Color(0xFFFFF9F5),
        cardBackground = Color(0xFFFFFFFF),
        accent = Color(0xFFF4A261),
        textPrimary = Color(0xFF4A2518),
        textSecondary = Color(0xFF8F6352)
    )
)
