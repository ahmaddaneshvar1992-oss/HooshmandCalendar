package ir.hooshmandcalendar.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.hooshmandcalendar.app.data.EventRepository
import ir.hooshmandcalendar.app.data.OccurrenceEngine
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.data.db.RepeatType
import ir.hooshmandcalendar.app.core.DayTime
import ir.hooshmandcalendar.app.core.JalaliDate
import kotlinx.coroutines.runBlocking

/**
 * زمان‌بندی یادآورها با AlarmManager.
 * پس از روشن شدن گوشی، تغییر ساعت یا به‌روزرسانی برنامه، BootReceiver دوباره زمان‌بندی می‌کند.
 */
object ReminderScheduler {

    const val ACTION_FIRE = "ir.hooshmandcalendar.app.ACTION_FIRE_REMINDER"
    const val EXTRA_EVENT_ID = "event_id"

    /** زمان‌بندی همه یادآورهای آینده (در شروع برنامه و پس از بوت) */
    fun scheduleAll(context: Context) {
        val repo = EventRepository.get(context)
        val now = System.currentTimeMillis()
        val events = try {
            runBlocking { repo.scheduledEvents(JalaliDate.today().epochDay) }
        } catch (_: Exception) {
            emptyList()
        }
        for (event in events) {
            val offset = event.reminderOffsetMinutes ?: continue
            val next = OccurrenceEngine.nextTriggerAt(event, offset, now) ?: continue
            scheduleAt(context, event.id, next)
        }
    }

    /** زمان‌بندی (یا باز-time‌بندی) یادآور یک رویداد */
    fun scheduleEvent(context: Context, event: EventEntity) {
        cancelEvent(context, event.id)
        val offset = event.reminderOffsetMinutes ?: return
        val next = OccurrenceEngine.nextTriggerAt(event, offset, System.currentTimeMillis()) ?: return
        scheduleAt(context, event.id, next)
    }

    fun cancelEvent(context: Context, eventId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, eventId))
    }

    private fun scheduleAt(context: Context, eventId: Long, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, eventId)
        val canExact = if (Build.VERSION.SDK_INT >= 31) am.canScheduleExactAlarms() else true
        try {
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 10 * 60 * 1000L, pi)
            }
        } catch (_: SecurityException) {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 10 * 60 * 1000L, pi)
        }
    }

    private fun pendingIntent(context: Context, eventId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        return PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** دریافت زنگ یادآور: نمایش اعلان + زمان‌بندی تکرار بعدی */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_FIRE) return
        val eventId = intent.getLongExtra(ReminderScheduler.EXTRA_EVENT_ID, -1L)
        if (eventId <= 0) return

        val repo = EventRepository.get(context)
        val event = runBlocking { repo.getById(eventId) } ?: return

        val j = JalaliDate(event.jYear, event.jMonth, event.jDay)
        val timePart = if (event.allDay) "امروز" else "ساعت ${DayTime(event.startMinutes).format()}"
        val text = buildString {
            append(timePart)
            append(" - ")
            append(j.shortFormat())
            if (event.description.isNotBlank()) {
                append("\n")
                append(event.description)
            }
        }

        Notifications.showEventNotification(
            context, eventId, event.title, text, event.soundUri, event.vibrate
        )

        // تکرار بعدی
        if (event.repeatType != RepeatType.NONE) {
            ReminderScheduler.scheduleEvent(context, event)
        }
    }
}
