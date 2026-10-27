package ir.hooshmandcalendar.app.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hooshmandcalendar.app.AppViewModel
import ir.hooshmandcalendar.app.data.Cities
import ir.hooshmandcalendar.app.core.toPersianDigits
import ir.hooshmandcalendar.app.ui.components.CityPicker
import ir.hooshmandcalendar.app.ui.theme.holidayColor
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** محاسبه جهت قبله (زاویه از شمال جغرافیایی) */
object QiblaMath {
    fun initialBearing(lat: Double, lon: Double, toLat: Double, toLon: Double): Double {
        val p1 = Math.toRadians(lat)
        val l1 = Math.toRadians(lon)
        val p2 = Math.toRadians(toLat)
        val l2 = Math.toRadians(toLon)
        val dl = l2 - l1
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }
}

/** صفحه قبله‌نما: قطب‌نمای حسگری + فلش قبله */
@Composable
fun QiblaScreen(
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val city = Cities.safe(settings.cityIndex)
    val bearing = remember(city) {
        QiblaMath.initialBearing(
            city.lat, city.lon,
            Cities.KAABA_LAT, Cities.KAABA_LON
        )
    }

    var azimuth by remember { mutableFloatStateOf(0f) }

    // حسگر جهت: بردار چرخش (یا یدکی) با هموارسازی
    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        var smooth = 0f
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val reading: Float = if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    event.values[0]
                } else {
                    val r = FloatArray(9)
                    val o = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(r, event.values)
                    SensorManager.getOrientation(r, o)
                    Math.toDegrees(o[0].toDouble()).toFloat()
                }
                val norm = (reading + 360f) % 360f
                var delta = norm - smooth
                if (delta > 180f) delta -= 360f
                if (delta < -180f) delta += 360f
                smooth = (smooth + delta * 0.2f + 360f) % 360f
                azimuth = smooth
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensor?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        onDispose { sm.unregisterListener(listener) }
    }

    val diff = ((bearing - azimuth + 540.0) % 360.0) - 180.0
    val aligned = abs(diff) <= 4.0
    val accent = if (aligned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // سربرگ
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "قبله‌نما",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))

        // قطب‌نما
        Box(
            Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val ringInner = MaterialTheme.colorScheme.surfaceVariant
            val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
            Canvas(Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f - 6.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)

                // حلقه بیرونی
                drawCircle(
                    color = accent,
                    radius = r,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = ringInner,
                    radius = r - 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // درجه‌بندی: می‌چرخد تا شمال همیشه درست نشان داده شود
                for (a in 0 until 360 step 15) {
                    val major = a % 90 == 0
                    rotate(degrees = azimuth + a, pivot = center) {
                        drawLine(
                            color = if (major) tickColor else tickColor.copy(alpha = 0.45f),
                            start = Offset(center.x, center.y - r + 8.dp.toPx()),
                            end = Offset(
                                center.x,
                                center.y - r + (if (major) 22.dp.toPx() else 15.dp.toPx())
                            ),
                            strokeWidth = if (major) 2.5.dp.toPx() else 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // جهت‌های اصلی (می‌چرخند با عقربه شمال)
            Box(
                Modifier
                    .fillMaxSize()
                    .rotate(azimuth)
            ) {
                Text(
                    "شمال",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 26.dp)
                )
                Text(
                    "جنوب",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 26.dp)
                )
                Text(
                    "شرق",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
                Text(
                    "غرب",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                )
            }

            // فلش قبله (ثابت، نسبت به بالاى گوشی می‌چرخد)
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .rotate((bearing - azimuth).toFloat()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Navigation,
                    contentDescription = "جهت قبله",
                    tint = accent,
                    modifier = Modifier.size(52.dp)
                )
            }
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(Modifier.height(10.dp))

        // وضعیت هم‌ترازی
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (aligned) Color(0xFF2E7D32).copy(alpha = 0.14f)
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (aligned) "رو به قبله هستید" else "گوشی را بچرخانید تا فلش رو به بالا شود",
                style = MaterialTheme.typography.titleSmall,
                color = if (aligned) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        // انتخاب شهر
        Text(
            text = "شهر شما",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        CityPicker(
            selectedIndex = settings.cityIndex,
            onSelect = { index ->
                scope.launch {
                    vm.settingsStore.setCityIndex(index)
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("زاویه قبله از شمال", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${bearing.toInt().toPersianDigits()} درجه",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("شهر انتخابی", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "گوشی را به‌صورت افقی و دور از فلزات و میدان‌های مغناطیسی نگه دارید. جهت‌یابی با حسگر قطب‌نمای گوشی انجام می‌شود و چند درجه خطای مغناطیسی طبیعی است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}
