package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = GpuCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F59),
    onPrimaryContainer = Color(0xFFB5F3FF),
    secondary = AcceleratorPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF371E70),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = HardwareGreen,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005327),
    onTertiaryContainer = Color(0xFF88FFB4),
    background = LimboBgDark,
    onBackground = TextPrimaryDark,
    surface = LimboSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = LimboSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainer = LimboSurfaceContainerDark,
    outline = BorderSubtleDark,
    outlineVariant = Color(0xFF1B253B),
    error = DangerCrimson,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GpuCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F4FB),
    onPrimaryContainer = Color(0xFF002024),
    secondary = AcceleratorPurpleDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADBFF),
    onSecondaryContainer = Color(0xFF220A55),
    tertiary = HardwareGreenDark,
    onTertiary = Color.White,
    background = LimboBgLight,
    onBackground = TextPrimaryLight,
    surface = LimboSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = LimboSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainer = LimboSurfaceContainerLight,
    outline = Color(0xFFCBD5E1),
    error = DangerCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark workstation theme
    dynamicColor: Boolean = false, // Keep high-contrast PC GPU branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
