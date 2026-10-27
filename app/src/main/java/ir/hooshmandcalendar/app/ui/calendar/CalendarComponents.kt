package ir.hooshmandcalendar.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.JalaaliCalendar
import ir.hooshmandcalendar.app.core.PersianDateNames
import ir.hooshmandcalendar.app.core.toEnglishDigits
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.core.parseIntSafe
import ir.hooshmandcalendar.app.data.Occasion
import ir.hooshmandcalendar.app.data.Occasions
import ir.hooshmandcalendar.app.data.OccasionKind
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.data.label
import ir.hooshmandcalendar.app.ui.theme.EVENT_COLORS
import ir.hooshmandcalendar.app.ui.theme.holidayColor

/** سربرگ روزهای هفته با نام کامل: شنبه تا جمعه (جمعه قرمز) */
@Composable
fun WeekdayHeaderRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        PersianDateNames.weekdays.forEachIndexed { i, name ->
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                maxLines = 1,
                color = if (i == 6) holidayColor() else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** یک خانه روز در تقویم */
@Composable
fun DayCell(
    day: JalaliDate,
    isSelected: Boolean,
    eventColors: List<Int>,
    showOccasion: Boolean,
    onSelect: (JalaliDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHoliday = Occasions.isOfficialHoliday(day)
    val isFriday = day.weekdayIndex == 6
    val hasOccasion = showOccasion && Occasions.occasionsOf(day).isNotEmpty()
    val today = day.isToday

    val numberColor = when {
        today -> MaterialTheme.colorScheme.onPrimary
        isHoliday || isFriday -> holidayColor()
        else -> MaterialTheme.colorScheme.onSurface
    }

    val bg = when {
        today -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (isSelected && !today) Modifier.border(
                    1.5.dp, MaterialTheme.colorScheme.primary, CircleShape
                ) else Modifier
            )
            .clickable { onSelect(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.day.toPersianDigits(),
                style = MaterialTheme.typography.bodyMedium,
                color = numberColor
            )
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                val dots = eventColors.take(3)
                dots.forEach { colorIndex ->
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (today) MaterialTheme.colorScheme.onPrimary
                                else EVENT_COLORS.getOrElse(colorIndex) { EVENT_COLORS[0] }
                            )
                    )
                }
            }
        }
        if (hasOccasion && !today) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(holidayColor())
            )
        }
    }
}

/** شبکه یک ماه کامل */
@Composable
fun MonthGrid(
    monthYear: Pair<Int, Int>, // (سال، ماه)
    selected: JalaliDate,
    eventColors: Map<Long, List<Int>>,
    showOccasions: Boolean,
    onSelect: (JalaliDate) -> Unit
) {
    val (year, month) = monthYear
    val len = JalaaliCalendar.lengthOfMonth(month, year)
    val firstDay = JalaliDate(year, month, 1)
    val leading = firstDay.weekdayIndex // تعداد خانه‌های خالی ابتدای هفته

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var day = 1 - leading
        while (day <= len) {
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        val d = day + col
                        if (d in 1..len) {
                            val date = JalaliDate(year, month, d)
                            DayCell(
                                day = date,
                                isSelected = date == selected,
                                eventColors = eventColors[date.epochDay].orEmpty(),
                                showOccasion = showOccasions,
                                onSelect = onSelect
                            )
                        }
                    }
                }
            }
            day += 7
        }
    }
}

/** چیپ مناسبت */
@Composable
fun OccasionChip(occasion: Occasion) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (occasion.holiday) holidayColor().copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (occasion.holiday) holidayColor()
        else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = if (occasion.holiday) occasion.title else "${occasion.title} · ${occasion.kind.label()}",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** کارت جزئیات روز انتخابی (مطابق طرح: تاریخ + مناسبت‌ها + رویدادها) */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayDetailCard(
    date: JalaliDate,
    events: List<EventEntity>,
    showOccasions: Boolean,
    onOpenEvent: (Long, Long) -> Unit
) {
    val occasions = if (showOccasions) Occasions.occasionsOf(date) else emptyList()
    val hijri = date.toHijri()
    val greg = date.toGregorian()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = date.longFormat(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "هفته ${date.weekOfYear.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = hijri.shortFormat() + "  •  " +
                    "${greg.day.toPersianDigits()} ${greg.monthName} ${greg.year.toPersianDigits()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (occasions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    occasions.forEach { OccasionChip(it) }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (events.isEmpty()) {
                Text(
                    text = "رویدادی برای این روز ثبت نشده است",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    events.forEach { event ->
                        EventRow(event = event, onClick = { onOpenEvent(event.id, date.epochDay) })
                    }
                }
            }
        }
    }
}

