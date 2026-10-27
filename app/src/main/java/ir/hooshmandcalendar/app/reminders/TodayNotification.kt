package ir.hooshmandcalendar.app.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ir.hooshmandcalendar.app.MainActivity
import ir.hooshmandcalendar.app.R
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.toPersianDigits

/**
 * اعلان دائمی تقویم: تاریخ امروز همیشه در نوار وضعیت دیده می‌شود.
 * هر شب نیمه‌شب به‌صورت خودکار تازه‌سازی می‌شود.
 */
object TodayNotification {

    const val CHANNEL_TODAY = "today_date"
    const val NOTIFICATION_ID = 4242
    const val ACTION_MIDNIGHT = "ir.hooshmandcalendar.app.ACTION_MIDNIGHT_REFRESH"
    private const val MIDNIGHT_REQUEST_CODE = 4301

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_TODAY,
            "تاریخ امروز در نوار وضعیت",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "نمایش بی‌صدای تاریخ امروز شمسی در نوار وضعیت"
            setSound(null, null)
            enableVibration(false)
        }
        nm.createNotificationChannel(channel)
    }

    /** نمایش/به‌روزرسانی اعلان تاریخ امروز */
    fun post(context: Context) {
        ensureChannel(context)
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return

        val j = JalaliDate.today()
        val h = j.toHijri()
        val g = j.toGregorian()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 99, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "امروز: ${j.longFormat()}"
        val text = "${h.shortFormat()}  •  ${g.day.toPersianDigits()} ${g.monthName} ${g.year.toPersianDigits()}"

        val builder = NotificationCompat.Builder(context, CHANNEL_TODAY)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pending)

        try {
            nm.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun cancel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }

    /** زمان‌بندی تازه‌سازی خودکار در نخستین دقیقه‌های فردا */
    fun scheduleMidnightRefresh(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        val tzOff = java.util.TimeZone.getDefault().getOffset(now)
        val nextMidnight = ((now + tzOff) / 86400000L + 1L) * 86400000L - tzOff + 20_000L

        val intent = Intent(context, TodayNotificationReceiver::class.java).apply {
            action = ACTION_MIDNIGHT
        }
        val pi = PendingIntent.getBroadcast(
            context, MIDNIGHT_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            val canExact = if (Build.VERSION.SDK_INT >= 31) am.canScheduleExactAlarms() else true
            if (canExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMidnight, pi)
            } else {
                am.setWindow(AlarmManager.RTC_WAKEUP, nextMidnight, 15 * 60 * 1000L, pi)
            }
        } catch (_: SecurityException) {
            am.setWindow(AlarmManager.RTC_WAKEUP, nextMidnight, 15 * 60 * 1000L, pi)
        }
    }

    fun postAndSchedule(context: Context) {
        post(context)
        scheduleMidnightRefresh(context)
    }
}

/** تازه‌سازی شبانه اعلان تاریخ + همگام‌سازی اذان در روز جدید */
class TodayNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TodayNotification.ACTION_MIDNIGHT) return
        try {
            TodayNotification.postAndSchedule(context)
            ir.hooshmandcalendar.app.prayer.AzanScheduler.reschedule(context)
        } catch (_: Exception) {
        }
    }
}
