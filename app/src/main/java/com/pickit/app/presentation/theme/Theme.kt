package com.pickit.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = SurfaceCard,
    primaryContainer = BrandPrimaryContainer,
    secondary = BrandSecondary,
    tertiary = ActionAccent,
    background = SurfaceBase,
    surface = SurfaceCard,
    outline = BorderSubtle,
    error = ErrorStrong,
)

private val DarkColors = darkColorScheme(
    primary = BrandSecondary,
    secondary = BrandSecondary,
    tertiary = ActionAccent,
)

@Composable
fun PickItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PickItTypography,
        content = content,
    )
}
