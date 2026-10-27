package ir.hooshmandcalendar.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.core.DayTime
import ir.hooshmandcalendar.app.prayer.AzanScreen
import ir.hooshmandcalendar.app.qibla.QiblaScreen
import ir.hooshmandcalendar.app.ui.calendar.CalendarScreen
import ir.hooshmandcalendar.app.ui.converter.ConverterScreen
import ir.hooshmandcalendar.app.ui.events.EventEditorScreen
import ir.hooshmandcalendar.app.ui.events.EventsScreen
import ir.hooshmandcalendar.app.ui.occasions.OccasionsScreen
import ir.hooshmandcalendar.app.ui.settings.SettingsScreen
import ir.hooshmandcalendar.app.ui.theme.HooshmandTheme

object Routes {
    const val CALENDAR = "calendar"
    const val EVENTS = "events"
    const val OCCASIONS = "occasions"
    const val SETTINGS = "settings"
    const val CONVERTER = "converter"
    const val QIBLA = "qibla"
    const val AZAN = "azan"
    const val EDITOR = "editor?eventId={eventId}&day={day}"
    fun editor(eventId: Long = -1L, day: Long = -1L) = "editor?eventId=$eventId&day=$day"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.CALENDAR, "تقویم", Icons.Rounded.CalendarMonth),
    Tab(Routes.EVENTS, "رویدادها", Icons.Rounded.Event),
    Tab(Routes.OCCASIONS, "مناسبت‌ها", Icons.Rounded.Star),
    Tab(Routes.SETTINGS, "تنظیمات", Icons.Rounded.Settings)
)

@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    // اعمال تنظیم اعداد فارسی روی فرمت زمان
    LaunchedEffect(settings.persianDigits) {
        DayTime.usePersianDigits = settings.persianDigits
    }

    HooshmandTheme(themeMode = settings.themeMode, accentIndex = settings.accentIndex) {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route
        val showBottomBar = currentRoute in tabs.map { it.route } ||
            currentRoute == Routes.EVENTS || currentRoute == null

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.CALENDAR,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.CALENDAR) {
                    CalendarScreen(
                        vm = vm,
                        onOpenConverter = { navController.navigate(Routes.CONVERTER) },
                        onOpenQibla = { navController.navigate(Routes.QIBLA) },
                        onOpenAzan = { navController.navigate(Routes.AZAN) },
                        onOpenEditor = { eventId, day -> navController.navigate(Routes.editor(eventId, day)) }
                    )
                }
                composable(Routes.EVENTS) {
                    EventsScreen(
                        vm = vm,
                        onOpenEditor = { eventId, day -> navController.navigate(Routes.editor(eventId, day)) }
                    )
                }
                composable(Routes.OCCASIONS) {
                    OccasionsScreen()
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(vm = vm)
                }
                composable(Routes.CONVERTER) {
                    ConverterScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.QIBLA) {
                    QiblaScreen(vm = vm, onBack = { navController.popBackStack() })
                }
                composable(Routes.AZAN) {
                    AzanScreen(vm = vm, onBack = { navController.popBackStack() })
                }
                composable(
                    route = Routes.EDITOR,
                    arguments = listOf(
                        navArgument("eventId") { defaultValue = -1L },
                        navArgument("day") { defaultValue = -1L }
                    )
                ) { entry ->
                    val eventId = entry.arguments?.getLong("eventId") ?: -1L
                    val day = entry.arguments?.getLong("day") ?: -1L
                    EventEditorScreen(
                        vm = vm,
                        eventId = eventId,
                        initialDay = day,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
