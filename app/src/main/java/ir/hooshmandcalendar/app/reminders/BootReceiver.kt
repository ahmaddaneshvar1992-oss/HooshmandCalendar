package ir.hooshmandcalendar.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * پس از روشن شدن گوشی، تغییر ساعت/منطقه زمانی یا به‌روزرسانی برنامه،
 * همه یادآورها، اذان‌ها و اعلان تاریخ امروز دوباره زمان‌بندی می‌شوند.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            try {
                ReminderScheduler.scheduleAll(context)
            } catch (_: Exception) {
            }
            try {
                TodayNotification.postAndSchedule(context)
            } catch (_: Exception) {
            }
            try {
                ir.hooshmandcalendar.app.prayer.AzanScheduler.reschedule(context)
            } catch (_: Exception) {
            }
        }
    }
}
