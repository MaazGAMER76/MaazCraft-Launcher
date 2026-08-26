package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MaazCraftDarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = TextPrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleAccent,
    secondary = PurpleAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = PurpleDarkBorder,
    onSecondaryContainer = PurpleLight,
    tertiary = CyanInfo,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = PurpleDarkBorder,
    outlineVariant = PurplePrimary.copy(alpha = 0.3f),
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaazCraftDarkColorScheme,
        typography = Typography,
        content = content
    )
}
