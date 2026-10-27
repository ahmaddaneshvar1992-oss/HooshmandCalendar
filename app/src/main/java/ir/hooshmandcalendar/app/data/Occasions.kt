package ir.hooshmandcalendar.app.data

import ir.hooshmandcalendar.app.core.HijriCalendar
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.toPersianDigits

/** انواع مناسبت برای نمایش چیپ رنگی (مطابق طراحی) */
enum class OccasionKind { NATIONAL, RELIGIOUS, INTERNATIONAL }

/** یک مناسبت یا تعطیلی */
data class Occasion(
    val title: String,
    val holiday: Boolean,
    val kind: OccasionKind
)

/**
 * پایگاه داده مناسبت‌ها و تعطیلات رسمی ایران.
 *
 * - تعطیلات ملی ثابت: هر سال در همان روز شمسی.
 * - مناسبت‌های مذهبی: وابسته به تقویم قمری (الگوریتمی، ممکن است ±۱ روز خطا داشته باشد).
 * - جدول رسمی ۱۴۰۴ و ۱۴۰۵: تاریخ‌های دقیق مصوب شورای فرهنگ عمومی؛
 *   برای این دو سال، مناسبت‌های مذهبی از جدول رسمی خوانده می‌شود (بدون خطا).
 */
object Occasions {

    // کلید: ماه*100+روز
    private fun k(m: Int, d: Int) = m * 100 + d

    // ------------------------------------------------------------------
    // تعطیلات رسمی ملی ثابت (هر سال)
    // ------------------------------------------------------------------
    val nationalHolidays: Set<Int> = setOf(
        k(1, 1), k(1, 2), k(1, 3), k(1, 4),
        k(1, 12), k(1, 13),
        k(3, 14), k(3, 15),
        k(11, 22), k(12, 29)
    )

