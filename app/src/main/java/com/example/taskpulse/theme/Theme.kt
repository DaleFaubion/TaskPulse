package com.example.taskpulse.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonViolet,
    secondary = ElectricCyan,
    tertiary = FlameRose,
    background = MidnightBackground,
    surface = GlassSurface,
    surfaceVariant = GlassSurfaceVariant,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
  )

private val LightColorScheme = DarkColorScheme // Enforce sleek Dark Theme by default for TaskPulse

@Composable
fun TaskPulseTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false, // Use our handcrafted Glassmorphism palette
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

