package ir.hooshmandcalendar.app.core

/**
 * تبدیل تقویم هجری قمری (تبویری - الگوریتم کویتی).
 * دقت این الگوریتم نسبت به تقویم رسمی ایران (رؤیت هلال) ممکن است ±۱ روز باشد؛
 * به همین دلیل برای سال‌های دارای داده رسمی (۱۴۰۴ و ۱۴۰۵) از جدول رسمی استفاده می‌شود
 * (کلاس Occasions).
 */
object HijriCalendar {

    /** شماره روز ژولینی به قمری: [سال، ماه، روز] */
    fun fromJdn(jdn: Int): IntArray {
        val l0 = jdn - 1948440 + 10632
        val n = (l0 - 1) / 10631
        var l = l0 - 10631 * n + 354
        val j = (10985 - l) / 5316 * (50 * l / 17719) + (l / 5670) * (43 * l / 15238)
        l = l - (30 - j) / 15 * (17719 * j / 50) - (j / 16) * (15238 * j / 43) + 29
        val m = 24 * l / 709
        val d = l - 709 * m / 24
        val y = 30 * n + j - 30
        return intArrayOf(y, m, d)
    }

    /** قمری به شماره روز ژولینی */
    fun toJdn(hy: Int, hm: Int, hd: Int): Int =
        (11 * hy + 3) / 30 + 354 * hy + 30 * hm - (hm - 1) / 2 + hd + 1948440 - 385

    fun toJalali(hy: Int, hm: Int, hd: Int): IntArray = JalaaliCalendar.fromJdn(toJdn(hy, hm, hd))
    fun fromJalali(jy: Int, jm: Int, jd: Int): IntArray = fromJdn(JalaaliCalendar.toJdn(jy, jm, jd))

    /** طول ماه قمری در سال مشخص */
    fun lengthOfMonth(hy: Int, hm: Int): Int {
        val start = toJdn(hy, hm, 1)
        val next = if (hm < 12) toJdn(hy, hm + 1, 1) else toJdn(hy + 1, 1, 1)
        return next - start
    }

    /**
     * روز قمری امن: اگر مناسبت روی روز ۳۰ ماه ۲۹ روزه بیفتد، روز ۲۹ برگردانده می‌شود.
     */
    fun safeDay(hy: Int, hm: Int, hd: Int): Int =
        hd.coerceAtMost(lengthOfMonth(hy, hm))
}
