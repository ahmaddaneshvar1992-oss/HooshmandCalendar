package ir.hooshmandcalendar.app.data

import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.JalaaliCalendar
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.data.db.RepeatType

/**
 * موتور تکرار: تعیین روزهای رخداد یک رویداد در یک بازه زمانی.
 * تکرار ماهانه/سالانه بر اساس تقویم شمسی است (تولدها سالانه شمسی تکرار می‌شوند).
 */
object OccurrenceEngine {

    /** آیا رویداد در روز مشخص رخ می‌دهد؟ */
    fun occursOn(event: EventEntity, date: JalaliDate): Boolean {
        val start = JalaliDate(event.jYear, event.jMonth, event.jDay)
        return occurrencesInRange(event, date, date).isNotEmpty()
    }

    /** همه روزهای رخداد رویداد در بازه [from, to] (شمسی، به‌صورت epoch day) */
    fun occurrencesInRange(event: EventEntity, from: JalaliDate, to: JalaliDate): List<Long> {
        val start = JalaliDate(event.jYear, event.jMonth, event.jDay)
        if (from.epochDay > to.epochDay) return emptyList()

        val out = mutableListOf<Long>()
        when (event.repeatType) {
            RepeatType.NONE -> {
                if (start.epochDay in from.epochDay..to.epochDay) out.add(start.epochDay)
            }
            RepeatType.DAILY -> {
                val first = maxOf(start.epochDay, from.epochDay)
                var d = first
                while (d <= to.epochDay) {
                    out.add(d); d += 1
                }
            }
            RepeatType.WEEKLY -> {
                // هم‌ترازی با روز هفته رویداد
                val weekdayOffset = Math.floorMod(start.epochDay, 7)
                var d = from.epochDay
                val rem = Math.floorMod(d, 7)
                var delta = weekdayOffset - rem
                if (delta < 0) delta += 7
                d += delta
                if (d < start.epochDay) d += 7
                while (d <= to.epochDay) {
                    out.add(d); d += 7
                }
            }
            RepeatType.MONTHLY -> {
                var y = start.year
                var m = start.month
                // پرش به بازه
                while (JalaliDate(y, m, 1).epochDay < from.epochDay - 62) {
                    val nm = JalaliDate(y, m, 1).plusMonths(1)
                    y = nm.year; m = nm.month
                }
                while (JalaliDate(y, m, 1).epochDay <= to.epochDay) {
                    val len = JalaaliCalendar.lengthOfMonth(m, y)
                    val day = event.jDay.coerceAtMost(len)
                    val ed = JalaliDate(y, m, day)
                    if (ed.epochDay >= start.epochDay && ed.epochDay in from.epochDay..to.epochDay) {
                        out.add(ed.epochDay)
                    }
                    val nm = JalaliDate(y, m, 1).plusMonths(1)
                    y = nm.year; m = nm.month
                    if (y > to.year + 1) break
                }
            }
            RepeatType.YEARLY -> {
                var y = start.year
                while (JalaaliCalendar.toJdn(y, event.jMonth.coerceAtMost(12), 1) <= to.jdn) {
                    val len = JalaaliCalendar.lengthOfMonth(event.jMonth, y)
                    val day = event.jDay.coerceAtMost(len)
                    val ed = JalaliDate(y, event.jMonth, day)
                    if (ed.epochDay >= start.epochDay && ed.epochDay in from.epochDay..to.epochDay) {
                        out.add(ed.epochDay)
                    }
                    y += 1
                    if (y > to.year + 1) break
                }
            }
        }
        return out
    }

    /**
     * اولین زمان رخداد آینده رویداد (برای زمان‌بندی یادآور) به شکل epoch millis.
     * اگر رویداد تکرار ندارد و گذشته است، null برمی‌گردد.
     * @param reminderOffsetMinutes دقایق قبل از شروع رویداد
     */
    fun nextTriggerAt(event: EventEntity, reminderOffsetMinutes: Int, nowMillis: Long): Long? {
        val today = JalaliDate.today()
        val horizon = today.plusDays(400)
        val occurrences = occurrencesInRange(event, today.plusDays(-1), horizon)
        for (epochDay in occurrences) {
            // رویداد تمام‌روز یادآورش ساعت ۹ صبح است
            val startMin = if (event.allDay) 9 * 60 else event.startMinutes
            val triggerMillis = (epochDay * 24L * 60L + startMin - reminderOffsetMinutes) * 60L * 1000L
            if (triggerMillis > nowMillis) return triggerMillis
        }
        return null
    }
}
