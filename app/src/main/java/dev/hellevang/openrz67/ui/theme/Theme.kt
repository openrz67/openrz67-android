package dev.hellevang.openrz67.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

private val LightColorScheme = lightColorScheme(
    primary = Color("#2A5555".toColorInt()),        // Deep Teal for primary buttons
    secondary = Color("#B8660A".toColorInt()),      // Burnt Orange for secondary actions
    background = Color("#FBE7C9".toColorInt()),     // Warm cream background
    surface = Color("#FFFEF8".toColorInt()),        // Warm white for cards/surfaces
    onPrimary = Color("#FFFEF8".toColorInt()),      // White text on primary
    onSecondary = Color("#FFFEF8".toColorInt()),    // White text on secondary
    onBackground = Color("#3D2914".toColorInt()),   // Dark brown for main text
    onSurface = Color("#2C1810".toColorInt())       // Rich espresso for surface text
)

@Composable
fun OpenRZ67Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}