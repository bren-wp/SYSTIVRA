// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.measuredChargePercentages
import com.brendigo.systivra.data.Reading

/**
 * The 390×844 SVGs in brending/screens are layout references, not device readings.
 * Data below the reference introduction always comes from Android. Nothing is fabricated.
 */
internal data class DetailReference(
    val title: String,
    val subtitle: String,
    val description: String,
    val rows: List<String>,
    val hasChart: Boolean = false
)

internal val detailReferences = mapOf(
    "cpu-details" to DetailReference("CPU Details", "Processor overview", "System information",
        listOf("Architecture", "Core count", "CPU load", "Data availability"), true),
    "gpu-details" to DetailReference("GPU Details", "Graphics processor", "OpenGL and Vulkan data",
        listOf("Renderer", "Graphics API", "GPU load", "Availability")),
    "memory-details" to DetailReference("Memory Details", "System memory", "Used and available RAM",
        listOf("Total RAM", "Available RAM", "Memory pressure", "History"), true),
    "storage-details" to DetailReference("Storage Details", "Internal storage", "Space and volumes",
        listOf("Total storage", "Available space", "Used space", "Volume details")),
    "battery-details" to DetailReference("Battery Details", "Power information", "Charge and temperature",
        listOf("Charge level", "Charging state", "Battery temperature", "Current"), true),
    "thermal-details" to DetailReference("Thermal Details", "Thermal status", "Android platform reporting",
        listOf("Thermal status", "Headroom if available", "Battery temperature", "Safety guidance"), true),
    "display-details" to DetailReference("Display Details", "Screen information", "Resolution and density",
        listOf("Resolution", "Pixel density", "Refresh rate", "Color modes")),
    "device-and-android" to DetailReference("Device & Android", "System profile", "Platform identifiers",
        listOf("Manufacturer", "Model", "Android version", "Security patch")),
    "sensor-explorer" to DetailReference("Sensor Explorer", "Available sensors", "Device-specific inventory",
        listOf("Accelerometer", "Gyroscope", "Light sensor", "Proximity"))
)

private fun measuredPercentages(route: String, samples: List<DeviceSnapshot>): List<Float> {
    if (route == "memory-details") return samples.mapNotNull(::measuredMemoryUsage)
    if (route == "battery-details") return samples.measuredChargePercentages()
    if (route != "cpu-details") return emptyList()
    return samples.mapNotNull { sample ->
        sample.cpu.firstOrNull { it.name == "System-wide CPU load" }
            ?.value?.trim()?.removeSuffix("%")?.trim()?.toFloatOrNull()
            ?.takeIf { it.isFinite() && it in 0f..100f }
    }
}

@Composable
internal fun ReferenceDetailScreen(
    route: String,
    readings: List<Reading>,
    samples: List<DeviceSnapshot>,
    open: (String) -> Unit
) {
    val spec = detailReferences[route] ?: return
    val measured = measuredPercentages(route, samples)
    var selectedLabel by rememberSaveable(route) { mutableStateOf<String?>(null) }
    val selected = selectedLabel?.let { metricDetailForRow(route, it, readings) }
    val context = LocalContext.current
    val fahrenheit = remember(context) { preferredFahrenheit(context) }

    if (selectedLabel != null) {
        val reading = selected
        ReferenceQuickResultDialog(
            testTag = "selected-metric-$route",
            title = selectedLabel.orEmpty(),
            value = reading?.let { displayReading(it, fahrenheit) } ?: "Unavailable",
            source = reading?.source,
            onDismiss = { selectedLabel = null },
            onCopy = reading?.takeIf { !it.value.isNullOrBlank() }?.let { valid ->
                { copyMeasurement(context, valid) }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().testTag("reference-detail-$route").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading(spec.title, spec.subtitle, spec.description)
        if (spec.hasChart) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .testTag("open-readings-$route")
                    .clickable { open("$route-readings") }
            ) { ReferenceMeasuredChart(measured) }
        } else {
            ReferenceHeroCard { open("$route-readings") }
        }
        Spacer(Modifier.height(if (spec.hasChart) 1.dp else 0.dp))
        spec.rows.forEachIndexed { index, label ->
            ReferenceMetricRow(label, listOf("◇", "▤", "◉", "⌁")[index], index) {
                if (route == "memory-details" && label == "History") {
                    open("history")
                } else {
                    selectedLabel = label
                }
            }
        }
    }
}


/** A dedicated searchable screen; reference overview pages no longer duplicate
 * every reading below a 116dp spacer. Readings come from Android only. */
internal fun copyMeasurement(context: Context, reading: Reading) {
    val available = reading.value?.takeIf(String::isNotBlank) ?: return
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(
            ClipData.newPlainText("SYSTIVRA device measurement", "${reading.name}: $available")
        )
    }
}

/**
 * All reference chart boards use one 350x170dp source rectangle. Place the
 * guides at their actual SVG offsets (y=78/108/138 from card top) rather than
 * deriving y from a variable-height Compose text column. This also keeps the
 * chart stable when user-visible measurement strings change.
 *
 * The SVG zigzag is deliberately NOT drawn: it is not a real device reading.
 * Only genuine finite, valid Android sample percentages become plotted points.
 */
@Composable
internal fun ReferenceMeasuredChart(points: List<Float>, footer: String? = null) {
    val valid = points.filter { it.isFinite() && it in 0f..100f }
    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp).testTag("reference-measured-chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Color(0xFF233857))
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.matchParentSize().testTag("reference-chart-plot")) {
                val startX = 20.dp.toPx()
                val endX = size.width - 20.dp.toPx()
                val topY = 31.dp.toPx()
                val bottomY = 138.dp.toPx()
                val guides = listOf(78.dp.toPx(), 108.dp.toPx(), 138.dp.toPx())
                val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(4.dp.toPx(), 7.dp.toPx()), 0f
                )
                if (endX > startX) {
                    guides.forEach { y ->
                        drawLine(
                            color = Color(0xFF24425C),
                            start = Offset(startX, y),
                            end = Offset(endX, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dash
                        )
                    }
                    if (valid.size >= 2) {
                        val dx = (endX - startX) / (valid.size - 1).toFloat()
                        valid.zipWithNext().forEachIndexed { index, pair ->
                            val y0 = bottomY - pair.first / 100f * (bottomY - topY)
                            val y1 = bottomY - pair.second / 100f * (bottomY - topY)
                            drawLine(
                                color = ReferenceChartLine,
                                start = Offset(startX + dx * index, y0),
                                end = Offset(startX + dx * (index + 1), y1),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
            Text(
                "Live data chart",
                modifier = Modifier.offset(x = 18.dp, y = 14.dp),
                fontSize = 13.sp, color = Color(0xFFA8BED2)
            )
            if (valid.size < 2) {
                Text(
                    "Waiting for measured samples",
                    modifier = Modifier.align(Alignment.Center),
                    color = Muted, fontSize = 12.sp
                )
            }
            Text(
                footer ?: if (valid.size >= 2)
                    "Recorded device samples · measured percentages"
                else
                    "No illustrative or fabricated readings",
                modifier = Modifier.offset(x = 18.dp, y = 142.dp),
                fontSize = 11.sp, color = Color(0xFF8DAAC1)
            )
        }
    }
}
