package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

data class OctDimensions(
    val paddingScreen: Dp = 20.dp,
    val paddingCard: Dp = 16.dp,
    val spacerSmall: Dp = 8.dp,
    val spacerMedium: Dp = 14.dp,
    val spacerLarge: Dp = 20.dp,
    val polaroidHeight: Dp = 260.dp,
    val bannerHeight: Dp = 160.dp,
    val isCompact: Boolean = false
)

val LocalOctThemePalette = staticCompositionLocalOf {
    OctThemes[0]
}

val LocalBackgroundStyle = staticCompositionLocalOf {
    "Pastel"
}

val LocalCustomBackgroundUri = staticCompositionLocalOf<String?> {
    null
}

val LocalFontStyle = staticCompositionLocalOf {
    "Default"
}

val LocalIsCompactMode = staticCompositionLocalOf {
    false
}

val LocalOctDimensions = staticCompositionLocalOf {
    OctDimensions()
}

@Composable
fun Oct15Theme(
    themeName: String = "Lavender Dream",
    backgroundStyle: String = "Pastel",
    customBackgroundUri: String? = null,
    fontStyle: String = "Default",
    compactModeSetting: String = "Auto",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isAutoCompact = configuration.screenHeightDp < 720 || configuration.screenWidthDp < 380
    val isCompact = when (compactModeSetting.lowercase()) {
        "compact" -> true
        "standard", "comfortable" -> false
        else -> isAutoCompact
    }

    val dimensions = if (isCompact) {
        OctDimensions(
            paddingScreen = 12.dp,
            paddingCard = 10.dp,
            spacerSmall = 4.dp,
            spacerMedium = 8.dp,
            spacerLarge = 12.dp,
            polaroidHeight = 190.dp,
            bannerHeight = 120.dp,
            isCompact = true
        )
    } else {
        OctDimensions(
            paddingScreen = 20.dp,
            paddingCard = 16.dp,
            spacerSmall = 8.dp,
            spacerMedium = 14.dp,
            spacerLarge = 20.dp,
            polaroidHeight = 260.dp,
            bannerHeight = 160.dp,
            isCompact = false
        )
    }

    val selectedPalette = OctThemes.find { it.name.equals(themeName, ignoreCase = true) }
        ?: OctThemes[0]

    val effectiveIsDark = darkTheme || selectedPalette.isDark

    val colorScheme = if (effectiveIsDark) {
        darkColorScheme(
            primary = selectedPalette.primary,
            onPrimary = Color.White,
            primaryContainer = selectedPalette.surface,
            onPrimaryContainer = selectedPalette.textPrimary,
            secondary = selectedPalette.accent,
            onSecondary = Color.White,
            background = selectedPalette.background,
            onBackground = selectedPalette.textPrimary,
            surface = selectedPalette.cardBackground,
            onSurface = selectedPalette.textPrimary,
            surfaceVariant = selectedPalette.surface,
            onSurfaceVariant = selectedPalette.textSecondary
        )
    } else {
        lightColorScheme(
            primary = selectedPalette.primary,
            onPrimary = Color.White,
            primaryContainer = selectedPalette.surface,
            onPrimaryContainer = selectedPalette.primary,
            secondary = selectedPalette.accent,
            onSecondary = Color.White,
            background = selectedPalette.background,
            onBackground = selectedPalette.textPrimary,
            surface = selectedPalette.cardBackground,
            onSurface = selectedPalette.textPrimary,
            surfaceVariant = selectedPalette.surface,
            onSurfaceVariant = selectedPalette.textSecondary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !effectiveIsDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !effectiveIsDark
            }
        }
    }

    val dynamicTypography = getTypographyForStyle(fontStyle, isCompact = isCompact)

    CompositionLocalProvider(
        LocalOctThemePalette provides selectedPalette,
        LocalBackgroundStyle provides backgroundStyle,
        LocalCustomBackgroundUri provides customBackgroundUri,
        LocalFontStyle provides fontStyle,
        LocalIsCompactMode provides isCompact,
        LocalOctDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = dynamicTypography,
            content = content
        )
    }
}
