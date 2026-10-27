package ir.hooshmandcalendar.app.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.hooshmandcalendar.app.core.DayTime
import ir.hooshmandcalendar.app.data.Cities
import ir.hooshmandcalendar.app.data.SettingsStore
import ir.hooshmandcalendar.app.reminders.Notifications
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.TimeZone

/**
 * زمان‌بندی خودکار اذان با AlarmManager.
 * در هر لحظه فقط زنگ «اذان بعدی» برنامه‌ریزی می‌شود و پس از هر اذان،
 * زنگ بعدی دوباره تنظیم می‌شود. کاملاً آفلاین.
 */
object AzanScheduler {

    const val ACTION_FIRE = "ir.hooshmandcalendar.app.ACTION_AZAN_FIRE"
    const val EXTRA_KIND = "azan_kind"
    private const val REQUEST_CODE = 8771

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, 0))
    }

    /** بر اساس تنظیمات (شهر/روش/فعال بودن) زنگ بعدی را می‌چیند */
    fun reschedule(context: Context) {
        try {
            val s = runBlocking { SettingsStore(context).settings.first() }
            val city = Cities.safe(s.cityIndex)
            val method = PrayerTimes.Method.entries.getOrElse(s.azanMethodIndex) { PrayerTimes.Method.IRAN }
            if (s.azanEnabled) scheduleNext(context, city, method) else cancel(context)
        } catch (_: Exception) {
        }
    }

    /** نزدیک‌ترین اذان آینده (تا ۳ روز پیش‌رو) را زمان‌بندی می‌کند */
    fun scheduleNext(context: Context, city: ir.hooshmandcalendar.app.data.City, method: PrayerTimes.Method) {
        try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val now = System.currentTimeMillis()
            val tzOff = TimeZone.getDefault().getOffset(now)
            val todayEpochDay = (now + tzOff) / 86400000L

            for (dayOffset in 0..2L) {
                val epochDay = todayEpochDay + dayOffset
                val times = PrayerTimes.computeForEpochDay(epochDay, city, method)
                for (kind in PrayerTimes.Kind.azanKinds) {
                    val fireAt = epochDay * 86400000L - tzOff + times.minutesOf(kind) * 60000L
                    if (fireAt > now + 15_000L) {
                        val kindIndex = PrayerTimes.Kind.entries.indexOf(kind)
                        val pi = pendingIntent(context, kindIndex)
                        val canExact = if (Build.VERSION.SDK_INT >= 31) am.canScheduleExactAlarms() else true
                        try {
                            if (canExact) {
                                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
                            } else {
                                am.setWindow(AlarmManager.RTC_WAKEUP, fireAt, 10 * 60 * 1000L, pi)
                            }
                        } catch (_: SecurityException) {
                            am.setWindow(AlarmManager.RTC_WAKEUP, fireAt, 10 * 60 * 1000L, pi)
                        }
                        return
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun pendingIntent(context: Context, kindIndex: Int): PendingIntent {
        val intent = Intent(context, AzanReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_KIND, kindIndex)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** دریافت زنگ اذان: نمایش اعلان با صدای اذان + زمان‌بندی اذان بعدی */
class AzanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AzanScheduler.ACTION_FIRE) return

        try {
            val kindIndex = intent.getIntExtra(AzanScheduler.EXTRA_KIND, -1)
            val kind = PrayerTimes.Kind.entries.getOrElse(kindIndex) { PrayerTimes.Kind.DHUHR }

            val s = runBlocking { SettingsStore(context).settings.first() }
            val city = Cities.safe(s.cityIndex)
            val method = PrayerTimes.Method.entries.getOrElse(s.azanMethodIndex) { PrayerTimes.Method.IRAN }

            val times = PrayerTimes.computeToday(city, method)
            val timeStr = DayTime(times.minutesOf(kind)).format()

            Notifications.showAzanNotification(
                context,
                kind.label,
                "${city.name} - ساعت $timeStr"
            )

            // زنگ اذان بعدی
            AzanScheduler.scheduleNext(context, city, method)
        } catch (_: Exception) {
        }
    }
}
