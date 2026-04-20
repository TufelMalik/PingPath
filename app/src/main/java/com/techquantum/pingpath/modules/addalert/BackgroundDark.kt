package com.techquantum.pingpath.modules.addalert

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Exact Hex Colors mapped from HTML
val BackgroundDark = Color(0xFF0D0D0D)
val SurfaceDark = Color(0xFF131313)
val SurfaceContainer = Color(0xFF1A1A1A)
val SurfaceContainerHigh = Color(0xFF2A2A2A)
val SurfaceContainerHighest = Color(0xFF353534)
val PrimaryCyan = Color(0xFF44DDC1)
val PrimaryCyanDark = Color(0xFF00BFA5)
val SecondaryPurple = Color(0xFFCDBDFF)
val TertiaryOrange = Color(0xFFFFBA38)
val OnSurfaceText = Color(0xFFFFFFFF)
val OnSurfaceVariantText = Color(0xFF9E9E9E)
val OutlineVariant = Color(0xFF3C4A46)

private val AppColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceContainer,
    primary = PrimaryCyan,
    primaryContainer = PrimaryCyanDark,
    secondary = SecondaryPurple,
    tertiary = TertiaryOrange,
    onBackground = OnSurfaceText,
    onSurface = OnSurfaceText,
    onSurfaceVariant = OnSurfaceVariantText,
    outlineVariant = OutlineVariant
)

// Inter Typography mappings (scalable sp matched 1:1 with HTML px for visual parity)
val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, // Inter assumed
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)

val RadiusDefault = 12.dp // 16px * 0.75
val RadiusLarge = 18.dp

@Composable
fun ProximAlertTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}