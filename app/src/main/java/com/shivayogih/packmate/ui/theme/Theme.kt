package com.shivayogih.packmate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF356A35),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F1B0),
    onPrimaryContainer = Color(0xFF002204),
    secondary = Color(0xFF52634F),
    tertiary = Color(0xFF38656A),
    surface = Color(0xFFF9FAF5),
    surfaceVariant = Color(0xFFDEE5D9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD696),
    onPrimary = Color(0xFF07390F),
    primaryContainer = Color(0xFF1E5123),
    onPrimaryContainer = Color(0xFFB8F1B0),
    secondary = Color(0xFFB9CCB4),
    tertiary = Color(0xFFA0CFD4),
)

@Composable
fun PackMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
            dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
