package com.example.orbittap.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class OrbitVisualColors(
    val orbitTrack: Color,
    val targetArc: Color,
    val targetGlow: Color,
    val runnerDot: Color,
    val runnerGlow: Color,
    val centerDot: Color,
    val centerDotPulse: Color,
    val perfectFlash: Color,
    val missFlash: Color,
    val elevatedCard: Color,
    val subtleBorder: Color
)

private val DarkOrbitColors = OrbitVisualColors(
    orbitTrack = DarkOrbitTrack,
    targetArc = AccentCyan,
    targetGlow = ArcGlowCyan,
    runnerDot = RunnerColor,
    runnerGlow = Color(0x99FFFFFF),
    centerDot = DarkOrbitCenter,
    centerDotPulse = Color(0x3358A6FF),
    perfectFlash = PerfectGold,
    missFlash = MissRed,
    elevatedCard = DarkSurfaceElevated,
    subtleBorder = Color(0x30FFFFFF)
)

private val LightOrbitColors = OrbitVisualColors(
    orbitTrack = LightOrbitTrack,
    targetArc = Color(0xFF0284C7),
    targetGlow = Color(0x440284C7),
    runnerDot = Color(0xFF0F172A),
    runnerGlow = Color(0x550F172A),
    centerDot = LightOrbitCenter,
    centerDotPulse = Color(0x330969DA),
    perfectFlash = Color(0xFFD97706),
    missFlash = MissRed,
    elevatedCard = LightSurfaceElevated,
    subtleBorder = Color(0x20000000)
)

val LocalOrbitVisualColors = staticCompositionLocalOf { DarkOrbitColors }

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = AccentMint,
    onSecondary = Color(0xFF003830),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF30363D),
    error = MissRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFD0D7DE),
    error = MissRed,
    onError = Color.White
)

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun OrbitTapTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val orbitColors = if (darkTheme) DarkOrbitColors else LightOrbitColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalOrbitVisualColors provides orbitColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
