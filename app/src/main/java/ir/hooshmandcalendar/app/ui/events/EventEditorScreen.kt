package ir.hooshmandcalendar.app.ui.events

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.core.DayTime
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.data.db.RepeatType
import ir.hooshmandcalendar.app.ui.theme.EVENT_COLORS
import ir.hooshmandcalendar.app.ui.theme.EVENT_COLOR_NAMES
import kotlinx.coroutines.launch

private val REMINDER_OPTIONS = listOf(
    null to "بدون یادآور",
    0 to "در زمان رویداد",
    5 to "۵ دقیقه قبل",
    10 to "۱۰ دقیقه قبل",
    15 to "۱۵ دقیقه قبل",
    30 to "۳۰ دقیقه قبل",
    60 to "۱ ساعت قبل",
    1440 to "۱ روز قبل"
)

private val REPEAT_OPTIONS = listOf(
    RepeatType.NONE to "هیچ‌وقت",
    RepeatType.DAILY to "روزانه",
    RepeatType.WEEKLY to "هفتگی",
    RepeatType.MONTHLY to "ماهانه",
    RepeatType.YEARLY to "سالانه"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(
    vm: AppViewModel,
    eventId: Long,
    initialDay: Long,
    onBack: () -> Unit
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = eventId > 0

    var state by remember { mutableStateOf(EditorState.default(initialDay)) }
    var titleError by remember { mutableStateOf(false) }

    // بارگذاری رویداد موجود
    LaunchedEffect(eventId) {
        if (isEditing) {
            vm.getEvent(eventId)?.let {
                state = EditorState.from(it)
            }
        }
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePickerStart by remember { mutableStateOf(false) }
    var showTimePickerEnd by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }

    // انتخاب زنگ از سیستم
    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            state = state.copy(soundUri = uri?.toString())
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            // سربرگ: بستن | عنوان | ذخیره (مطابق طرح)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, contentDescription = "بستن")
                }
                Text(
                    text = if (isEditing) "ویرایش رویداد" else "افزودن رویداد",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = {
                        if (state.title.isBlank()) {
                            titleError = true
                            return@IconButton
                        }
                        val entity = state.toEntity(eventId)
                        vm.saveEvent(entity) { onBack() }
                    }
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "ذخیره",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // عنوان رویداد
                OutlinedTextField(
                    value = state.title,
                    onValueChange = {
                        titleError = false
                        state = state.copy(title = it)
                    },
                    label = { Text("عنوان رویداد") },
                    placeholder = { Text("جلسه با تیم") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("عنوان را وارد کنید") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // تاریخ و زمان
                EditorSection(title = "تاریخ و زمان") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showDatePicker = true }
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                state.date.shortFormat(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text("تمام‌روز", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = state.allDay,
                            onCheckedChange = { state = state.copy(allDay = it) }
                        )
                    }
                    if (!state.allDay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { showTimePickerStart = true }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.AccessTime, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(DayTime(state.startMinutes).format(), style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(" تا ", style = MaterialTheme.typography.bodyMedium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { showTimePickerEnd = true }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.AccessTime, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    state.endMinutes?.let { DayTime(it).format() } ?: "--:--",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // تکرار رویداد
                EditorSection(title = "تکرار رویداد") {
                    DropdownRow(
                        icon = Icons.Rounded.Repeat,
                        value = REPEAT_OPTIONS.first { it.first == state.repeatType }.second,
                        expanded = showRepeatMenu,
                        onExpand = { showRepeatMenu = it }
                    ) {
                        REPEAT_OPTIONS.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    state = state.copy(repeatType = type)
                                    showRepeatMenu = false
                                }
                            )
                        }
                    }
                }

                // یادآور
                EditorSection(title = "یادآور") {
                    DropdownRow(
                        icon = Icons.Rounded.Notifications,
                        value = REMINDER_OPTIONS.first { it.first == state.reminderOffset }.second,
                        expanded = showReminderMenu,
                        onExpand = { showReminderMenu = it }
                    ) {
                        REMINDER_OPTIONS.forEach { (offset, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    state = state.copy(reminderOffset = offset)
                                    showReminderMenu = false
                                }
                            )
                        }
                    }
                    // صدای یادآور
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "صدای یادآور")
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                    putExtra(
                                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                        state.soundUri?.let { Uri.parse(it) }
                                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                                    )
                                }
                                runCatching { ringtoneLauncher.launch(intent) }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("صدای یادآور", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = when {
                                state.soundUri == null -> "پیش‌فرض سیستم"
                                else -> runCatching {
                                    RingtoneManager.getRingtone(context, Uri.parse(state.soundUri))
                                        ?.getTitle(context) ?: "سفارشی"
                                }.getOrDefault("سفارشی")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("لرزش", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = state.vibrate,
                            onCheckedChange = { state = state.copy(vibrate = it) }
                        )
                    }
                }

                // رنگ رویداد (۸ رنگ مطابق طرح)
                EditorSection(title = "رنگ رویداد") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EVENT_COLORS.forEachIndexed { index, color ->
                            val selectedColor = state.colorIndex == index
                            Box(
                                Modifier
                                    .size(if (selectedColor) 34.dp else 30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (selectedColor) Modifier.border(
                                            2.5.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { state = state.copy(colorIndex = index) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor) {
                                    Icon(
                                        Icons.Rounded.Check, null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("تولد / سالگرد", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "هر سال به‌صورت خودکار یادآوری می‌شود",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isBirthday,
                            onCheckedChange = { isBirthday ->
                                state = state.copy(
                                    isBirthday = isBirthday,
                                    repeatType = if (isBirthday) RepeatType.YEARLY else state.repeatType
                                )
                            }
                        )
                    }
                }

                // توضیحات
                EditorSection(title = "توضیحات (اختیاری)") {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { state = state.copy(description = it) },
                        placeholder = { Text("توضیحات رویداد را وارد کنید…") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isEditing) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.size(6.dp))
                        Text("حذف رویداد", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ---------- دیالوگ‌ها ----------
    if (showDatePicker) {
        MiniDatePickerDialog(
            initial = state.date,
            onDismiss = { showDatePicker = false },
            onPick = {
                state = state.copy(date = it)
                showDatePicker = false
            }
        )
    }
    if (showTimePickerStart) {
        TimePickerDialog(
            title = "ساعت شروع",
            initialMinutes = state.startMinutes,
            onDismiss = { showTimePickerStart = false }
        ) { minutes ->
            state = state.copy(startMinutes = minutes)
            if (state.endMinutes == null) state = state.copy(endMinutes = (minutes + 60).coerceAtMost(24 * 60 - 1))
            showTimePickerStart = false
        }
    }
    if (showTimePickerEnd) {
        TimePickerDialog(
            title = "ساعت پایان",
            initialMinutes = state.endMinutes ?: (state.startMinutes + 60).coerceAtMost(24 * 60 - 1),
            onDismiss = { showTimePickerEnd = false }
        ) { minutes ->
            state = state.copy(endMinutes = minutes)
            showTimePickerEnd = false
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف رویداد") },
            text = { Text("این رویداد حذف شود؟ این عمل قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vm.getEvent(eventId)?.let { vm.deleteEvent(it) }
                        showDeleteConfirm = false
                        onBack()
                    }
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") }
            }
        )
    }
}

// ------------------------------------------------------------------
// وضعیت فرم
// ------------------------------------------------------------------
private data class EditorState(
    val title: String = "",
    val description: String = "",
    val date: JalaliDate = JalaliDate.today(),
    val allDay: Boolean = false,
    val startMinutes: Int = 600,
    val endMinutes: Int? = null,
    val colorIndex: Int = 4,
    val reminderOffset: Int? = 15,
    val repeatType: Int = RepeatType.NONE,
    val soundUri: String? = null,
    val vibrate: Boolean = true,
    val isBirthday: Boolean = false
) {
    fun toEntity(id: Long): EventEntity = EventEntity(
        id = if (id > 0) id else 0,
        title = title.trim(),
        description = description.trim(),
        jYear = date.year,
        jMonth = date.month,
        jDay = date.day,
        dayEpochDay = date.epochDay,
        allDay = allDay,
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        colorIndex = colorIndex,
        reminderOffsetMinutes = reminderOffset,
        repeatType = repeatType,
        soundUri = soundUri,
        vibrate = vibrate,
        isBirthday = isBirthday
    )

    companion object {
        fun default(epochDay: Long): EditorState {
            val date = if (epochDay > 0) JalaliDate.fromEpochDay(epochDay) else JalaliDate.today()
            return EditorState(date = date)
        }

        fun from(e: EventEntity): EditorState = EditorState(
            title = e.title,
            description = e.description,
            date = JalaliDate(e.jYear, e.jMonth, e.jDay),
            allDay = e.allDay,
            startMinutes = e.startMinutes,
            endMinutes = e.endMinutes,
            colorIndex = e.colorIndex,
            reminderOffset = e.reminderOffsetMinutes,
            repeatType = e.repeatType,
            soundUri = e.soundUri,
            vibrate = e.vibrate,
            isBirthday = e.isBirthday
        )
    }
}

// ------------------------------------------------------------------
// اجزای کمکی فرم
// ------------------------------------------------------------------
@Composable
private fun EditorSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) { content() }
        }
    }
}

@Composable
private fun DropdownRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    items: @Composable () -> Unit
) {
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onExpand(true) }
                .padding(vertical = 10.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text("▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpand(false) }, content = { items() })
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(state = timeState)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(timeState.hour * 60 + timeState.minute) }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

/** انتخابگر کوچک تاریخ شمسی (ماه/روز) */
@Composable
fun MiniDatePickerDialog(
    initial: JalaliDate,
    onDismiss: () -> Unit,
    onPick: (JalaliDate) -> Unit
) {
    var year by remember { mutableStateOf(initial.year) }
    var month by remember { mutableStateOf(initial.month) }
    var day by remember { mutableStateOf(initial.day) }
    val len = ir.hooshmandcalendar.app.core.JalaaliCalendar.lengthOfMonth(month, year)
    val safeDay = day.coerceAtMost(len)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { year -= 1 }) { Text("‹") }
                Text(
                    "${ir.hooshmandcalendar.app.core.PersianDateNames.jalaliMonths[month - 1]} ${year.toPersianDigits()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                TextButton(onClick = { year += 1 }) { Text("›") }
            }
        },
        text = {
            Column {
                // انتخاب ماه
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { month = if (month == 1) 12 else month - 1 }) { Text("ماه قبلی") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { month = if (month == 12) 1 else month + 1 }) { Text("ماه بعدی") }
                }
                // شبکه روزها
                val leading = JalaliDate(year, month, 1).weekdayIndex
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    var d = 1 - leading
                    while (d <= len) {
                        Row {
                            repeat(7) { col ->
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    val dd = d + col
                                    if (dd in 1..len) {
                                        val selected = dd == safeDay
                                        Box(
                                            Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selected) MaterialTheme.colorScheme.primary
                                                    else Color.Transparent
                                                )
                                                .clickable { day = dd },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                dd.toPersianDigits(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        d += 7
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(JalaliDate(year, month, safeDay)) }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
