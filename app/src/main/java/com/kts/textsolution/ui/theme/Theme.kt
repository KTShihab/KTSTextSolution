package com.kts.textsolution.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF141210)
private val Paper = Color(0xFFF3EEE4)
private val Teal = Color(0xFF3D8F86)
private val TealDeep = Color(0xFF1F6B64)
private val SurfaceDark = Color(0xFF1C1A17)
private val SurfaceLight = Color(0xFFFFFDF8)

@Composable
fun KtsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        highContrast && darkTheme -> darkColorScheme(
            primary = Color(0xFF00E0C8),
            onPrimary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color.Black,
            onSurface = Color.White,
            error = Color(0xFFFF6B5A),
        )
        highContrast && !darkTheme -> lightColorScheme(
            primary = Color(0xFF005C56),
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            error = Color(0xFF9B1200),
        )
        darkTheme -> darkColorScheme(
            primary = Teal,
            onPrimary = Paper,
            background = Ink,
            onBackground = Paper,
            surface = SurfaceDark,
            onSurface = Paper,
            error = Color(0xFFC45C4A),
        )
        else -> lightColorScheme(
            primary = TealDeep,
            onPrimary = Paper,
            background = Paper,
            onBackground = Ink,
            surface = SurfaceLight,
            onSurface = Ink,
            error = Color(0xFFA33B2C),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
