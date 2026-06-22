package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// CommCore Premium Midnight Design Tokens - Dynamic getters based on global light/dark state

object CommThemeState {
    var isLightMode by mutableStateOf(false)
}

val MidnightBg: Color get() = if (CommThemeState.isLightMode) Color(0xFFF1F5F9) else Color(0xFF0C091A)
val MidnightSurface: Color get() = if (CommThemeState.isLightMode) Color(0xFFFFFFFF) else Color(0xFF15112B)
val MidnightSurfaceCard: Color get() = if (CommThemeState.isLightMode) Color(0xFFE2E8F0) else Color(0xFF1F1B3D)
val CyberPurple: Color get() = if (CommThemeState.isLightMode) Color(0xFF6D28D9) else Color(0xFF8B5CF6)
val CyberMint: Color get() = if (CommThemeState.isLightMode) Color(0xFF059669) else Color(0xFF10B981)
val GoldAccent: Color get() = if (CommThemeState.isLightMode) Color(0xFFD97706) else Color(0xFFF59E0B)
val LightSilver: Color get() = if (CommThemeState.isLightMode) Color(0xFF0F172A) else Color(0xFFE2E8F0)
val DarkSilver: Color get() = if (CommThemeState.isLightMode) Color(0xFF475569) else Color(0xFF94A3B8)
val BorderGlass: Color get() = if (CommThemeState.isLightMode) Color(0x336D28D9) else Color(0x228B5CF6)
