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
import com.example.ui.ThemeSetting

private val DarkColorScheme = darkColorScheme(
    primary = SonoraVioletDark,
    onPrimary = Color(0xFF1E1038),
    primaryContainer = SonoraVioletPrimaryDark,
    onPrimaryContainer = Color(0xFFF5F3FF),
    secondary = SonoraCoralDark,
    onSecondary = Color(0xFF4C0519),
    tertiary = SonoraCyanDark,
    onTertiary = Color(0xFF083344),
    background = SonoraBackgroundDark,
    onBackground = SonoraOnSurfaceDark,
    surface = SonoraSurfaceDark,
    onSurface = SonoraOnSurfaceDark,
    surfaceVariant = SonoraSurfaceVariantDark,
    onSurfaceVariant = SonoraOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = SonoraVioletLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = SonoraVioletLight,
    secondary = SonoraCoralLight,
    onSecondary = Color.White,
    tertiary = SonoraCyanLight,
    onTertiary = Color.White,
    background = SonoraBackgroundLight,
    onBackground = SonoraOnSurfaceLight,
    surface = SonoraSurfaceLight,
    onSurface = SonoraOnSurfaceLight,
    surfaceVariant = SonoraSurfaceVariantLight,
    onSurfaceVariant = SonoraOnSurfaceVariantLight
)

@Composable
fun SonoraTheme(
    themeSetting: ThemeSetting = ThemeSetting.SYSTEM,
    dynamicColor: Boolean = false, // Keep Sonora's distinct visual identity
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeSetting) {
        ThemeSetting.SYSTEM -> isSystemDark
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
