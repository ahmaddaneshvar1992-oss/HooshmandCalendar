package ir.hooshmandcalendar.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------- پوسته‌های رنگی (۵ پوسته) ----------
data class AccentPalette(
    val name: String,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val onPrimary: Color
)

val ACCENTS = listOf(
    AccentPalette("آبی", Color(0xFF1E88E5), Color(0xFF1565C0), Color(0xFFE3F0FC), Color.White),
    AccentPalette("سبز", Color(0xFF10B981), Color(0xFF059669), Color(0xFFE3F7EF), Color.White),
    AccentPalette("بنفش", Color(0xFF8B5CF6), Color(0xFF7C3AED), Color(0xFFF1EBFE), Color.White),
    AccentPalette("نارنجی", Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFFEF0E3), Color.White),
    AccentPalette("سرمه‌ای", Color(0xFF3F51B5), Color(0xFF303F9F), Color(0xFFE8EAF6), Color.White)
)

// ---------- رنگ‌های رویداد (۸ رنگ مطابق طراحی) ----------
val EVENT_COLORS = listOf(
    Color(0xFFEF4444), // قرمز
    Color(0xFFF97316), // نارنجی
    Color(0xFFEAB308), // زرد
    Color(0xFF22C55E), // سبز
    Color(0xFF3B82F6), // آبی
    Color(0xFF6366F1), // نیلی
    Color(0xFFA855F7), // بنفش
    Color(0xFF1D4ED8)  // آبی تیره
)

val EVENT_COLOR_NAMES = listOf("قرمز", "نارنجی", "زرد", "سبز", "آبی", "نیلی", "بنفش", "سرمه‌ای")

// ---------- رنگ‌های عمومی ----------
val HolidayRed = Color(0xFFE53935)
val HolidayRedDark = Color(0xFFEF9A9A)
val WeekendRed = Color(0xFFEF5350)
val WeekendRedDark = Color(0xFFEF9A9A)

// Light scheme
val LightBackground = Color(0xFFF6F8FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF2F7)
val LightOnBackground = Color(0xFF191C20)
val LightOnSurface = Color(0xFF191C20)
val LightOutline = Color(0xFFDDE3EB)

// Dark scheme
val DarkBackground = Color(0xFF121417)
val DarkSurface = Color(0xFF1A1D22)
val DarkSurfaceVariant = Color(0xFF23272E)
val DarkOnBackground = Color(0xFFE2E4E8)
val DarkOnSurface = Color(0xFFE2E4E8)
val DarkOutline = Color(0xFF3A4048)
