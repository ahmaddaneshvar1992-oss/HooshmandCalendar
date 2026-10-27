package ir.hooshmandcalendar.app.core

/**
 * تبدیل تقویم هجری خورشیدی (جلالی) به میلادی و برعکس.
 * الگوریتم بورکوفسکی/جلالی (jalaali) — پیاده‌سازی مطابق کتابخانه استاندارد jalaali-js.
 * دقت برای سال‌های -۶۱ تا ۳۱۷۷ هجری شمسی تضمین شده است.
 */
object JalaaliCalendar {

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private class JalCalResult(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCalResult {
        val bl = breaks.size
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        var leap = 0

        require(jy >= jp && jy < breaks[bl - 1]) { "سال جلالی نامعتبر: $jy" }

        for (i in 1 until bl) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
            jp = jm
        }
        var n = jy - jp

        leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1

        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4

        return JalCalResult(leap, gy, march)
    }

    /** تقسیم صحیح با گرد شدن به سمت صفر (مطابق رفتار JavaScript/Java) */
    private fun div(a: Int, b: Int): Int = a / b
    private fun mod(a: Int, b: Int): Int = a % b

    /** میلادی به شماره روز ژولینی */
    fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Int {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
        d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
        return d
    }

    /** شماره روز ژولینی به میلادی */
    fun jdnToGregorian(jdn: Int): IntArray {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(mod(j, 1461), 4) * 5 + 308
        val gd = div(mod(i, 153), 5) + 1
        val gm = mod(div(i, 153), 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return intArrayOf(gy, gm, gd)
    }

    /** جلالی به شماره روز ژولینی */
    fun toJdn(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return gregorianToJdn(r.gy, 3, r.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    /** شماره روز ژولینی به جلالی */
    fun fromJdn(jdn: Int): IntArray {
        val gy = jdnToGregorian(jdn)[0]
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = gregorianToJdn(gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                return intArrayOf(jy, 1 + div(k, 31), mod(k, 31) + 1)
            }
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return intArrayOf(jy, 7 + div(k, 30), mod(k, 30) + 1)
    }

    fun toGregorian(jy: Int, jm: Int, jd: Int): IntArray = jdnToGregorian(toJdn(jy, jm, jd))
    fun fromGregorian(gy: Int, gm: Int, gd: Int): IntArray = fromJdn(gregorianToJdn(gy, gm, gd))

    /** کبیسه بودن سال شمسی */
    fun isLeapYear(jy: Int): Boolean = jalCal(jy).leap == 0

    /** تعداد روزهای ماه شمسی */
    fun lengthOfMonth(jm: Int, jy: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        isLeapYear(jy) -> 30
        else -> 29
    }
}
