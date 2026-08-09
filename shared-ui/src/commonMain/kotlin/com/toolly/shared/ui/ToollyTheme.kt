package com.toolly.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle

internal object ToollyColors {
    val Primary = Color(0xFF2961F2)
    val PrimaryContainer = Color(0xFFE5ECFF)
    val Surface = Color(0xFFF5F7FA)
    val SurfaceStrong = Color(0xFFFFFFFF)
    val Outline = Color(0xFFC7CFD9)
    val SecondaryText = Color(0xFF616B78)
    val PrimaryText = Color(0xFF1C2129)
    val Positive = Color(0xFF1FA66F)
    val CameraSurface = Color(0xFF1C2129)
}

internal object ToollySpacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Screen = 24.dp
    val MinimumTarget = 48.dp
    val PrimaryActionHeight = 52.dp
}

private val ToollyLightColors = lightColorScheme(
    primary = ToollyColors.Primary,
    onPrimary = Color.White,
    primaryContainer = ToollyColors.PrimaryContainer,
    onPrimaryContainer = ToollyColors.PrimaryText,
    background = ToollyColors.Surface,
    onBackground = ToollyColors.PrimaryText,
    surface = ToollyColors.Surface,
    onSurface = ToollyColors.PrimaryText,
    surfaceVariant = ToollyColors.SurfaceStrong,
    onSurfaceVariant = ToollyColors.SecondaryText,
    outline = ToollyColors.Outline,
    error = Color(0xFFB3261E),
)

private val ToollyDarkColors = darkColorScheme(
    primary = Color(0xFF9CB7FF),
    onPrimary = Color(0xFF002F84),
    primaryContainer = Color(0xFF173F95),
    onPrimaryContainer = Color(0xFFDDE5FF),
    background = Color(0xFF11151B),
    onBackground = Color(0xFFF0F2F6),
    surface = Color(0xFF11151B),
    onSurface = Color(0xFFF0F2F6),
    surfaceVariant = Color(0xFF1C222B),
    onSurfaceVariant = Color(0xFFBBC3CF),
    outline = Color(0xFF596372),
    error = Color(0xFFFFB4AB),
)

private val ToollyTypography = Typography(
    displaySmall = TextStyle(
        fontSize = 34.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineMedium = TextStyle(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
)

private val ToollyShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
internal fun ToollyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ToollyDarkColors else ToollyLightColors,
        typography = ToollyTypography,
        shapes = ToollyShapes,
        content = content,
    )
}