    // ------------------------------------------------------------------
    // مناسبت‌های ثابت ملی و بین‌المللی (هر سال - غیرتعطیل)
    // ------------------------------------------------------------------
    val fixedOccasions: Map<Int, List<Occasion>> = mapOf(
        k(1, 6) to listOf(Occasion("زادروز زرتشت پیامبر", false, OccasionKind.RELIGIOUS)),
        k(1, 20) to listOf(Occasion("روز ملی فناوری هسته‌ای", false, OccasionKind.NATIONAL), Occasion("شهادت سید مرتضی آوینی", false, OccasionKind.NATIONAL)),
        k(1, 25) to listOf(Occasion("بزرگداشت عطار نیشابوری", false, OccasionKind.NATIONAL)),
        k(1, 29) to listOf(Occasion("روز ارتش جمهوری اسلامی ایران", false, OccasionKind.NATIONAL)),
        k(2, 1) to listOf(Occasion("بزرگداشت سعدی - روز نثر فارسی", false, OccasionKind.NATIONAL)),
        k(2, 3) to listOf(Occasion("بزرگداشت شیخ بهایی - روز معماری", false, OccasionKind.NATIONAL)),
        k(2, 5) to listOf(Occasion("شکست حمله نظامی آمریکا به ایران در طبس", false, OccasionKind.NATIONAL)),
        k(2, 9) to listOf(Occasion("روز شوراها و روز روانشناس", false, OccasionKind.NATIONAL)),
        k(2, 10) to listOf(Occasion("روز ملی خلیج فارس", false, OccasionKind.NATIONAL)),
        k(2, 12) to listOf(Occasion("شهادت استاد مطهری - روز معلم", false, OccasionKind.NATIONAL)),
        k(2, 19) to listOf(Occasion("ولادت امام رضا (ع)", false, OccasionKind.RELIGIOUS)),
        k(2, 25) to listOf(Occasion("بزرگداشت فردوسی - روز پاسداشت زبان فارسی", false, OccasionKind.NATIONAL)),
        k(2, 28) to listOf(Occasion("بزرگداشت عمر خیام", false, OccasionKind.NATIONAL)),
        k(3, 1) to listOf(Occasion("روز بهره‌وری و بزرگداشت ملاصدرا", false, OccasionKind.NATIONAL)),
        k(3, 3) to listOf(Occasion("فتح خرمشهر - روز مقاومت، ایثار و پیروزی", false, OccasionKind.NATIONAL)),
        k(3, 27) to listOf(Occasion("روز جهاد کشاورزی", false, OccasionKind.NATIONAL)),
        k(3, 31) to listOf(Occasion("شهادت دکتر چمران - روز خانواده", false, OccasionKind.NATIONAL)),
        k(4, 7) to listOf(Occasion("شهادت دکتر بهشتی - روز قوه قضاییه", false, OccasionKind.NATIONAL)),
        k(4, 10) to listOf(Occasion("روز صنعت و معدن", false, OccasionKind.NATIONAL)),
        k(4, 27) to listOf(Occasion("اعلام پذیرش قطعنامه ۵۹۸", false, OccasionKind.NATIONAL)),
        k(5, 5) to listOf(Occasion("سالروز عملیات مرصاد", false, OccasionKind.NATIONAL)),
        k(5, 17) to listOf(Occasion("روز خبرنگار", false, OccasionKind.NATIONAL)),
        k(6, 1) to listOf(Occasion("روز پزشک - بزرگداشت ابوعلی سینا", false, OccasionKind.NATIONAL)),
        k(6, 5) to listOf(Occasion("روز داروسازی - بزرگداشت زکریای رازی", false, OccasionKind.NATIONAL)),
        k(6, 8) to listOf(Occasion("شهادت رجایی و باهنر - روز مبارزه با تروریسم", false, OccasionKind.NATIONAL)),
        k(6, 13) to listOf(Occasion("روز تعاون - بزرگداشت ابوریحان بیرونی", false, OccasionKind.NATIONAL)),
        k(6, 17) to listOf(Occasion("قیام ۱۷ شهریور", false, OccasionKind.NATIONAL)),
        k(6, 27) to listOf(Occasion("روز شعر و ادب فارسی - بزرگداشت استاد شهریار", false, OccasionKind.NATIONAL)),
        k(6, 31) to listOf(Occasion("آغاز جنگ تحمیلی - هفته دفاع مقدس", false, OccasionKind.NATIONAL)),
        k(7, 7) to listOf(Occasion("شهادت سرداران فلاحی، فکوری، نامجو و کلاهدوز", false, OccasionKind.NATIONAL)),
        k(7, 8) to listOf(Occasion("بزرگداشت مولوی", false, OccasionKind.NATIONAL)),
        k(7, 13) to listOf(Occasion("روز نیروی انتظامی", false, OccasionKind.NATIONAL)),
        k(7, 20) to listOf(Occasion("بزرگداشت حافظ", false, OccasionKind.NATIONAL)),
        k(7, 24) to listOf(Occasion("روز پیوند اولیا و مربیان", false, OccasionKind.NATIONAL)),
        k(8, 1) to listOf(Occasion("روز آمار و برنامه‌ریزی - بزرگداشت ابوالفضل بیهقی", false, OccasionKind.NATIONAL)),
        k(8, 8) to listOf(Occasion("شهادت محمدحسین فهمیده - روز نوجوان", false, OccasionKind.NATIONAL)),
        k(8, 13) to listOf(Occasion("روز ملی مبارزه با استکبار - روز دانش‌آموز", false, OccasionKind.NATIONAL)),
        k(8, 24) to listOf(Occasion("روز کتاب، کتاب‌خوانی و کتابدار", false, OccasionKind.NATIONAL)),
        k(9, 5) to listOf(Occasion("روز بسیج مستضعفان", false, OccasionKind.NATIONAL)),
        k(9, 7) to listOf(Occasion("روز نیروی دریایی", false, OccasionKind.NATIONAL)),
        k(9, 10) to listOf(Occasion("شهادت دکتر مدرس - روز مجلس", false, OccasionKind.NATIONAL)),
        k(9, 16) to listOf(Occasion("روز دانشجو", false, OccasionKind.NATIONAL)),
        k(9, 25) to listOf(Occasion("روز پژوهش", false, OccasionKind.NATIONAL)),
        k(9, 30) to listOf(Occasion("شب یلدا - چله", false, OccasionKind.NATIONAL)),
        k(10, 4) to listOf(Occasion("ولادت حضرت عیسی مسیح (ع)", false, OccasionKind.RELIGIOUS)),
        k(10, 13) to listOf(Occasion("شهادت سردار سلیمانی - روز جهانی مقاومت", false, OccasionKind.NATIONAL)),
        k(10, 20) to listOf(Occasion("شهادت میرزاتقی‌خان امیرکبیر", false, OccasionKind.NATIONAL)),
        k(11, 12) to listOf(Occasion("بازگشت امام خمینی - آغاز دهه فجر", false, OccasionKind.NATIONAL)),
        k(11, 19) to listOf(Occasion("روز نیروی هوایی", false, OccasionKind.NATIONAL)),
        k(12, 5) to listOf(Occasion("بزرگداشت خواجه نصیرالدین طوسی - روز مهندسی", false, OccasionKind.NATIONAL)),
        k(12, 15) to listOf(Occasion("روز درختکاری", false, OccasionKind.NATIONAL)),
        k(12, 22) to listOf(Occasion("روز بزرگداشت شهدا", false, OccasionKind.NATIONAL)),
        k(12, 25) to listOf(Occasion("بزرگداشت پروین اعتصامی", false, OccasionKind.NATIONAL))
    )

