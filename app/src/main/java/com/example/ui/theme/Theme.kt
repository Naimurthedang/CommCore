package com.example.ui.theme

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
    primary = CyberPurple,
    secondary = CyberMint,
    tertiary = GoldAccent,
    background = MidnightBg,
    surface = MidnightSurface,
    onBackground = LightSilver,
    onSurface = LightSilver
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CyberPurple,
    secondary = CyberMint,
    tertiary = GoldAccent,
    background = MidnightBg,
    surface = MidnightSurface,
    onBackground = LightSilver,
    onSurface = LightSilver
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (CommThemeState.isLightMode) LightColorScheme else DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
