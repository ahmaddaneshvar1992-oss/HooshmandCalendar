package ir.hooshmandcalendar.app

import android.app.Application
import ir.hooshmandcalendar.app.prayer.AzanScheduler
import ir.hooshmandcalendar.app.reminders.Notifications
import ir.hooshmandcalendar.app.reminders.ReminderScheduler
import ir.hooshmandcalendar.app.reminders.TodayNotification

/** کلاس اصلی برنامه: ساخت کانال اعلان، اعلان تاریخ امروز و زمان‌بندی یادآورها و اذان در شروع */
class HooshmandApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Notifications.ensureDefaultChannel(this)
        Notifications.ensureAzanChannel(this)
        // اطمینان از اینکه یادآورها پس از نصب مجدد یا آپدیت دوباره فعال می‌شوند
        Thread {
            try {
                ReminderScheduler.scheduleAll(this)
            } catch (_: Exception) {
            }
            try {
                TodayNotification.postAndSchedule(this)
            } catch (_: Exception) {
            }
            try {
                AzanScheduler.reschedule(this)
            } catch (_: Exception) {
            }
        }.start()
    }
}
