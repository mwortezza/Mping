package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MpingLiquidGlassColorScheme = darkColorScheme(
    primary = LiquidCyan,
    onPrimary = SpaceBlack,
    primaryContainer = ElectricIndigo,
    onPrimaryContainer = TextHigh,
    secondary = NeonRose,
    onSecondary = Color.White,
    secondaryContainer = CyberViolet,
    onSecondaryContainer = TextHigh,
    tertiary = PlasmaSky,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextHigh,
    surface = SpaceSurface,
    onSurface = TextHigh,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = TextMedium,
    outline = GlassBorderHighlight,
    outlineVariant = GlassBorderSubtle
)

@Composable
fun MpingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MpingLiquidGlassColorScheme,
        typography = Typography,
        content = content
    )
}
