package com.example.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Schema colori con sfondi scuri eleganti ad alto contrasto e testi 100% bianchi
private val AppColorScheme = darkColorScheme(
    primary = SagePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = CartaDaZuccheroDark,
    onSecondary = Color.White,
    secondaryContainer = CartaDaZuccheroContainer,
    onSecondaryContainer = Color.White,
    tertiary = TerracottaAccentDark,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = Color.White,
    background = PannaBackgroundDark,
    surface = PannaSurfaceDark,
    surfaceVariant = PannaSurfaceVariantDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    outline = PannaOutline,
    outlineVariant = PannaOutlineVariant
)

@Composable
fun MealPlanTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White
        ) {
            content()
        }
    }
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MealPlanTheme(darkTheme = darkTheme, content = content)
