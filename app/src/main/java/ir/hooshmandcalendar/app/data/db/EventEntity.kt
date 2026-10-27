package ir.hooshmandcalendar.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** انواع تکرار */
object RepeatType {
    const val NONE = 0
    const val DAILY = 1
    const val WEEKLY = 2
    const val MONTHLY = 3
    const val YEARLY = 4
}

/**
 * رویداد ذخیره‌شده در پایگاه داده محلی (کاملاً آفلاین).
 * تاریخ به‌صورت شمسی ذخیره می‌شود و dayEpochDay برای جست‌وجوی سریع نگه داشته می‌شود.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    /** تاریخ شمسی */
    val jYear: Int,
    val jMonth: Int,
    val jDay: Int,
    /** روز میلادی معادل (epoch day) برای جست‌وجوی بازه‌ای */
    val dayEpochDay: Long,
    /** تمام‌روز بودن */
    val allDay: Boolean = false,
    /** دقیقه از نیمه‌شب */
    val startMinutes: Int = 540,
    val endMinutes: Int? = null,
    /** اندیس رنگ از ۸ رنگ برنامه */
    val colorIndex: Int = 0,
    /** دقیقه‌های قبل از رویداد برای یادآور؛ null = بدون یادآور */
    val reminderOffsetMinutes: Int? = null,
    /** یکی از RepeatType */
    val repeatType: Int = RepeatType.NONE,
    /** آدرس صدا (Ringtone URI)؛ null = پیش‌فرض سیستم */
    val soundUri: String? = null,
    /** لرزش */
    val vibrate: Boolean = true,
    /** تولد یا سالگرد (نمایش آیکون کیک/هدیه) */
    val isBirthday: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
