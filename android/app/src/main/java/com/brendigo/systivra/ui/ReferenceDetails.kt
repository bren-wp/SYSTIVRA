package com.brendigo.systivra.ui

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.brendigo.systivra.data.Reading
import kotlinx.coroutines.launch

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
    val readings = when (route) {
        "cpu-details" -> samples.map { it.cpu.firstOrNull { r -> r.name == "System-wide CPU load" } }
        "battery-details" -> samples.map { it.battery.firstOrNull { r -> r.name == "Charge level" } }
        else -> emptyList()
    }
    return readings.mapNotNull { item ->
        item?.value?.trim()?.removeSuffix("%")?.trim()?.toFloatOrNull()
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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val measured = measuredPercentages(route, samples)
    LazyColumn(
        state = listState,
        modifier = Modifier.testTag("reference-detail-$route"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(spec.title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text(spec.subtitle, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(spec.description, fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(18.dp))
            }
        }
        item {
            if (spec.hasChart) ReferenceMeasuredChart(measured)
            else ReferenceHeroCard { open(if (route == "device-and-android") "system-overview" else "device-and-android") }
        }
        item { Spacer(Modifier.height(7.dp)) }
        items(spec.rows.size) { index ->
            ReferenceMetricRow(spec.rows[index], listOf("◇", "▤", "◉", "⌁")[index], index) {
                // Each reference row is an actual action: show the complete device report below.
                scope.launch { listState.animateScrollToItem(8) }
            }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item { Text("Android device readings", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        if (readings.isEmpty()) {
            item { Text("Waiting for available device readings", color = Muted) }
        }
        items(readings) { reading ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Color(0xFF233857))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(reading.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(reading.display, fontSize = 16.sp,
                        color = if (reading.value == null) Muted else Cyan)
                    Text("Source: ${reading.source}", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

/** Never draw a decorative trend that could be mistaken for actual measurements. */
@Composable
private fun ReferenceMeasuredChart(points: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Color(0xFF233857))
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Live data chart", fontSize = 13.sp, color = Muted)
            if (points.size >= 2) {
                Canvas(Modifier.fillMaxWidth().height(96.dp)) {
                    val gap = size.width / (points.size - 1).toFloat()
                    points.zipWithNext().forEachIndexed { i, pair ->
                        drawLine(
                            color = Mint,
                            start = Offset(gap * i, size.height * (1f - pair.first / 100f)),
                            end = Offset(gap * (i + 1), size.height * (1f - pair.second / 100f)),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                Text("Recorded device samples · measured percentages", fontSize = 11.sp, color = Muted)
            } else {
                Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                    Text("Waiting for measured samples", color = Muted, fontSize = 12.sp)
                }
                Text("No illustrative or fabricated readings", fontSize = 11.sp, color = Muted)
            }
        }
    }
}
