package ir.hooshmandcalendar.app.ui.occasions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.PersianDateNames
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.Occasion
import ir.hooshmandcalendar.app.data.OccasionKind
import ir.hooshmandcalendar.app.data.Occasions
import ir.hooshmandcalendar.app.data.hijriAccuracyNote
import ir.hooshmandcalendar.app.data.label
import ir.hooshmandcalendar.app.ui.theme.holidayColor

/** صفحه مناسبت‌ها: فهرست کامل مناسبت‌های سال با فیلتر (مطابق طرح) */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OccasionsScreen() {
    val today = JalaliDate.today()
    var year by remember { mutableStateOf(today.year) }
    var filter by remember { mutableStateOf(0) } // 0=همه 1=رسمی 2=مذهبی

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // سربرگ سال
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            TextButton(onClick = { if (year > 1300) year -= 1 }) { Text("‹") }
            Text(
                text = "مناسبت‌های سال ${year.toPersianDigits()}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            TextButton(onClick = { if (year < 1500) year += 1 }) { Text("›") }
        }

        // فیلتر
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("همه", "رسمی", "مذهبی").forEachIndexed { index, label ->
                SegmentedButton(
                    selected = filter == index,
                    onClick = { filter = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Text(
            text = hijriAccuracyNote(year),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        val months = remember(year, filter) {
            (1..12).map { m ->
                val days = Occasions.occasionsOfMonth(year, m)
                    .mapValues { (_, list) ->
                        when (filter) {
                            1 -> list.filter { it.holiday }               // رسمی = همه تعطیلات رسمی
                            2 -> list.filter { it.kind == OccasionKind.RELIGIOUS } // مذهبی
                            else -> list
                        }
                    }
                    .filterValues { it.isNotEmpty() }
                m to days
            }.filter { it.second.isNotEmpty() }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            months.forEach { (month, daysMap) ->
                item(key = "header_$month") {
                    Text(
                        text = PersianDateNames.jalaliMonths[month - 1],
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                items(daysMap.entries.toList(), key = { "${month}_${it.key}" }) { (day, occasions) ->
                    OccasionRow(year, month, day, occasions)
                }
            }
            item { Spacer(Modifier.padding(bottom = 16.dp)) }
        }
    }
}

@Composable
private fun OccasionRow(year: Int, month: Int, day: Int, occasions: List<Occasion>) {
    val isHoliday = occasions.any { it.holiday }
    val date = JalaliDate(year, month, day)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isHoliday) holidayColor().copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // تاریخ
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(74.dp)
            ) {
                Text(
                    text = "${day.toPersianDigits()} ${PersianDateNames.jalaliMonths[month - 1]}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isHoliday || date.weekdayIndex == 6) holidayColor()
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date.weekdayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            // عنوان‌ها
            Column(Modifier.weight(1f)) {
                occasions.forEach { occ ->
                    Text(
                        text = occ.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            // برچسب نوع (مطابق طرح)
            val kind = occasions.first().kind
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (kind) {
                    OccasionKind.NATIONAL -> MaterialTheme.colorScheme.primaryContainer
                    OccasionKind.RELIGIOUS -> holidayColor().copy(alpha = 0.14f)
                    OccasionKind.INTERNATIONAL -> MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = when (kind) {
                    OccasionKind.NATIONAL -> MaterialTheme.colorScheme.onPrimaryContainer
                    OccasionKind.RELIGIOUS -> holidayColor()
                    OccasionKind.INTERNATIONAL -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Text(
                    text = if (isHoliday) "تعطیل رسمی" else kind.label(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
