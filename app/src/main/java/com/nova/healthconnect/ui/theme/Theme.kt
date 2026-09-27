package com.nova.healthconnect.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NovaTeal,
    onPrimary = NovaTextOnTeal,
    primaryContainer = NovaMint,
    onPrimaryContainer = NovaTealDark,
    secondary = NovaViolet,
    onSecondary = NovaTextOnTeal,
    secondaryContainer = NovaVioletSoft,
    onSecondaryContainer = NovaViolet,
    tertiary = NovaTealLight,
    background = NovaBackground,
    onBackground = NovaTextPrimary,
    surface = NovaSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceVariant,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaBorder,
    outlineVariant = NovaBorderSoft
)

private val DarkColorScheme = darkColorScheme(
    primary = NovaTealLight,
    onPrimary = NovaTextPrimary,
    primaryContainer = NovaTealDark,
    onPrimaryContainer = NovaMint,
    secondary = NovaVioletLight,
    onSecondary = NovaTextPrimary,
    background = Color(0xFF0F1523),
    surface = Color(0xFF161E31),
    onBackground = Color(0xFFFAF8FF),
    onSurface = Color(0xFFFAF8FF),
    outline = Color(0xFF283452)
)

@Composable
fun NovaHealthConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
