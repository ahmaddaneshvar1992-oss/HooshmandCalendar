package ir.hooshmandcalendar.app.ui.converter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.hooshmandcalendar.app.core.GregorianDate
import ir.hooshmandcalendar.app.core.HijriCalendar
import ir.hooshmandcalendar.app.core.HijriDate
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.PersianDateNames
import ir.hooshmandcalendar.app.core.toEnglishDigits
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.hijriAccuracyNote
import ir.hooshmandcalendar.app.ui.theme.holidayColor

private enum class CalendarType { JALALI, GREGORIAN, HIJRI }

/**
 * صفحه تبدیل تاریخ شمسی ↔ میلادی ↔ قمری.
 * هر سه‌گانه هم‌زمان به‌روزرسانی می‌شوند؛ ورودی هر تقویم دو تقویم دیگر را می‌سازد.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(onBack: () -> Unit) {
    var date by remember { mutableStateOf(JalaliDate.today()) }
    // کدام تقویم در حال ویرایش است تا بازخورد دایره‌ای رخ ندهد
    var editing by remember { mutableStateOf<CalendarType?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
            }
            Text(
                text = "تبدیل تاریخ",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            TextButton(onClick = {
                date = JalaliDate.today()
                editing = null
                error = null
            }) { Text("امروز") }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = date.longFormat(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "هفته ${date.weekOfYear.toPersianDigits()} از سال ${date.year.toPersianDigits()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        CalendarCard(
            title = "تقویم هجری شمسی",
            months = PersianDateNames.jalaliMonths,
            labels = Triple("روز", "ماه", "سال"),
            yearValue = date.year,
            monthValue = date.month,
            dayValue = date.day,
            onEdit = { y, m, d ->
                editing = CalendarType.JALALI
                val len = if (m in 1..12) ir.hooshmandcalendar.app.core.JalaaliCalendar.lengthOfMonth(m, y) else 0
                if (y !in 1300..1500 || m !in 1..12 || d !in 1..len) {
                    error = "تاریخ شمسی معتبر نیست"
                } else {
                    error = null
                    date = JalaliDate(y, m, d)
                }
            },
            onError = { error = "عدد واردشده معتبر نیست" }
        )

        Spacer(Modifier.height(12.dp))

        val g = date.toGregorian()
        CalendarCard(
            title = "تقویم میلادی",
            months = PersianDateNames.gregorianMonths,
            labels = Triple("روز", "ماه", "سال"),
            yearValue = g.year,
            monthValue = g.month,
            dayValue = g.day,
            onEdit = { y, m, d ->
                editing = CalendarType.GREGORIAN
                val len = if (m in 1..12) gregorianLength(m, y) else 0
                if (y !in 1900..2100 || m !in 1..12 || d !in 1..len) {
                    error = "تاریخ میلادی معتبر نیست"
                } else {
                    error = null
                    date = GregorianDate(y, m, d).toJalali()
                }
            },
            onError = { error = "عدد واردشده معتبر نیست" }
        )

        Spacer(Modifier.height(12.dp))

        val h = date.toHijri()
        CalendarCard(
            title = "تقویم هجری قمری",
            months = PersianDateNames.hijriMonths,
            labels = Triple("روز", "ماه", "سال"),
            yearValue = h.year,
            monthValue = h.month,
            dayValue = h.day,
            onEdit = { y, m, d ->
                editing = CalendarType.HIJRI
                val len = if (m in 1..12) HijriCalendar.lengthOfMonth(y, m) else 0
                if (y !in 1400..1500 || m !in 1..12 || d !in 1..len) {
                    error = "تاریخ قمری معتبر نیست"
                } else {
                    error = null
                    date = HijriCalendar.toJalali(y, m, d).let { JalaliDate(it[0], it[1], it[2]) }
                }
            },
            onError = { error = "عدد واردشده معتبر نیست" }
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = hijriAccuracyNote(date.year),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(24.dp))
    }
}

private fun gregorianLength(m: Int, y: Int): Int = when (m) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) 29 else 28
    else -> 0
}

@Composable
private fun CalendarCard(
    title: String,
    months: List<String>,
    labels: Triple<String, String, String>,
    yearValue: Int,
    monthValue: Int,
    dayValue: Int,
    onEdit: (Int, Int, Int) -> Unit,
    onError: () -> Unit
) {
    var dayText by remember(dayValue, title) { mutableStateOf(dayValue.toPersianDigits()) }
    var monthText by remember(monthValue, title) { mutableStateOf(monthValue.toPersianDigits()) }
    var yearText by remember(yearValue, title) { mutableStateOf(yearValue.toPersianDigits()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${months[monthValue - 1]} ${yearValue.toPersianDigits()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(
                    label = labels.first,
                    text = dayText,
                    onText = {
                        dayText = it
                        val d = it.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        val m = monthText.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        val y = yearText.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        if (d != null && m != null && y != null) onEdit(y, m, d) else onError()
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = labels.second,
                    text = monthText,
                    onText = {
                        monthText = it
                        val d = dayText.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        val m = it.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        val y = yearText.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        if (d != null && m != null && y != null) onEdit(y, m, d) else onError()
                    },
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    label = labels.third,
                    text = yearText,
                    onText = {
                        yearText = it
                        val d = dayText.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        val m = monthText.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        val y = it.toEnglishDigits().filter { c -> c.isDigit() }.toIntOrNull()
                        if (d != null && m != null && y != null) onEdit(y, m, d) else onError()
                    },
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    text: String,
    onText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onText,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}
