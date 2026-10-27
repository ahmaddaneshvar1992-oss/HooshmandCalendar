package ir.hooshmandcalendar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ir.hooshmandcalendar.app.data.ThemeMode

private fun lightScheme(accent: AccentPalette) = lightColorScheme(
    primary = accent.primary,
    onPrimary = accent.onPrimary,
    primaryContainer = accent.primaryLight,
    onPrimaryContainer = accent.primaryDark,
    secondary = accent.primaryDark,
    onSecondary = accent.onPrimary,
    secondaryContainer = accent.primaryLight,
    onSecondaryContainer = accent.primaryDark,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A626C),
    outline = LightOutline,
    error = HolidayRed,
    surfaceTint = Color.Transparent
)

private fun darkScheme(accent: AccentPalette) = darkColorScheme(
    primary = accent.primaryLight.darken(0.25f),
    onPrimary = Color(0xFF0B1B2B),
    primaryContainer = accent.primaryDark,
    onPrimaryContainer = Color(0xFFEAF4FF),
    secondary = accent.primaryLight,
    onSecondary = Color(0xFF0B1B2B),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA6ADB6),
    outline = DarkOutline,
    error = HolidayRedDark,
    surfaceTint = Color.Transparent
)

private fun Color.darken(f: Float): Color = Color(
    (red * (1f - f)).coerceIn(0f, 1f),
    (green * (1f - f)).coerceIn(0f, 1f),
    (blue * (1f - f)).coerceIn(0f, 1f),
    alpha
)

/** رنگ قرمز تعطیلات مطابق تم روشن/تاریک */
@Composable
fun holidayColor(): Color =
    if (isSystemInDarkTheme()) HolidayRedDark else HolidayRed

@Composable
fun HooshmandTheme(
    themeMode: ThemeMode,
    accentIndex: Int,
    content: @Composable () -> Unit
) {
    val accent = ACCENTS.getOrElse(accentIndex) { ACCENTS[0] }
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) darkScheme(accent) else lightScheme(accent),
        typography = AppTypography,
        content = content
    )
}
