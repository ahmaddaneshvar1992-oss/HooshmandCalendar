package ir.hooshmandcalendar.app.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import ir.hooshmandcalendar.app.MainActivity
import ir.hooshmandcalendar.app.R

/**
 * ساخت کانال‌ها و نمایش اعلان یادآور.
 * برای پشتیبانی از صدای دلخواه هر رویداد، برای هر صدا یک کانال ساخته می‌شود.
 */
object Notifications {

    const val CHANNEL_REMINDER = "reminders"
    private const val CHANNEL_REMINDER_PREFIX = "reminders_sound_"

    fun ensureDefaultChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_REMINDER,
            "یادآور رویدادها",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان یادآور قرارها، کارها و تولدها"
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
    }

    /** کانال مخصوص صدای انتخابی رویداد */
    private fun channelForSound(context: Context, soundUri: String?, vibrate: Boolean): NotificationChannel {
        val id = if (soundUri.isNullOrBlank()) CHANNEL_REMINDER
        else CHANNEL_REMINDER_PREFIX + soundUri.hashCode()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.getNotificationChannel(id)?.let { return it }

        val channel = NotificationChannel(
            id,
            if (soundUri.isNullOrBlank()) "یادآور رویدادها" else "یادآورها (صدای سفارشی)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان یادآور با صدای انتخابی"
            setSound(
                if (soundUri.isNullOrBlank()) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                else Uri.parse(soundUri),
                null
            )
            enableVibration(vibrate)
        }
        nm.createNotificationChannel(channel)
        return channel
    }

    fun showEventNotification(context: Context, eventId: Long, title: String, text: String, soundUri: String?, vibrate: Boolean) {
        ensureDefaultChannel(context)
        val channel = channelForSound(context, soundUri, vibrate)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, eventId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(if (vibrate) Notification.DEFAULT_VIBRATE else 0)

        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1_000_000 + (eventId % 100_000).toInt(), builder.build())
        } catch (_: SecurityException) {
        }
    }

    // ------------------------------------------------------------------
    // اذان: کانال با صدای داخلی اذان (کاملاً آفلاین)
    // ------------------------------------------------------------------
    const val CHANNEL_AZAN = "azan_prayer"

    fun ensureAzanChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_AZAN) != null) return
        val channel = NotificationChannel(
            CHANNEL_AZAN,
            "اذان و اوقات شرعی",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "پخش اذان در اوقات شرعی شهر انتخابی"
            setSound(
                Uri.parse("android.resource://${context.packageName}/raw/azan"),
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
    }

    fun showAzanNotification(context: Context, title: String, text: String) {
        ensureAzanChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 990, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_AZAN)
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(999_001, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
