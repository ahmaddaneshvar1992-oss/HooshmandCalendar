package ir.hooshmandcalendar.app.prayer

import ir.hooshmandcalendar.app.data.City
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * محاسبه کاملاً آفلاین اوقات شرعی بر اساس موقعیت خورشید.
 * روش ایران: مؤسسه ژئوفیزیک دانشگاه تهران (صبح ۱۷/۷ درجه، عشاء ۱۴ درجه، مغرب ۴/۵ درجه زیر افق)
 * روش MWL: سازمان جهانی اسلام (صبح ۱۸، عشاء ۱۷)
 */
object PrayerTimes {

    /** یک وقت شرعی برای نمایش و زمان‌بندی */
    enum class Kind(val label: String) {
        FAJR("اذان صبح"),
        SUNRISE("طلوع آفتاب"),
        DHUHR("اذان ظهر"),
        ASR("اذان عصر"),
        MAGHRIB("اذان مغرب"),
        ISHA("اذان عشاء");

        companion object {
            /** اذان‌های دارای زنگ (طلوع فقط اطلاع‌رسانی است) */
            val azanKinds = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
        }
    }

    enum class Method(val label: String, val fajrAngle: Double, val ishaAngle: Double, val maghribAngle: Double) {
        IRAN("ژئوفیزیک تهران", 17.7, 14.0, 4.5),
        MWL("سازمان جهانی اسلام", 18.0, 17.0, 0.833)
    }

    /** اوقات یک روز به شکل دقیقه از نیمه‌شب محلی */
    data class DayTimes(
        val fajr: Int,
        val sunrise: Int,
        val dhuhr: Int,
        val asr: Int,
        val maghrib: Int,
        val isha: Int
    ) {
        fun minutesOf(kind: Kind): Int = when (kind) {
            Kind.FAJR -> fajr
            Kind.SUNRISE -> sunrise
            Kind.DHUHR -> dhuhr
            Kind.ASR -> asr
            Kind.MAGHRIB -> maghrib
            Kind.ISHA -> isha
        }
    }

    // ---------------- ریاضیات نجومی ----------------

    private fun d2r(d: Double) = d * PI / 180.0
    private fun r2d(r: Double) = r * 180.0 / PI
    private fun fixAngle(a: Double): Double {
        var x = a % 360.0
        if (x < 0) x += 360.0
        return x
    }
    private fun fixHour(h: Double): Double {
        var x = h % 24.0
        if (x < 0) x += 24.0
        return x
    }

    /** تاریخ ژولینی (میلادی) */
    private fun julian(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private class SunPos(val declination: Double, val equation: Double)

    private fun sunPosition(jd: Double): SunPos {
        val d = jd - 2451545.0
        val g = d2r(fixAngle(357.529 + 0.98560028 * d))
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = d2r(fixAngle(q + 1.915 * sin(g) + 0.020 * sin(2 * g)))
        val e = d2r(23.439 - 0.00000036 * d)
        val ra = atan2(cos(e) * sin(l), cos(l)) / d2r(15.0) // ساعت
        val decl = r2d(asin(sin(e) * sin(l)))
        val eqt = q / 15.0 - fixHour(ra)
        return SunPos(decl, eqt)
    }

    private class Ctx(val lat: Double, val lon: Double, val tzHours: Double, val jDate: Double) {
        fun midDay(t: Double): Double {
            val eqt = sunPosition(jDate + t).equation
            return fixHour(12.0 - eqt)
        }

        fun sunAngleTime(angle: Double, t: Double, dir: Int): Double {
            val decl = sunPosition(jDate + t).declination
            val noon = midDay(t)
            val x = (-sin(d2r(angle)) - sin(d2r(decl)) * sin(d2r(lat))) /
                (cos(d2r(decl)) * cos(d2r(lat)))
            val clamped = x.coerceIn(-1.0, 1.0)
            val delta = r2d(acos(clamped)) / 15.0
            return noon + dir * delta
        }

        fun asrTime(shadowFactor: Double, t: Double): Double {
            val decl = sunPosition(jDate + t).declination
            val angle = -r2d(atan2(1.0, shadowFactor + tan(d2r(abs(lat - decl)))))
            return sunAngleTime(angle, t, +1)
        }
    }

    /** محاسبه اوقات برای یک تاریخ میلادی مشخص */
    fun compute(
        gYear: Int,
        gMonth: Int,
        gDay: Int,
        city: City,
        tzHours: Double,
        method: Method
    ): DayTimes {
        val jDate = julian(gYear, gMonth, gDay) - city.lon / (15.0 * 24.0)
        val ctx = Ctx(city.lat, city.lon, tzHours, jDate)

        // برآورد اولیه بخش‌هایی از روز
        var tFajr = 5.0 / 24.0
        var tSunrise = 6.0 / 24.0
        var tDhuhr = 12.0 / 24.0
        var tAsr = 13.0 / 24.0
        var tMaghrib = 18.0 / 24.0
        var tIsha = 18.0 / 24.0

        // دو دور تکرار برای همگرایی
        repeat(2) {
            tFajr = ctx.sunAngleTime(method.fajrAngle, tFajr, -1) / 24.0
            tSunrise = ctx.sunAngleTime(0.833, tSunrise, -1) / 24.0
            tDhuhr = ctx.midDay(tDhuhr) / 24.0
            tAsr = ctx.asrTime(1.0, tAsr) / 24.0
            tMaghrib = if (method.maghribAngle > 0.9) {
                ctx.sunAngleTime(method.maghribAngle, tMaghrib, +1) / 24.0
            } else {
                ctx.sunAngleTime(0.833, tMaghrib, +1) / 24.0
            }
            tIsha = ctx.sunAngleTime(method.ishaAngle, tIsha, +1) / 24.0
        }

        fun toLocalMinutes(portions: Double): Int {
            val hoursSolar = fixHour(portions * 24.0)
            val local = fixHour(hoursSolar + ctx.tzHours - city.lon / 15.0)
            return (Math.round(local * 60.0)).toInt()
        }

        return DayTimes(
            fajr = toLocalMinutes(tFajr),
            sunrise = toLocalMinutes(tSunrise),
            dhuhr = toLocalMinutes(tDhuhr),
            asr = toLocalMinutes(tAsr),
            maghrib = toLocalMinutes(tMaghrib),
            isha = toLocalMinutes(tIsha)
        )
    }

    /** اوقات امروز بر اساس تاریخ و منطقه زمانی دستگاه */
    fun computeToday(city: City, method: Method): DayTimes {
        val cal = Calendar.getInstance()
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val tzHours = TimeZone.getDefault().getOffset(cal.timeInMillis) / 3600000.0
        return compute(gYear, gMonth, gDay, city, tzHours, method)
    }

    /** اوقات یک روز خاص بر اساس epochDay (روز یونیکس) */
    fun computeForEpochDay(epochDay: Long, city: City, method: Method): DayTimes {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.timeInMillis = epochDay * 86400000L
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val tz = TimeZone.getDefault().getOffset(epochDay * 86400000L) / 3600000.0
        return compute(gYear, gMonth, gDay, city, tz, method)
    }
}
