package ir.hooshmandcalendar.app.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.core.DayTime
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.data.db.RepeatType
import ir.hooshmandcalendar.app.ui.theme.EVENT_COLORS

/** صفحه رویدادها: فهرست همه رویدادها + جست‌وجو */
@Composable
fun EventsScreen(
    vm: AppViewModel,
    onOpenEditor: (Long, Long) -> Unit
) {
    var query by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val events by vm.allEvents.collectAsStateWithLifecycle()

    val filtered = androidx.compose.runtime.remember(events, query) {
        val q = query.trim()
        val list = if (q.isEmpty()) events
        else events.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true)
        }
        list.sortedWith(compareBy({ it.dayEpochDay }, { it.startMinutes }))
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "رویدادها",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("جست‌وجوی رویدادها…") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Close, null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.padding(6.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Event, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        if (query.isBlank()) "هنوز رویدادی ثبت نکرده‌اید.\nبا دکمه + رویداد جدید بسازید."
                        else "رویدادی مطابق جست‌وجو یافت نشد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { event ->
                    EventListRow(event) { onOpenEditor(event.id, event.dayEpochDay) }
                }
                item { Spacer(Modifier.padding(bottom = 12.dp)) }
            }
        }
    }
}

@Composable
private fun EventListRow(event: EventEntity, onClick: () -> Unit) {
    val color = EVENT_COLORS.getOrElse(event.colorIndex) { EVENT_COLORS[0] }
    val date = JalaliDate(event.jYear, event.jMonth, event.jDay)
    val isPast = event.dayEpochDay < JalaliDate.today().epochDay && event.repeatType == RepeatType.NONE

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = date.shortFormat() +
                        (if (!event.allDay) " · " + DayTime(event.startMinutes).format() else "") +
                        (if (event.repeatType != RepeatType.NONE) " · تکرار " + repeatLabel(event.repeatType) else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun repeatLabel(type: Int): String = when (type) {
    RepeatType.DAILY -> "روزانه"
    RepeatType.WEEKLY -> "هفتگی"
    RepeatType.MONTHLY -> "ماهانه"
    RepeatType.YEARLY -> "سالانه"
    else -> ""
}
