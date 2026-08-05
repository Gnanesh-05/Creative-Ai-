package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = ProfessionalPrimary,
    onPrimary = Color.White,
    primaryContainer = ProfessionalPrimaryContainer,
    onPrimaryContainer = ProfessionalOnPrimaryContainer,
    secondary = ProfessionalSecondary,
    onSecondary = Color.White,
    secondaryContainer = ProfessionalSecondaryContainer,
    onSecondaryContainer = ProfessionalOnSecondaryContainer,
    background = ProfessionalBackground,
    onBackground = ProfessionalTextPrimary,
    surface = ProfessionalSurface,
    onSurface = ProfessionalTextPrimary,
    surfaceVariant = ProfessionalSurfaceVariant,
    onSurfaceVariant = ProfessionalTextSecondary,
    outline = ProfessionalBorder,
    outlineVariant = ProfessionalDivider
  )

private val DarkColorScheme = LightColorScheme // Professional White Theme default for consistency

@Composable
fun CreativeAITheme(
  darkTheme: Boolean = false, // Enforce crisp white & professional theme across the app
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
