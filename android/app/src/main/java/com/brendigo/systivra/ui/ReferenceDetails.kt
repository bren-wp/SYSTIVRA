package com.brendigo.systivra.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

    LazyColumn(
        modifier = Modifier.testTag("reference-detail-$route"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading(spec.title, spec.subtitle, spec.description) }
        item {
            if (spec.hasChart) ReferenceMeasuredChart(measured)
            else ReferenceHeroCard { open("$route-readings") }
        }
        item { Spacer(Modifier.height(0.dp)) }
        items(spec.rows.size) { index ->
            ReferenceMetricRow(spec.rows[index], listOf("◇", "▤", "◉", "⌁")[index], index) {
                val label = spec.rows[index]
                if (route == "memory-details" && label == "History") {
                    open("history")
                } else {
                    selectedLabel = label
                }
            }
        }
        item {
            TextButton(
                onClick = { open("$route-readings") },
                modifier = Modifier.fillMaxWidth().testTag("open-readings-$route")
            ) { Text("All device readings") }
        }
    }
}


/** A dedicated searchable screen; reference overview pages no longer duplicate
 * every reading below a 116dp spacer. Readings come from Android only. */
@Composable
internal fun DeviceReadingsBrowser(route: String, readings: List<Reading>) {
    val spec = detailReferences[route] ?: return
    val context = LocalContext.current
    val fahrenheit = remember(context) { preferredFahrenheit(context) }
    var searchQuery by rememberSaveable(route) { mutableStateOf("") }
    val filtered = filterDeviceReadings(readings, searchQuery)
    LazyColumn(
        modifier = Modifier.testTag("readings-browser-$route"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("${spec.title} readings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "${readings.count { !it.value.isNullOrBlank() }} of ${readings.size} Android measurements available",
                color = Muted
            )
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("metric-search-$route"),
                label = { Text("Search readings") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = Muted,
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Muted,
                    cursorColor = Cyan,
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel
                )
            )
        }
        if (filtered.isEmpty()) {
            item {
                Text(
                    if (readings.isEmpty()) "No readings are available from Android"
                    else "No readings match your search",
                    color = Muted
                )
            }
        }
        items(filtered) { reading ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Color(0xFF233857))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(reading.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        displayReading(reading, fahrenheit),
                        color = if (reading.value.isNullOrBlank()) Muted else Cyan
                    )
                    Text("Source: ${reading.source}", color = Muted)
                    if (!reading.value.isNullOrBlank()) {
                        TextButton(onClick = { copyMeasurement(context, reading) }) {
                            Text("Copy measurement")
                        }
                    }
                }
            }
        }
    }
}

private fun copyMeasurement(context: Context, reading: Reading) {
    val available = reading.value?.takeIf(String::isNotBlank) ?: return
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(
            ClipData.newPlainText("SYSTIVRA device measurement", "${reading.name}: $available")
        )
    }
}

/** Never draw a decorative trend that could be mistaken for actual measurements. */
@Composable
internal fun ReferenceMeasuredChart(points: List<Float>) {
    val valid = points.filter { it.isFinite() && it in 0f..100f }
    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp).testTag("reference-measured-chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Color(0xFF233857))
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Live data chart", fontSize = 13.sp, color = Muted)
            Box(Modifier.fillMaxWidth().height(78.dp)) {
                Canvas(Modifier.matchParentSize().testTag("reference-chart-plot")) {
                    // The source SVG has 4/7px dashed guides. They are
                    // decorative gridlines, NOT invented device samples.
                    val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(4.dp.toPx(), 7.dp.toPx()), 0f
                    )
                    for (fraction in listOf(0.25f, 0.60f, 0.95f)) {
                        val y = size.height * fraction
                        drawLine(
                            color = Color(0xFF24425C),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dash
                        )
                    }
                    if (valid.size >= 2) {
                        val dx = size.width / (valid.size - 1).toFloat()
                        valid.zipWithNext().forEachIndexed { i, pair ->
                            drawLine(
                                color = ReferenceChartLine,
                                start = Offset(dx * i, size.height * (1f - pair.first / 100f)),
                                end = Offset(dx * (i + 1), size.height * (1f - pair.second / 100f)),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
                if (valid.size < 2) {
                    Text(
                        "Waiting for measured samples",
                        modifier = Modifier.align(Alignment.Center),
                        color = Muted, fontSize = 12.sp
                    )
                }
            }
            Text(
                if (valid.size >= 2)
                    "Recorded device samples · measured percentages"
                else
                    "No illustrative or fabricated readings",
                fontSize = 11.sp, color = Muted
            )
        }
    }
}