/** یک ردیف رویداد (مطابق طرح: آیکون رنگی + عنوان + ساعت) */
@Composable
fun EventRow(event: EventEntity, onClick: () -> Unit) {
    val color = EVENT_COLORS.getOrElse(event.colorIndex) { EVENT_COLORS[0] }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.14f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (event.isBirthday) Icons.Rounded.Cake else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (event.allDay) "تمام‌روز"
            else event.endMinutes?.let {
                "${ir.hooshmandcalendar.app.core.DayTime(event.startMinutes).format()} تا ${ir.hooshmandcalendar.app.core.DayTime(it).format()}"
            } ?: ir.hooshmandcalendar.app.core.DayTime(event.startMinutes).format(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** گفت‌وگوی رفتن سریع به تاریخ دلخواه */
@Composable
fun GoToDateDialog(
    initial: JalaliDate,
    onDismiss: () -> Unit,
    onGo: (JalaliDate) -> Unit
) {
    var dayText by remember { mutableStateOf(initial.day.toPersianDigits()) }
    var monthText by remember { mutableStateOf(initial.month.toPersianDigits()) }
    var yearText by remember { mutableStateOf(initial.year.toPersianDigits()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رفتن به تاریخ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "تاریخ شمسی موردنظر را وارد کنید (اعداد فارسی یا انگلیسی):",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = it },
                        label = { Text("روز") },
                        modifier = Modifier.weight(1f),
                        isError = error != null,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = monthText,
                        onValueChange = { monthText = it },
                        label = { Text("ماه") },
                        modifier = Modifier.weight(1f),
                        isError = error != null,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it },
                        label = { Text("سال") },
                        modifier = Modifier.weight(1.3f),
                        isError = error != null,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val d = dayText.parseIntSafe()
                val m = monthText.parseIntSafe()
                val y = yearText.parseIntSafe()
                when {
                    d == null || m == null || y == null ->
                        error = "اعداد واردشده معتبر نیست"
                    m !in 1..12 -> error = "ماه باید بین ۱ تا ۱۲ باشد"
                    d !in 1..JalaaliCalendar.lengthOfMonth(m, y) ->
                        error = "این ماه ${JalaaliCalendar.lengthOfMonth(m, y).toPersianDigits()} روز دارد"
                    y !in 1300..1500 -> error = "سال باید بین ۱۳۰۰ تا ۱۵۰۰ باشد"
                    else -> {
                        onGo(JalaliDate(y, m, d))
                        onDismiss()
                    }
                }
            }) { Text("برو") }
        },
        dismissButton = {
            TextButton(onClick = {
                onGo(JalaliDate.today())
                onDismiss()
            }) { Text("امروز") }
        }
    )
}

/** گفت‌وگوی انتخاب سریع ماه و سال */
@Composable
fun MonthPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onPick: (Int, Int) -> Unit
) {
    var year by remember { mutableStateOf(currentYear) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (year > 1300) year -= 1 }) { Text("‹") }
                Text(
                    text = year.toPersianDigits(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
                TextButton(onClick = { if (year < 1500) year += 1 }) { Text("›") }
            }
        },
        text = {
            Column {
                for (row in 0 until 3) {
                    Row {
                        for (col in 0 until 4) {
                            val m = row * 4 + col + 1
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                val selected = m == currentMonth && year == currentYear
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else androidx.compose.ui.graphics.Color.Transparent,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onPick(year, m); onDismiss() }
                                ) {
                                    Text(
                                        text = PersianDateNames.jalaliMonths[m - 1],
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
