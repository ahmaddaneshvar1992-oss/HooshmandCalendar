package ir.hooshmandcalendar.app.prayer

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.R
import ir.hooshmandcalendar.app.core.DayTime
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.Cities
import ir.hooshmandcalendar.app.ui.components.CityPicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/** صفحه اذان و اوقات شرعی — کاملاً آفلاین */
@Composable
fun AzanScreen(
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val city = Cities.safe(settings.cityIndex)
    val method = PrayerTimes.Method.entries.getOrElse(settings.azanMethodIndex) { PrayerTimes.Method.IRAN }
    val times = remember(city, method) { PrayerTimes.computeToday(city, method) }

    // ساعت فعلی برای برجسته‌کردن اذان بعدی (به‌روزرسانی هر ۳۰ ثانیه)
    var nowMinutes by remember { mutableIntStateOf(currentMinutes()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMinutes = currentMinutes()
            delay(30_000)
        }
    }

    // پخش‌کننده اذان
    var playing by remember { mutableStateOf(false) }
    val player = remember { MediaPlayer.create(context, R.raw.azan) }
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (_: Exception) {
            }
        }
    }

    fun togglePlay() {
        try {
            if (playing) {
                player.pause()
                player.seekTo(0)
                playing = false
            } else {
                player.start()
                playing = true
            }
        } catch (_: Exception) {
        }
    }

    val rows = listOf(
        PrayerTimes.Kind.FAJR to times.fajr,
        PrayerTimes.Kind.SUNRISE to times.sunrise,
        PrayerTimes.Kind.DHUHR to times.dhuhr,
        PrayerTimes.Kind.ASR to times.asr,
        PrayerTimes.Kind.MAGHRIB to times.maghrib,
        PrayerTimes.Kind.ISHA to times.isha
    )
    val nextEntry = rows.filter { it.first != PrayerTimes.Kind.SUNRISE }
        .firstOrNull { it.second > nowMinutes }
        ?: (PrayerTimes.Kind.FAJR to times.fajr + 1440)
    val remainMinutes = nextEntry.second - nowMinutes

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "اذان و اوقات شرعی",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(6.dp))

        // شمارش معکوس اذان بعدی
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "اذان بعدی: ${nextEntry.first.label}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ساعت ${DayTime(nextEntry.second % 1440).format()} - ${
                        (remainMinutes / 60).toPersianDigits()
                    } ساعت و ${(remainMinutes % 60).toPersianDigits()} دقیقه مانده",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // دکمه پخش اذان
        Button(
            onClick = { togglePlay() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = if (playing) "توقف پخش اذان" else "پخش اذان",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        // شهر و روش محاسبه
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "شهر",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                CityPicker(
                    selectedIndex = settings.cityIndex,
                    onSelect = { index ->
                        scope.launch {
                            vm.settingsStore.setCityIndex(index)
                            if (settings.azanEnabled) {
                                AzanScheduler.scheduleNext(
                                    context,
                                    Cities.safe(index),
                                    method
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "روش محاسبه",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                PrayerTimes.Method.entries.forEach { m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = m.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (m == method) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = m == method,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    scope.launch {
                                        vm.settingsStore.setAzanMethod(m.ordinal)
                                        if (settings.azanEnabled) {
                                            AzanScheduler.scheduleNext(context, city, m)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "زنگ خودکار اذان",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "در اوقات شرعی، اذان با اعلان پخش می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.azanEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                vm.settingsStore.setAzanEnabled(enabled)
                                if (enabled) AzanScheduler.scheduleNext(context, city, method)
                                else AzanScheduler.cancel(context)
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // جدول اوقات امروز
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "اوقات شرعی امروز - ${city.name}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                rows.forEach { (kind, minutes) ->
                    val isNext = kind == nextEntry.first
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isNext) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = kind.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isNext) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isNext) {
                                Text(
                                    text = "بعدی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                            }
                            Text(
                                text = DayTime(minutes).format(),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isNext) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "محاسبه کاملاً آفلاین بر اساس موقعیت خورشید انجام می‌شود؛ چند دقیقه اختلاف با جدول رسمی طبیعی است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

private fun currentMinutes(): Int {
    val c = Calendar.getInstance()
    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
}
