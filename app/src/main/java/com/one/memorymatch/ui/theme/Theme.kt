package com.one.memorymatch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KidsColorScheme = lightColorScheme(
    primary = ButtonPrimary,
    onPrimary = TextLight,
    primaryContainer = ButtonPrimaryDark,
    secondary = ButtonSecondary,
    onSecondary = TextLight,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark
)

@Composable
fun KidsMemoryTheme(
    content: @Composable () -> Unit
) {
    // V1 kids app: always cheerful bright light theme
    MaterialTheme(
        colorScheme = KidsColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
