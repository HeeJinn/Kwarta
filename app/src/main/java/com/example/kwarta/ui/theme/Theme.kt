package com.example.kwarta.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFC4C4C4)
)

private val DarkBlueColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBAC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFF4FDBEE),
    onTertiary = Color(0xFF00363D),
    tertiaryContainer = Color(0xFF004F58),
    onTertiaryContainer = Color(0xFF97F0FF),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7D2),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E)
)

private val LightBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF006874),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF97F0FF),
    onTertiaryContainer = Color(0xFF002024),
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7D2)
)

private val DarkGreenColorScheme = darkColorScheme(
    primary = Color(0xFF7DDA9A),
    onPrimary = Color(0xFF00391B),
    primaryContainer = Color(0xFF005228),
    onPrimaryContainer = Color(0xFF98F7B5),
    secondary = Color(0xFFB6CBB9),
    onSecondary = Color(0xFF223428),
    secondaryContainer = Color(0xFF384B3E),
    onSecondaryContainer = Color(0xFFD2E8D5),
    tertiary = Color(0xFF7CDACF),
    onTertiary = Color(0xFF003738),
    tertiaryContainer = Color(0xFF004F50),
    onTertiaryContainer = Color(0xFF98F2F4),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF404942),
    onSurfaceVariant = Color(0xFFC0C9C0),
    outline = Color(0xFF8A938A),
    outlineVariant = Color(0xFF404942)
)

private val LightGreenColorScheme = lightColorScheme(
    primary = Color(0xFF006D3A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF98F7B5),
    onPrimaryContainer = Color(0xFF00210D),
    secondary = Color(0xFF4F6354),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E8D5),
    onSecondaryContainer = Color(0xFF0D1F13),
    tertiary = Color(0xFF00696B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF98F2F4),
    onTertiaryContainer = Color(0xFF002021),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DC),
    onSurfaceVariant = Color(0xFF404942),
    outline = Color(0xFF707971),
    outlineVariant = Color(0xFFC0C9C0)
)

private val DarkOrangeColorScheme = darkColorScheme(
    primary = Color(0xFFFFB868),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3B00),
    onPrimaryContainer = Color(0xFFFFDCBE),
    secondary = Color(0xFFE1C2A5),
    onSecondary = Color(0xFF402D18),
    secondaryContainer = Color(0xFF59432C),
    onSecondaryContainer = Color(0xFFFEDEBF),
    tertiary = Color(0xFFC0CD98),
    onTertiary = Color(0xFF2A3410),
    tertiaryContainer = Color(0xFF414B25),
    onTertiaryContainer = Color(0xFFDCFAB2),
    background = Color(0xFF201A15),
    onBackground = Color(0xFFECE0D6),
    surface = Color(0xFF201A15),
    onSurface = Color(0xFFECE0D6),
    surfaceVariant = Color(0xFF4F453B),
    onSurfaceVariant = Color(0xFFD4C4B7),
    outline = Color(0xFF9D8E81),
    outlineVariant = Color(0xFF4F453B)
)

private val LightOrangeColorScheme = lightColorScheme(
    primary = Color(0xFF8B5000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCBE),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = Color(0xFF725A42),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFEDEBF),
    onSecondaryContainer = Color(0xFF281805),
    tertiary = Color(0xFF58633A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCFAB2),
    onTertiaryContainer = Color(0xFF161E01),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF201A15),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF201A15),
    surfaceVariant = Color(0xFFF1E0D2),
    onSurfaceVariant = Color(0xFF4F453B),
    outline = Color(0xFF817567),
    outlineVariant = Color(0xFFD4C4B7)
)

private val DarkMonoColorScheme = darkColorScheme(
    primary = Color(0xFFE3E3E3),
    onPrimary = Color(0xFF1C1C1C),
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFE3E3E3),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF2C2C2C),
    secondaryContainer = Color(0xFF4A4A4A),
    onSecondaryContainer = Color(0xFFE3E3E3),
    tertiary = Color(0xFFA0A0A0),
    onTertiary = Color(0xFF1C1C1C),
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = Color(0xFFE3E3E3),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF2C2C2C)
)

private val LightMonoColorScheme = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF0F0F0F),
    secondary = Color(0xFF5A5A5A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEAEAEA),
    onSecondaryContainer = Color(0xFF1E1E1E),
    tertiary = Color(0xFF606060),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2E2E2),
    onTertiaryContainer = Color(0xFF1C1C1C),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE2E2E2),
    onSurfaceVariant = Color(0xFF4A4A4A),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFE2E2E2)
)

@Composable
fun KwartaTheme(
    themeMode: String = "SYSTEM",
    themeColor: String = "PURPLE",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (themeColor) {
        "BLUE" -> if (darkTheme) DarkBlueColorScheme else LightBlueColorScheme
        "GREEN" -> if (darkTheme) DarkGreenColorScheme else LightGreenColorScheme
        "ORANGE" -> if (darkTheme) DarkOrangeColorScheme else LightOrangeColorScheme
        "BLACK" -> if (darkTheme) DarkMonoColorScheme else LightMonoColorScheme
        else -> {
            if (themeColor == "DYNAMIC" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}