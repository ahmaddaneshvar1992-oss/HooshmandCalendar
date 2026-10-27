package ir.hooshmandcalendar.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** حالت نمایش تقویم */
enum class CalendarViewMode { MONTH, WEEK, LIST }

/** حالت پوسته (روشن/تاریک/سیستم) */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentIndex: Int = 0,
    val persianDigits: Boolean = true,
    val showOccasions: Boolean = true,
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val selectedEpochDay: Long = 0L,
    val showTodayNotification: Boolean = true,
    val azanEnabled: Boolean = true,
    val cityIndex: Int = 0,
    val azanMethodIndex: Int = 0
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val ACCENT = intPreferencesKey("accent_index")
        val PERSIAN_DIGITS = booleanPreferencesKey("persian_digits")
        val SHOW_OCCASIONS = booleanPreferencesKey("show_occasions")
        val VIEW_MODE = intPreferencesKey("view_mode")
        val SELECTED_DAY = longPreferencesKey("selected_epoch_day")
        val TODAY_NOTIF = booleanPreferencesKey("today_notification")
        val AZAN_ENABLED = booleanPreferencesKey("azan_enabled")
        val CITY_INDEX = intPreferencesKey("city_index")
        val AZAN_METHOD = intPreferencesKey("azan_method")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = ThemeMode.entries.getOrElse(p[Keys.THEME] ?: 0) { ThemeMode.SYSTEM },
            accentIndex = p[Keys.ACCENT] ?: 0,
            persianDigits = p[Keys.PERSIAN_DIGITS] ?: true,
            showOccasions = p[Keys.SHOW_OCCASIONS] ?: true,
            viewMode = CalendarViewMode.entries.getOrElse(p[Keys.VIEW_MODE] ?: 0) { CalendarViewMode.MONTH },
            selectedEpochDay = p[Keys.SELECTED_DAY] ?: 0L,
            showTodayNotification = p[Keys.TODAY_NOTIF] ?: true,
            azanEnabled = p[Keys.AZAN_ENABLED] ?: true,
            cityIndex = p[Keys.CITY_INDEX] ?: 0,
            azanMethodIndex = p[Keys.AZAN_METHOD] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.ordinal }
    suspend fun setAccent(index: Int) = context.dataStore.edit { it[Keys.ACCENT] = index }
    suspend fun setPersianDigits(enabled: Boolean) = context.dataStore.edit { it[Keys.PERSIAN_DIGITS] = enabled }
    suspend fun setShowOccasions(enabled: Boolean) = context.dataStore.edit { it[Keys.SHOW_OCCASIONS] = enabled }
    suspend fun setViewMode(mode: CalendarViewMode) = context.dataStore.edit { it[Keys.VIEW_MODE] = mode.ordinal }
    suspend fun setSelectedDay(epochDay: Long) = context.dataStore.edit { it[Keys.SELECTED_DAY] = epochDay }
    suspend fun setShowTodayNotification(enabled: Boolean) = context.dataStore.edit { it[Keys.TODAY_NOTIF] = enabled }
    suspend fun setAzanEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.AZAN_ENABLED] = enabled }
    suspend fun setCityIndex(index: Int) = context.dataStore.edit { it[Keys.CITY_INDEX] = index }
    suspend fun setAzanMethod(index: Int) = context.dataStore.edit { it[Keys.AZAN_METHOD] = index }
}
