package dev.hellevang.openrz67.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2A5555),          // Deep teal
    onPrimary = Color(0xFFFFFEF8),
    secondary = Color(0xFFB8660A),        // Burnt orange, used for "active/stop"
    onSecondary = Color(0xFFFFFEF8),
    secondaryContainer = Color(0xFFCDE0DD), // Selected segment
    onSecondaryContainer = Color(0xFF1C3A3A),
    background = Color(0xFFFBE7C9),       // Warm cream, matches the illustration
    onBackground = Color(0xFF3D2914),
    surface = Color(0xFFFFFEF8),
    onSurface = Color(0xFF2C1810),
    surfaceVariant = Color(0xFFF1DDBB),
    onSurfaceVariant = Color(0xFF5C4630),
    outline = Color(0xFF8C7458)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6FB3B3),
    onPrimary = Color(0xFF0F2A2A),
    secondary = Color(0xFFE8963A),
    onSecondary = Color(0xFF2C1810),
    secondaryContainer = Color(0xFF2E5252),
    onSecondaryContainer = Color(0xFFD8EEEE),
    background = Color(0xFF1B140E),       // Dark brown, easy on night vision
    onBackground = Color(0xFFF1E3CC),
    surface = Color(0xFF241B13),
    onSurface = Color(0xFFF1E3CC),
    surfaceVariant = Color(0xFF33281E),
    onSurfaceVariant = Color(0xFFCBB79A),
    outline = Color(0xFF7A6448)
)

@Composable
fun OpenRZ67Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
        content = content
    )
}