    // ------------------------------------------------------------------
    // مناسبت‌های مذهبی بر اساس تاریخ قمری (الگوریتمی برای سال‌های بدون جدول رسمی)
    // key = ماه قمری*100+روز قمری
    // ------------------------------------------------------------------
    val religiousByHijri: Map<Int, List<Occasion>> = mapOf(
        k(1, 1) to listOf(Occasion("آغاز سال نو هجری قمری", false, OccasionKind.RELIGIOUS)),
        k(1, 9) to listOf(Occasion("تاسوعای حسینی", true, OccasionKind.RELIGIOUS)),
        k(1, 10) to listOf(Occasion("عاشورای حسینی", true, OccasionKind.RELIGIOUS)),
        k(2, 15) to listOf(Occasion("وفات حضرت زینب (س)", false, OccasionKind.RELIGIOUS)),
        k(2, 20) to listOf(Occasion("اربعین حسینی", true, OccasionKind.RELIGIOUS)),
        k(2, 28) to listOf(Occasion("رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)", true, OccasionKind.RELIGIOUS)),
        k(2, 30) to listOf(Occasion("شهادت امام رضا (ع)", true, OccasionKind.RELIGIOUS)),
        k(3, 8) to listOf(Occasion("شهادت امام حسن عسکری (ع)", true, OccasionKind.RELIGIOUS)),
        k(3, 9) to listOf(Occasion("ولادت امام حسن عسکری (ع)", false, OccasionKind.RELIGIOUS)),
        k(3, 12) to listOf(Occasion("ولادت پیامبر (ص) به روایت اهل سنت - آغاز هفته وحدت", false, OccasionKind.RELIGIOUS)),
        k(3, 17) to listOf(Occasion("میلاد پیامبر اکرم (ص) و امام جعفر صادق (ع)", true, OccasionKind.RELIGIOUS)),
        k(5, 5) to listOf(Occasion("ولادت حضرت زینب (س) - روز پرستار", false, OccasionKind.RELIGIOUS)),
        k(6, 3) to listOf(Occasion("شهادت حضرت فاطمه زهرا (س)", true, OccasionKind.RELIGIOUS)),
        k(6, 20) to listOf(Occasion("ولادت حضرت فاطمه زهرا (س) - روز زن و مادر", false, OccasionKind.RELIGIOUS)),
        k(7, 1) to listOf(Occasion("ولادت امام محمد باقر (ع)", false, OccasionKind.RELIGIOUS)),
        k(7, 3) to listOf(Occasion("شهادت امام علی‌النقی الهادی (ع)", false, OccasionKind.RELIGIOUS)),
        k(7, 13) to listOf(Occasion("ولادت امام علی (ع) - روز پدر", true, OccasionKind.RELIGIOUS)),
        k(7, 25) to listOf(Occasion("شهادت امام موسی کاظم (ع)", false, OccasionKind.RELIGIOUS)),
        k(7, 27) to listOf(Occasion("مبعث رسول اکرم (ص)", true, OccasionKind.RELIGIOUS)),
        k(8, 3) to listOf(Occasion("ولادت امام حسین (ع)", false, OccasionKind.RELIGIOUS)),
        k(8, 4) to listOf(Occasion("ولادت حضرت ابوالفضل العباس (ع)", false, OccasionKind.RELIGIOUS)),
        k(8, 5) to listOf(Occasion("ولادت امام زین‌العابدین (ع)", false, OccasionKind.RELIGIOUS)),
        k(8, 11) to listOf(Occasion("ولادت حضرت علی‌اکبر (ع) - روز جوان", false, OccasionKind.RELIGIOUS)),
        k(8, 15) to listOf(Occasion("ولادت امام زمان (عج)", true, OccasionKind.RELIGIOUS)),
        k(9, 10) to listOf(Occasion("وفات حضرت خدیجه (س)", false, OccasionKind.RELIGIOUS)),
        k(9, 15) to listOf(Occasion("ولادت امام حسن مجتبی (ع) - روز اکرام", false, OccasionKind.RELIGIOUS)),
        k(9, 19) to listOf(Occasion("ضربت خوردن امام علی (ع) - شب قدر", false, OccasionKind.RELIGIOUS)),
        k(9, 21) to listOf(Occasion("شهادت امام علی (ع)", true, OccasionKind.RELIGIOUS)),
        k(9, 23) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(10, 1) to listOf(Occasion("عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(10, 2) to listOf(Occasion("تعطیل به مناسبت عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(10, 25) to listOf(Occasion("شهادت امام جعفر صادق (ع)", true, OccasionKind.RELIGIOUS)),
        k(11, 1) to listOf(Occasion("ولادت حضرت معصومه (س) - روز دختران", false, OccasionKind.RELIGIOUS)),
        k(11, 11) to listOf(Occasion("وفات حضرت معصومه (س)", false, OccasionKind.RELIGIOUS)),
        k(11, 29) to listOf(Occasion("شهادت امام محمد تقی (ع)", false, OccasionKind.RELIGIOUS)),
        k(12, 7) to listOf(Occasion("شهادت امام محمد باقر (ع)", false, OccasionKind.RELIGIOUS)),
        k(12, 10) to listOf(Occasion("عید سعید قربان", true, OccasionKind.RELIGIOUS)),
        k(12, 15) to listOf(Occasion("ولادت امام علی‌النقی الهادی (ع)", false, OccasionKind.RELIGIOUS)),
        k(12, 18) to listOf(Occasion("عید سعید غدیر خم", true, OccasionKind.RELIGIOUS)),
        k(12, 20) to listOf(Occasion("ولادت امام موسی کاظم (ع)", false, OccasionKind.RELIGIOUS))
    )

    // ------------------------------------------------------------------
    // جدول رسمی ۱۴۰۴ (مناسبت‌های وابسته به قمری با تاریخ دقیق رسمی)
    // ------------------------------------------------------------------
    private val official1404: Map<Int, List<Occasion>> = mapOf(
        k(1, 1) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(1, 3) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(1, 8) to listOf(Occasion("روز جهانی قدس", false, OccasionKind.RELIGIOUS)),
        k(1, 11) to listOf(Occasion("عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(1, 12) to listOf(Occasion("تعطیل به مناسبت عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(1, 30) to listOf(Occasion("ولادت حضرت معصومه (س) - روز دختران", false, OccasionKind.RELIGIOUS)),
        k(2, 4) to listOf(Occasion("شهادت امام جعفر صادق (ع)", true, OccasionKind.RELIGIOUS)),
        k(3, 16) to listOf(Occasion("عید سعید قربان", true, OccasionKind.RELIGIOUS)),
        k(3, 24) to listOf(Occasion("عید سعید غدیر خم", true, OccasionKind.RELIGIOUS)),
        k(4, 6) to listOf(Occasion("آغاز سال ۱۴۴۷ هجری قمری", false, OccasionKind.RELIGIOUS)),
        k(4, 14) to listOf(Occasion("تاسوعای حسینی", true, OccasionKind.RELIGIOUS)),
        k(4, 15) to listOf(Occasion("عاشورای حسینی", true, OccasionKind.RELIGIOUS)),
        k(5, 10) to listOf(Occasion("شهادت امام حسن مجتبی (ع) به روایتی - بزرگداشت سلمان فارسی", false, OccasionKind.RELIGIOUS)),
        k(5, 23) to listOf(Occasion("اربعین حسینی", true, OccasionKind.RELIGIOUS)),
        k(5, 31) to listOf(Occasion("رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)", true, OccasionKind.RELIGIOUS)),
        k(6, 2) to listOf(Occasion("شهادت امام رضا (ع)", true, OccasionKind.RELIGIOUS)),
        k(6, 10) to listOf(Occasion("شهادت امام حسن عسکری (ع) - آغاز امامت امام زمان (عج)", true, OccasionKind.RELIGIOUS)),
        k(6, 14) to listOf(Occasion("ولادت پیامبر (ص) به روایت اهل سنت - آغاز هفته وحدت", false, OccasionKind.RELIGIOUS)),
        k(6, 19) to listOf(Occasion("ولادت پیامبر اکرم (ص) و امام جعفر صادق (ع)", true, OccasionKind.RELIGIOUS)),
        k(7, 9) to listOf(Occasion("ولادت امام حسن عسکری (ع)", false, OccasionKind.RELIGIOUS)),
        k(8, 5) to listOf(Occasion("ولادت حضرت زینب (س) - روز پرستار", false, OccasionKind.RELIGIOUS)),
        k(9, 3) to listOf(Occasion("شهادت حضرت فاطمه زهرا (س)", true, OccasionKind.RELIGIOUS)),
        k(9, 20) to listOf(Occasion("ولادت حضرت فاطمه زهرا (س) - روز زن و مادر", false, OccasionKind.RELIGIOUS)),
        k(10, 1) to listOf(Occasion("ولادت امام محمد باقر (ع)", false, OccasionKind.RELIGIOUS)),
        k(10, 3) to listOf(Occasion("شهادت امام علی‌النقی الهادی (ع)", false, OccasionKind.RELIGIOUS)),
        k(10, 10) to listOf(Occasion("ولادت امام محمد تقی (ع)", false, OccasionKind.RELIGIOUS)),
        k(10, 13) to listOf(Occasion("ولادت امام علی (ع) - روز پدر", true, OccasionKind.RELIGIOUS)),
        k(10, 15) to listOf(Occasion("وفات حضرت زینب (س)", false, OccasionKind.RELIGIOUS)),
        k(10, 25) to listOf(Occasion("شهادت امام موسی کاظم (ع)", false, OccasionKind.RELIGIOUS)),
        k(10, 27) to listOf(Occasion("مبعث رسول اکرم (ص)", true, OccasionKind.RELIGIOUS)),
        k(11, 3) to listOf(Occasion("ولادت امام حسین (ع)", false, OccasionKind.RELIGIOUS)),
        k(11, 4) to listOf(Occasion("ولادت حضرت ابوالفضل العباس (ع) - روز جانباز", false, OccasionKind.RELIGIOUS)),
        k(11, 5) to listOf(Occasion("ولادت امام زین‌العابدین (ع)", false, OccasionKind.RELIGIOUS)),
        k(11, 11) to listOf(Occasion("ولادت حضرت علی‌اکبر (ع) - روز جوان", false, OccasionKind.RELIGIOUS)),
        k(11, 15) to listOf(Occasion("ولادت امام زمان (عج)", true, OccasionKind.RELIGIOUS)),
        k(12, 17) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(12, 19) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(12, 20) to listOf(Occasion("شهادت امام علی (ع)", true, OccasionKind.RELIGIOUS)),
        k(12, 21) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(12, 29) to listOf(Occasion("روز جهانی قدس", false, OccasionKind.RELIGIOUS))
    )

    // ------------------------------------------------------------------
    // جدول رسمی ۱۴۰۵
    // ------------------------------------------------------------------
    private val official1405: Map<Int, List<Occasion>> = mapOf(
        k(1, 1) to listOf(Occasion("عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(1, 2) to listOf(Occasion("تعطیل به مناسبت عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(1, 25) to listOf(Occasion("شهادت امام جعفر صادق (ع)", true, OccasionKind.RELIGIOUS)),
        k(3, 5) to listOf(Occasion("روز عرفه - روز نیایش", false, OccasionKind.RELIGIOUS)),
        k(3, 6) to listOf(Occasion("عید سعید قربان", true, OccasionKind.RELIGIOUS)),
        k(3, 14) to listOf(Occasion("عید سعید غدیر خم", true, OccasionKind.RELIGIOUS)),
        k(3, 26) to listOf(Occasion("آغاز سال ۱۴۴۸ هجری قمری", false, OccasionKind.RELIGIOUS)),
        k(4, 3) to listOf(Occasion("تاسوعای حسینی", true, OccasionKind.RELIGIOUS)),
        k(4, 4) to listOf(Occasion("عاشورای حسینی", true, OccasionKind.RELIGIOUS)),
        k(5, 13) to listOf(Occasion("اربعین حسینی", true, OccasionKind.RELIGIOUS)),
        k(5, 21) to listOf(Occasion("رحلت پیامبر اکرم (ص) و شهادت امام حسن مجتبی (ع)", true, OccasionKind.RELIGIOUS)),
        k(5, 22) to listOf(Occasion("شهادت امام رضا (ع)", true, OccasionKind.RELIGIOUS)),
        k(5, 30) to listOf(Occasion("شهادت امام حسن عسکری (ع) - آغاز امامت امام زمان (عج)", true, OccasionKind.RELIGIOUS)),
        k(6, 3) to listOf(Occasion("ولادت پیامبر (ص) به روایت اهل سنت - آغاز هفته وحدت", false, OccasionKind.RELIGIOUS)),
        k(6, 8) to listOf(Occasion("ولادت پیامبر اکرم (ص) و امام جعفر صادق (ع)", true, OccasionKind.RELIGIOUS)),
        k(7, 24) to listOf(Occasion("ولادت حضرت زینب (س) - روز پرستار", false, OccasionKind.RELIGIOUS)),
        k(8, 2) to listOf(Occasion("شهادت حضرت فاطمه زهرا (س) به روایتی", false, OccasionKind.RELIGIOUS)),
        k(8, 22) to listOf(Occasion("شهادت حضرت فاطمه زهرا (س)", true, OccasionKind.RELIGIOUS)),
        k(9, 6) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(9, 7) to listOf(Occasion("ضربت خوردن امام علی (ع) - روز نهج‌البلاغه", false, OccasionKind.RELIGIOUS)),
        k(9, 8) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(9, 9) to listOf(Occasion("شهادت امام علی (ع)", true, OccasionKind.RELIGIOUS)),
        k(9, 10) to listOf(Occasion("شب قدر", false, OccasionKind.RELIGIOUS)),
        k(9, 14) to listOf(Occasion("روز جهانی قدس", false, OccasionKind.RELIGIOUS)),
        k(9, 19) to listOf(Occasion("عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(9, 20) to listOf(Occasion("تعطیل به مناسبت عید سعید فطر", true, OccasionKind.RELIGIOUS)),
        k(10, 2) to listOf(Occasion("ولادت امام علی (ع) - روز پدر", true, OccasionKind.RELIGIOUS)),
        k(10, 16) to listOf(Occasion("مبعث رسول اکرم (ص)", true, OccasionKind.RELIGIOUS)),
        k(10, 22) to listOf(Occasion("ولادت امام حسین (ع) - روز پاسدار", false, OccasionKind.RELIGIOUS)),
        k(10, 23) to listOf(Occasion("ولادت حضرت ابوالفضل العباس (ع) - روز جانباز", false, OccasionKind.RELIGIOUS)),
        k(10, 24) to listOf(Occasion("ولادت امام زین‌العابدین (ع)", false, OccasionKind.RELIGIOUS)),
        k(10, 30) to listOf(Occasion("ولادت حضرت علی‌اکبر (ع) - روز جوان", false, OccasionKind.RELIGIOUS)),
        k(11, 4) to listOf(Occasion("ولادت امام زمان (عج)", true, OccasionKind.RELIGIOUS)),
        k(11, 9) to listOf(Occasion("ولادت حضرت فاطمه زهرا (س) - روز زن و مادر", false, OccasionKind.RELIGIOUS)),
        k(12, 3) to listOf(Occasion("ولادت امام حسن مجتبی (ع) - روز اکرام", false, OccasionKind.RELIGIOUS))
    )

    private val officialOverrides: Map<Int, Map<Int, List<Occasion>>> = mapOf(
        1404 to official1404,
        1405 to official1405
    )

    /** سال‌های دارای جدول رسمی */
    fun hasOfficialData(jy: Int): Boolean = officialOverrides.containsKey(jy)

    // ------------------------------------------------------------------
    // API
    // ------------------------------------------------------------------

    /** همه مناسبتهای یک روز مشخص (ثابت + مذهبی/رسمی) */
    fun occasionsOf(date: JalaliDate): List<Occasion> {
        val result = mutableListOf<Occasion>()
        result.addAll(fixedOccasions[k(date.month, date.day)].orEmpty())

        val official = officialOverrides[date.year]
        if (official != null) {
            result.addAll(official[k(date.month, date.day)].orEmpty())
        } else {
            val h = date.toHijri()
            result.addAll(religiousByHijri[k(h.month, HijriCalendar.safeDay(h.year, h.month, h.day))].orEmpty())
        }
        return result
    }

    /** تعطیل رسمی بودن روز (جمعه جداگانه در UI رنگ می‌شود) */
    fun isOfficialHoliday(date: JalaliDate): Boolean {
        if (nationalHolidays.contains(k(date.month, date.day))) return true
        val official = officialOverrides[date.year]
        return if (official != null) {
            official[k(date.month, date.day)].orEmpty().any { it.holiday }
        } else {
            val h = date.toHijri()
            religiousByHijri[k(h.month, HijriCalendar.safeDay(h.year, h.month, h.day))]
                .orEmpty().any { it.holiday }
        }
    }

    /** مناسبت‌های یک ماه برای صفحه مناسبت‌ها؛ کلید = روز ماه */
    fun occasionsOfMonth(jy: Int, jm: Int): Map<Int, List<Occasion>> {
        val len = ir.hooshmandcalendar.app.core.JalaaliCalendar.lengthOfMonth(jm, jy)
        val map = mutableMapOf<Int, List<Occasion>>()
        for (d in 1..len) {
            val list = occasionsOf(JalaliDate(jy, jm, d))
            if (list.isNotEmpty()) map[d] = list
        }
        return map
    }

    /** متن خلاصه برای نمایش زیر روزها */
    fun shortOccasionsOf(date: JalaliDate): String =
        occasionsOf(date).joinToString("، ") { it.title }
}

/** برچسب فارسی انواع مناسبت */
fun OccasionKind.label(): String = when (this) {
    OccasionKind.NATIONAL -> "مناسبت ملی"
    OccasionKind.RELIGIOUS -> "مذهبی"
    OccasionKind.INTERNATIONAL -> "بین‌المللی"
}

/** متن راهنمای دقت تقویم قمری */
fun hijriAccuracyNote(jy: Int): String =
    if (Occasions.hasOfficialData(jy))
        "تاریخ‌های قمری و مناسبت‌های مذهبی این سال بر اساس تقویم رسمی ایران است."
    else
        "تاریخ قمری این سال به‌صورت الگوریتمی محاسبه شده و ممکن است با تقویم رسمی ایران ±۱ روز اختلاف داشته باشد." +
            " تاریخ: " + jy.toPersianDigits()
