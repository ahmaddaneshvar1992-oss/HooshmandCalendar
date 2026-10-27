package ir.hooshmandcalendar.app.core

import java.time.LocalDate
import java.time.LocalTime

/** شماره روز ژولینی یک تاریخ میلادی (۱۹۷۰-۰۱-۰۱ = ۲۴۴۰۵۸۸) */
const val EPOCH_DAY_TO_JDN: Int = 2440588

fun epochDayToJdn(epochDay: Long): Int = (epochDay + EPOCH_DAY_TO_JDN).toInt()
fun jdnToEpochDay(jdn: Int): Long = (jdn - EPOCH_DAY_TO_JDN).toLong()

/** اندیس روز هفته ایرانی از شماره ژولینی: ۰=شنبه … ۶=جمعه */
fun weekdayIndexOfJdn(jdn: Int): Int = Math.floorMod(jdn + 2, 7)

object PersianDateNames {
    val jalaliMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val gregorianMonths = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )
    val hijriMonths = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )
    val weekdays = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )
    val weekdaysShort = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
}

/** تاریخ هجری شمسی */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {

    val jdn: Int get() = JalaaliCalendar.toJdn(year, month, day)
    val epochDay: Long get() = jdnToEpochDay(jdn)
    val weekdayIndex: Int get() = weekdayIndexOfJdn(jdn)
    val weekdayName: String get() = PersianDateNames.weekdays[weekdayIndex]
    val monthName: String get() = PersianDateNames.jalaliMonths[month - 1]
    val isToday: Boolean get() = this == today()

    /** روز از beginning of year (۱-بنیاد) */
    val dayOfYear: Int get() = jdn - JalaaliCalendar.toJdn(year, 1, 1) + 1

    /** شماره هفته سال (هفته با شنبه شروع می‌شود؛ هفته ۱ شامل روز اول سال است) */
    val weekOfYear: Int
        get() {
            val firstDay = JalaliDate(year, 1, 1)
            val offset = firstDay.weekdayIndex
            return (offset + dayOfYear - 1) / 7 + 1
        }

    fun plusDays(days: Int): JalaliDate = fromJdn(jdn + days)
    fun minusDays(days: Int): JalaliDate = fromJdn(jdn - days)

    fun plusMonths(months: Int): JalaliDate {
        val total = (year * 12 + month - 1) + months
        val y = Math.floorDiv(total, 12)
        val m = Math.floorMod(total, 12) + 1
        val d = day.coerceAtMost(JalaaliCalendar.lengthOfMonth(m, y))
        return JalaliDate(y, m, d)
    }

    fun toGregorian(): GregorianDate {
        val g = JalaaliCalendar.toGregorian(year, month, day)
        return GregorianDate(g[0], g[1], g[2])
    }

    fun toHijri(): HijriDate {
        val h = HijriCalendar.fromJdn(jdn)
        return HijriDate(h[0], h[1], h[2])
    }

    /** تاریخ کامل: شنبه ۲۲ فروردین ۱۴۰۴ */
    fun longFormat(): String = "$weekdayName ${day.toPersianDigits()} $monthName ${year.toPersianDigits()}"

    fun shortFormat(): String = "${day.toPersianDigits()} $monthName ${year.toPersianDigits()}"

    companion object {
        fun today(): JalaliDate {
            val now = LocalDate.now()
            val j = JalaaliCalendar.fromGregorian(now.year, now.monthValue, now.dayOfMonth)
            return JalaliDate(j[0], j[1], j[2])
        }

        fun fromJdn(jdn: Int): JalaliDate {
            val j = JalaaliCalendar.fromJdn(jdn)
            return JalaliDate(j[0], j[1], j[2])
        }

        fun fromEpochDay(epochDay: Long): JalaliDate = fromJdn(epochDayToJdn(epochDay))

        fun fromGregorian(gy: Int, gm: Int, gd: Int): JalaliDate {
            val j = JalaaliCalendar.fromGregorian(gy, gm, gd)
            return JalaliDate(j[0], j[1], j[2])
        }
    }
}

/** تاریخ میلادی */
data class GregorianDate(val year: Int, val month: Int, val day: Int) {

    val jdn: Int get() = JalaaliCalendar.gregorianToJdn(year, month, day)
    val monthName: String get() = PersianDateNames.gregorianMonths[month - 1]
    val weekdayIndex: Int get() = weekdayIndexOfJdn(jdn)
    val weekdayName: String get() = PersianDateNames.weekdays[weekdayIndex]

    fun toJalali(): JalaliDate = JalaliDate.fromJdn(jdn)
    fun toHijri(): HijriDate = HijriCalendar.fromJdn(jdn).let { HijriDate(it[0], it[1], it[2]) }

    /** 2025-03-21 */
    fun isoFormat(): String = "%04d-%02d-%02d".format(year, month, day)

    companion object {
        fun today(): GregorianDate {
            val now = LocalDate.now()
            return GregorianDate(now.year, now.monthValue, now.dayOfMonth)
        }
    }
}

/** تاریخ هجری قمری */
data class HijriDate(val year: Int, val month: Int, val day: Int) {

    val monthName: String get() = PersianDateNames.hijriMonths[month - 1]
    val jdn: Int get() = HijriCalendar.toJdn(year, month, day)

    fun toJalali(): JalaliDate = JalaliDate.fromJdn(jdn)

    /** ۱۷ ربیع‌الاول ۱۴۴۷ */
    fun shortFormat(): String = "${day.toPersianDigits()} $monthName ${year.toPersianDigits()}"

    companion object {
        fun fromJdn(jdn: Int): HijriDate {
            val h = HijriCalendar.fromJdn(jdn)
            return HijriDate(h[0], h[1], h[2])
        }
    }
}

/** زمان روز به شکل دقیقه از نیمه‌شب */
data class DayTime(val minutes: Int) {
    val hour: Int get() = minutes / 60
    val minute: Int get() = minutes % 60

    fun format(): String =
        "%02d:%02d".format(hour, minute).let { if (usePersianDigits) it.toPersianDigits() else it }

    companion object {
        var usePersianDigits: Boolean = true
        fun now(): DayTime {
            val t = LocalTime.now()
            return DayTime(t.hour * 60 + t.minute)
        }
    }
}
