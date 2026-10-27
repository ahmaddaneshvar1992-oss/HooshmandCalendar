package ir.hooshmandcalendar.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.data.AppSettings
import ir.hooshmandcalendar.app.data.EventRepository
import ir.hooshmandcalendar.app.data.OccurrenceEngine
import ir.hooshmandcalendar.app.data.SettingsStore
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.reminders.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ویومدل مشترک برنامه: تاریخ انتخابی، رویدادها و تنظیمات */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    val settingsStore = SettingsStore(app)
    private val repo = EventRepository.get(app)

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val allEvents: StateFlow<List<EventEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDate = MutableStateFlow(JalaliDate.today())
    val selectedDate: StateFlow<JalaliDate> = _selectedDate.asStateFlow()

    init {
        // بازیابی تاریخ انتخاب‌شده در اجرای قبلی
        viewModelScope.launch {
            val saved = settingsStore.settings.first()
            if (saved.selectedEpochDay > 0) {
                _selectedDate.value = JalaliDate.fromEpochDay(saved.selectedEpochDay)
            }
        }
    }

    fun selectDate(date: JalaliDate) {
        _selectedDate.value = date
        viewModelScope.launch { settingsStore.setSelectedDay(date.epochDay) }
    }

    /** رویدادهای یک روز با احتساب تکرار */
    fun eventsFor(date: JalaliDate, events: List<EventEntity>): List<EventEntity> =
        events.filter { OccurrenceEngine.occursOn(it, date) }
            .sortedWith(compareBy({ it.allDay.not() }, { it.startMinutes }))

    /** نقشه epochDay → رنگ‌ها برای نمایش نقطه‌های رویداد در یک بازه */
    fun eventColorsInRange(
        events: List<EventEntity>,
        from: JalaliDate,
        to: JalaliDate
    ): Map<Long, List<Int>> {
        val map = mutableMapOf<Long, MutableList<Int>>()
        for (event in events) {
            val color = if (event.colorIndex in 0 until 8) event.colorIndex else 0
            for (day in OccurrenceEngine.occurrencesInRange(event, from, to)) {
                map.getOrPut(day) { mutableListOf() }.add(color)
            }
        }
        return map
    }

    suspend fun getEvent(id: Long): EventEntity? = repo.getById(id)

    fun saveEvent(event: EventEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.upsert(event)
            val saved = if (id > 0) event.copy(id = id) else event
            ReminderScheduler.scheduleEvent(getApplication(), saved)
            onDone(saved.id)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repo.delete(event)
            ReminderScheduler.cancelEvent(getApplication(), event.id)
        }
    }
}
