package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val primaryPreviewColor: Color,
    val surfacePreviewColor: Color,
    val isDark: Boolean
) {
    LIGHT("Modern Light", "Crisp Indigo & Slate", IndigoPrimary, Color.White, false),
    DARK("Midnight OLED", "Deep Slate & Indigo Glow", IndigoLight, Color(0xFF1E293B), true),
    CLASSIC("Classic Sepia", "Warm Paper & Amber Brown", ClassicPrimary, ClassicSurface, false),
    FOREST("Emerald Focus", "Botanical Green & Fresh Mint", ForestPrimary, ForestSurface, false),
    SUNSET("Sunset Bloom", "Rose Coral & Soft Amber", SunsetPrimary, SunsetSurface, false);

    companion object {
        fun fromString(value: String?): AppThemeMode =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: LIGHT
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color.White,
    primaryContainer = IndigoDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = Color(0xFFFBBF24),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = IndigoDark,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = AmberTertiary,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569)
)

private val ClassicColorScheme = lightColorScheme(
    primary = ClassicPrimary,
    onPrimary = Color.White,
    primaryContainer = ClassicPrimaryContainer,
    onPrimaryContainer = ClassicSecondary,
    secondary = ClassicSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFE7DB),
    onSecondaryContainer = Color(0xFF292524),
    tertiary = Color(0xFFB45309),
    background = ClassicBackground,
    surface = ClassicSurface,
    surfaceVariant = ClassicSurfaceVariant,
    onBackground = ClassicOnBackground,
    onSurface = ClassicOnBackground,
    onSurfaceVariant = Color(0xFF57534E)
)

private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestSecondary,
    secondary = ForestSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = Color(0xFF0D9488),
    background = ForestBackground,
    surface = ForestSurface,
    surfaceVariant = ForestSurfaceVariant,
    onBackground = ForestOnBackground,
    onSurface = ForestOnBackground,
    onSurfaceVariant = Color(0xFF166534)
)

private val SunsetColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = SunsetPrimaryContainer,
    onPrimaryContainer = SunsetSecondary,
    secondary = SunsetSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECEF),
    onSecondaryContainer = Color(0xFF4C0519),
    tertiary = Color(0xFFEA580C),
    background = SunsetBackground,
    surface = SunsetSurface,
    surfaceVariant = SunsetSurfaceVariant,
    onBackground = SunsetOnBackground,
    onSurface = SunsetOnBackground,
    onSurfaceVariant = Color(0xFF881337)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.CLASSIC -> ClassicColorScheme
        AppThemeMode.FOREST -> ForestColorScheme
        AppThemeMode.SUNSET -> SunsetColorScheme
        AppThemeMode.LIGHT -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context)
            } else {
                LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
