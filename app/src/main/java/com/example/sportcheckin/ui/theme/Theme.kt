package com.example.sportcheckin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandOrange,
    onPrimary = Color.White,
    primaryContainer = SoftOrange,
    onPrimaryContainer = BrandOrangeDark,
    secondary = MintGreen,
    onSecondary = Color.White,
    secondaryContainer = SoftGreen,
    onSecondaryContainer = Color(0xFF0C6B4D),
    tertiary = WarningYellow,
    background = PageBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFD7DBE0)
)

private val DarkColors = darkColorScheme(
    primary = BrandOrange,
    secondary = MintGreen,
    tertiary = WarningYellow
)

@Composable
fun SportCheckInTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
