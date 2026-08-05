package com.example.frontend.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexusDarkColorScheme = darkColorScheme(
    primary = NexusViolet,
    onPrimary = Color.White,
    primaryContainer = NexusPurple,
    onPrimaryContainer = Color.White,
    secondary = NexusMagenta,
    onSecondary = Color.White,
    secondaryContainer = NexusGold,
    tertiary = NexusCyan,
    onTertiary = Color.Black,
    background = DarkSurface,
    onBackground = TextPrimaryDark,
    surface = DarkSurfaceVariant,
    onSurface = TextPrimaryDark,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = GlassBorder,
    outlineVariant = Color(0xFF4A1F6E)
)

private val NexusLightColorScheme = lightColorScheme(
    primary = NexusViolet,
    onPrimary = Color.White,
    primaryContainer = LightGlassSurface,
    onPrimaryContainer = TextPrimaryLight,
    secondary = NexusMagenta,
    onSecondary = Color.White,
    secondaryContainer = LightGlassSurfaceVariant,
    tertiary = NexusCyan,
    onTertiary = Color.Black,
    background = LightCanvasStart,
    onBackground = TextPrimaryLight,
    surface = LightGlassSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightGlassSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightGlassBorder,
    outlineVariant = Color(0x338B5CF6)
)

@Composable
fun NexusAiTheme(
    darkTheme: Boolean = false, // Default to light pastel glass theme matching attached image
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NexusDarkColorScheme
        else -> NexusLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
