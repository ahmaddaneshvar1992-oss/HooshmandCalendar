package ir.hooshmandcalendar.app.core

/** تبدیل ارقام فارسی/انگلیسی */
private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun Int.toPersianDigits(): String = toString().toPersianDigits()
fun Long.toPersianDigits(): String = toString().toPersianDigits()

fun String.toPersianDigits(): String = buildString {
    for (c in this@toPersianDigits) {
        append(if (c in '0'..'9') persianDigits[c - '0'] else c)
    }
}

fun String.toEnglishDigits(): String = buildString {
    for (c in this@toEnglishDigits) {
        when (c) {
            in '۰'..'۹' -> append(('0' + (c - '۰')))
            in '٠'..'٩' -> append(('0' + (c - '٠')))
            else -> append(c)
        }
    }
}

/** فقط عدد از ورودی متن */
fun String.parseIntSafe(): Int? = toEnglishDigits().filter { it.isDigit() }.toIntOrNull()
