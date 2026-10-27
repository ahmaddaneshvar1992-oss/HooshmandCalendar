package ir.hooshmandcalendar.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.data.BackupManager
import ir.hooshmandcalendar.app.data.ThemeMode
import ir.hooshmandcalendar.app.reminders.TodayNotification
import ir.hooshmandcalendar.app.ui.theme.ACCENTS
import kotlinx.coroutines.launch

/** صفحه تنظیمات: نمایش، یادآورها، داده‌ها و درباره */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val events by vm.allEvents.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // پشتیبان‌گیری: ساخت فایل
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = BackupManager.buildJson(events)
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(json.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("نوشتن فایل ممکن نشد")
                }.onSuccess {
                    snackbar.showSnackbar("پشتیبان‌گیری انجام شد")
                }.onFailure {
                    snackbar.showSnackbar("پشتیبان‌گیری ناموفق بود")
                }
            }
        }
    }

    // بازیابی: خواندن فایل
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    BackupManager.importFromJson(context, uri, events)
                }
                result.onSuccess {
                    if (it.added > 0) {
                        ReminderSchedulerHelper.rescheduleAll(context)
                        snackbar.showSnackbar("${it.added.toPersianDigits()} رویداد افزوده شد")
                    } else {
                        snackbar.showSnackbar("رویداد جدیدی در فایل نبود")
                    }
                }.onFailure {
                    snackbar.showSnackbar("خواندن فایل ناموفق بود")
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "تنظیمات",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // ---------- نمایش ----------
        SettingsSection(title = "نمایش") {
            Text("حالت برنامه", style = MaterialTheme.typography.bodyMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(
                    ThemeMode.SYSTEM to "سیستم",
                    ThemeMode.LIGHT to "روشن",
                    ThemeMode.DARK to "تاریک"
                ).forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = { scope.launch { vm.settingsStore.setThemeMode(mode) } },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("پوسته رنگی", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                ACCENTS.forEachIndexed { index, accent ->
                    val selected = settings.accentIndex == index
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .size(if (selected) 40.dp else 34.dp)
                            .clip(CircleShape)
                            .background(accent.primary)
                            .then(
                                if (selected) Modifier.border(
                                    2.5.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable { scope.launch { vm.settingsStore.setAccent(index) } }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            ToggleRow(
                title = "نمایش اعداد فارسی",
                checked = settings.persianDigits,
                onChange = { scope.launch { vm.settingsStore.setPersianDigits(it) } }
            )
            ToggleRow(
                title = "نمایش مناسبت‌ها در تقویم",
                checked = settings.showOccasions,
                onChange = { scope.launch { vm.settingsStore.setShowOccasions(it) } }
            )
            ToggleRow(
                title = "نمایش تاریخ امروز در نوار وضعیت",
                checked = settings.showTodayNotification,
                onChange = { enabled ->
                    scope.launch { vm.settingsStore.setShowTodayNotification(enabled) }
                    if (enabled) TodayNotification.postAndSchedule(context)
                    else TodayNotification.cancel(context)
                }
            )
        }

        // ---------- داده‌ها ----------
        SettingsSection(title = "داده‌ها") {
            Text(
                text = "رویدادهای شما فقط روی همین گوشی ذخیره می‌شوند. با پشتیبان‌گیری می‌توانید از آن‌ها نسخه پشتیبان بگیرید و پس از نصب مجدد بازیابی کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            ActionRow(
                icon = { Icon(Icons.Rounded.CloudUpload, null, tint = MaterialTheme.colorScheme.primary) },
                title = "پشتیبان‌گیری از رویدادها",
                subtitle = "${events.size.toPersianDigits()} رویداد ثبت شده"
            ) {
                exportLauncher.launch("hooshmand-calendar-backup.json")
            }
            ActionRow(
                icon = { Icon(Icons.Rounded.CloudDownload, null, tint = MaterialTheme.colorScheme.primary) },
                title = "بازیابی از فایل پشتیبان",
                subtitle = "فایل JSON پشتیبان را انتخاب کنید"
            ) {
                runCatching { importLauncher.launch(arrayOf("application/json", "text/*")) }
            }
        }

        // ---------- درباره ----------
        SettingsSection(title = "درباره برنامه") {
            Row {
                Text("نسخه", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text("۱٫۱٫۰", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "حریم خصوصی",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "این برنامه کاملاً آفلاین است؛ هیچ داده‌ای از شما جمع‌آوری یا ارسال نمی‌شود. رویدادها، تنظیمات و یادآورها فقط در حافظه خود گوشی ذخیره می‌مانند. اوقات شرعی و قبله‌نما بدون اینترنت و فقط با محاسبه نجومی و حسگر قطب‌نمای گوشی کار می‌کنند و تنها مجوزهای برنامه، اعلان‌ها و حسگرها است.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ActionRow(
                icon = { Icon(Icons.Rounded.Star, null, tint = MaterialTheme.colorScheme.primary) },
                title = "امتیاز دادن در کافه‌بازار",
                subtitle = "اگر برنامه برایتان مفید بود، به ما امتیاز بدهید"
            ) {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("bazaar://details?id=ir.hooshmandcalendar.app"))
                    )
                }.onFailure {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://cafebazaar.ir/app/ir.hooshmandcalendar.app"))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }

    SnackbarHost(hostState = snackbar, modifier = Modifier.padding(bottom = 80.dp))
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(Modifier.padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        icon()
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** کمک‌کننده برای دسترسی به زمان‌بند از UI */
private object ReminderSchedulerHelper {
    fun rescheduleAll(context: android.content.Context) {
        ir.hooshmandcalendar.app.reminders.ReminderScheduler.scheduleAll(context)
    }
}
