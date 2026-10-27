package ir.hooshmandcalendar.app

import ir.hooshmandcalendar.app.core.HijriCalendar
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.JalaaliCalendar
import ir.hooshmandcalendar.app.core.weekdayIndexOfJdn
import ir.hooshmandcalendar.app.data.Occasions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست موتور تبدیل تاریخ (شمسی/میلادی/قمری) و مناسبت‌ها.
 * مقادیر مرجع با تقویم رسمی ایران بررسی شده‌اند.
 */
class CalendarEngineTest {

    @Test
    fun `nowruz 1404 is 21 March 2025`() {
        assertEquals(listOf(2025, 3, 21), JalaaliCalendar.toGregorian(1404, 1, 1).toList())
        assertEquals(listOf(1404, 1, 1), JalaaliCalendar.fromGregorian(2025, 3, 21).toList())
    }

    @Test
    fun `1403 is leap and has 30 esfand`() {
        assertTrue(JalaaliCalendar.isLeapYear(1403))
        assertEquals(listOf(2025, 3, 20), JalaaliCalendar.toGregorian(1403, 12, 30).toList())
        assertFalse(JalaaliCalendar.isLeapYear(1404))
    }

    @Test
    fun `weekday of nowruz 1404 is friday`() {
        val d = JalaliDate(1404, 1, 1)
        assertEquals(6, d.weekdayIndex) // جمعه
        assertEquals(0, weekdayIndexOfJdn(JalaliDate(1405, 1, 1).jdn)) // شنبه
    }

    @Test
    fun `jalali roundtrip is stable for 20 years`() {
        var jdn = JalaaliCalendar.toJdn(1390, 1, 1)
        repeat(20 * 366) {
            val j = JalaaliCalendar.fromJdn(jdn)
            assertEquals(jdn, JalaaliCalendar.toJdn(j[0], j[1], j[2]))
            jdn++
        }
    }

    @Test
    fun `eid fitr 1404 matches official calendar`() {
        // ۱۱ فروردین ۱۴۰۴ = ۱ شوال ۱۴۴۶ (رسمی ایران)
        assertEquals(listOf(1446, 10, 1), HijriCalendar.fromJdn(JalaliDate(1404, 1, 11).jdn).toList())
    }

    @Test
    fun `ashura 1404 matches official calendar`() {
        // ۱۵ تیر ۱۴۰۴ = ۱۰ محرم ۱۴۴۷
        assertEquals(listOf(1447, 1, 10), HijriCalendar.fromJdn(JalaliDate(1404, 4, 15).jdn).toList())
    }

    @Test
    fun `hijri to jalali roundtrip`() {
        val jalali = HijriCalendar.toJalali(1447, 1, 10)
        assertEquals(listOf(1404, 4, 15), jalali.toList())
    }

    @Test
    fun `fixed national holidays detected`() {
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 1, 1)))
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 1, 12)))
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 11, 22)))
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 12, 29)))
        assertFalse(Occasions.isOfficialHoliday(JalaliDate(1404, 7, 20)))
    }

    @Test
    fun `official religious holidays 1404 detected`() {
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 1, 11))) // عید فطر
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 4, 15))) // عاشورا
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1404, 9, 3)))  // شهادت فاطمه
        // روز عادی
        assertFalse(Occasions.isOfficialHoliday(JalaliDate(1404, 5, 15)))
    }

    @Test
    fun `official religious holidays 1405 detected`() {
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1405, 1, 1)))  // نوروز + عید فطر
        assertTrue(Occasions.isOfficialHoliday(JalaliDate(1405, 12, 19))) // عید فطر ۱۴۴۸
    }

    @Test
    fun `month lengths are correct`() {
        assertEquals(31, JalaaliCalendar.lengthOfMonth(1, 1404))
        assertEquals(30, JalaaliCalendar.lengthOfMonth(7, 1404))
        assertEquals(29, JalaaliCalendar.lengthOfMonth(12, 1404))
        assertEquals(30, JalaaliCalendar.lengthOfMonth(12, 1403))
    }
}
