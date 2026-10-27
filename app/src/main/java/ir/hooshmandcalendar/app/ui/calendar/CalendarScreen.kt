package ir.hooshmandcalendar.app.ui.calendar

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.core.PersianDateNames
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.Occasions
import kotlinx.coroutines.flow.collectLatest

private const val BASE_YEAR = 1300
private const val YEAR_COUNT = 220

private fun monthIndex(date: JalaliDate) = (date.year - BASE_YEAR) * 12 + (date.month - 1)
private fun monthOfIndex(index: Int) = Pair(BASE_YEAR + index / 12, (index % 12) + 1)

@Composable
fun CalendarScreen(
    vm: AppViewModel,
    onOpenConverter: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenAzan: () -> Unit,
    onOpenEditor: (Long, Long) -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val selected by vm.selectedDate.collectAsStateWithLifecycle()
    val events by vm.allEvents.collectAsStateWithLifecycle()

    var showGoToDate by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenEditor(-1L, selected.epochDay) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "افزودن رویداد")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
        ) {
            // ---------- سربرگ ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenConverter) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = "تبدیل تاریخ",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${selected.monthName} ${selected.year.toPersianDigits()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.clickable { showMonthPicker = true }
                    )
                    Text(
                        text = selected.longFormat(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onOpenQibla) {
                    Icon(Icons.Rounded.Explore, contentDescription = "قبله‌نما",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onOpenAzan) {
                    Icon(Icons.Rounded.Mosque, contentDescription = "اذان و اوقات شرعی",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { showGoToDate = true }) {
                    Icon(Icons.Rounded.Today, contentDescription = "رفتن به تاریخ",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(6.dp))

            // ---------- بدنه (نمایش ماهانه) ----------
            MonthBody(
                vm = vm, selected = selected, events = events,
                showOccasions = settings.showOccasions,
                onOpenEditor = onOpenEditor
            )
        }
    }

    if (showGoToDate) {
        GoToDateDialog(
            initial = selected,
            onDismiss = { showGoToDate = false },
            onGo = { vm.selectDate(it) }
        )
    }
    if (showMonthPicker) {
        MonthPickerDialog(
            currentYear = selected.year,
            currentMonth = selected.month,
            onDismiss = { showMonthPicker = false },
            onPick = { y, m ->
                val len = ir.hooshmandcalendar.app.core.JalaaliCalendar.lengthOfMonth(m, y)
                vm.selectDate(JalaliDate(y, m, selected.day.coerceAtMost(len)))
            }
        )
    }
}

// ---------------------------------------------------------------------
// حالت ماهانه
// ---------------------------------------------------------------------
@Composable
private fun MonthBody(
    vm: AppViewModel,
    selected: JalaliDate,
    events: List<ir.hooshmandcalendar.app.data.db.EventEntity>,
    showOccasions: Boolean,
    onOpenEditor: (Long, Long) -> Unit
) {
    val pageCount = YEAR_COUNT * 12
    val pagerState = rememberPagerState(
        initialPage = monthIndex(selected),
        pageCount = { pageCount }
    )

    // همگام‌سازی انتخاب با صفحه
    LaunchedEffect(selected) {
        val target = monthIndex(selected)
        if (pagerState.currentPage != target && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(target)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            val (y, m) = monthOfIndex(page)
            if (selected.year != y || selected.month != m) {
                val len = ir.hooshmandcalendar.app.core.JalaaliCalendar.lengthOfMonth(m, y)
                vm.selectDate(JalaliDate(y, m, selected.day.coerceAtMost(len)))
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        WeekdayHeaderRow(Modifier.padding(vertical = 4.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) { page ->
            val (y, m) = monthOfIndex(page)
            val first = JalaliDate(y, m, 1)
            val last = JalaliDate(y, m, ir.hooshmandcalendar.app.core.JalaaliCalendar.lengthOfMonth(m, y))
            val colorsMap = remember(page, events) { vm.eventColorsInRange(events, first.minusDays(7), last.plusDays(7)) }
            Box(Modifier.padding(horizontal = 4.dp)) {
                MonthGrid(
                    monthYear = Pair(y, m),
                    selected = selected,
                    eventColors = colorsMap,
                    showOccasions = showOccasions,
                    onSelect = { vm.selectDate(it) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        val dayEvents = remember(selected, events) { vm.eventsFor(selected, events) }
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                DayDetailCard(
                    date = selected,
                    events = dayEvents,
                    showOccasions = showOccasions,
                    onOpenEvent = onOpenEditor
                )
            }
            item { Spacer(Modifier.height(56.dp)) }
        }
    }
}
