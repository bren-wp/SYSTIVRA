package com.systivra.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systivra.app.data.DeviceSnapshot

@Composable
fun SystemHealth(snapshot: DeviceSnapshot, open: (String) -> Unit) {
    val sections = listOf(
        Triple("Battery", snapshot.battery, "battery-details"),
        Triple("Memory", snapshot.ram, "memory-details"),
        Triple("Thermal", snapshot.thermal, "thermal-details"),
        Triple("Storage", snapshot.storage, "storage-details"),
        Triple("CPU", snapshot.cpu, "cpu-details"),
        Triple("GPU", snapshot.gpu, "gpu-details")
    )
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("System Health", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Coverage of real Android measurements. No invented health score.", color = Muted, fontSize = 13.sp)
        }
        items(sections.size) { index ->
            val (title, readings, path) = sections[index]
            val available = readings.count { it.value != null }
            Card(
                onClick = { open(path) },
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold, color = Cyan)
                    Text(available.toString() + " of " + readings.size + " metrics available")
                    Text("Inspect source and limitations ›", fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}

@Composable
fun PrivacyCenter() {
    val items = listOf(
        "Account" to "No login or registration required.",
        "Network" to "The app has no INTERNET permission or analytics SDK.",
        "Device information" to "Measurements are read locally through Android system APIs.",
        "Storage" to "Current session history stays in memory. CSV export requires your explicit file choice.",
        "Camera and microphone" to "Not requested in this version.",
        "Background monitoring" to "Sampling stops when the app is no longer in the foreground."
    )
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Privacy by design", color = Mint, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        items(items.size) { index ->
            val (title, details) = items[index]
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(details, color = Muted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CpuCoreInspector(snapshot: DeviceSnapshot) {
    val count = snapshot.cpu.firstOrNull { it.name == "Logical processors" }?.value
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("CPU Core Inspector", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("Android exposes a processor count but does not guarantee per-core load or clocks.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Logical processors", fontWeight = FontWeight.SemiBold)
                    Text(count ?: "Unavailable", fontSize = 29.sp, color = Cyan)
                    Text("Per-core frequency and utilization: Unavailable until independently verified.", color = Muted)
                }
            }
        }
    }
}

@Composable
fun ChargingAnalysis(snapshot: DeviceSnapshot, samples: List<DeviceSnapshot>) {
    val battery = snapshot.battery
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Charging Analysis", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item { Text("Measured battery values only. A charge capacity estimate is not available in this version.", color = Muted) }
        items(battery.size) { index ->
            val reading = battery[index]
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(reading.name, color = Muted, fontSize = 13.sp)
                    Text(reading.display, fontSize = 23.sp, color = Cyan, fontWeight = FontWeight.Bold)
                    Text("Source: " + reading.source, color = Muted, fontSize = 11.sp)
                }
            }
        }
        item { Text("Foreground samples captured: " + samples.size, color = Muted, fontSize = 12.sp) }
    }
}

@Composable
fun FullscreenMetric(samples: List<DeviceSnapshot>) {
    val values = samples.mapNotNull {
        it.battery.firstOrNull()?.value?.removeSuffix("%")?.toFloatOrNull()
    }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Battery Charge · Live", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("System-reported percentage. Graph uses actual session samples.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(values.lastOrNull()?.let { String.format(java.util.Locale.US, "%.1f%%", it) } ?: "Unavailable",
                        color = Mint, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    if (values.size > 1) {
                        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
                            val dx = size.width / (values.size - 1)
                            values.zipWithNext().forEachIndexed { index, pair ->
                                drawLine(Mint,
                                    Offset(index * dx, size.height * (1f - pair.first / 100f)),
                                    Offset((index + 1) * dx, size.height * (1f - pair.second / 100f)),
                                    strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    } else Text("More readings are needed to draw a chart.", color = Muted)
                    Text("Minimum: " + (values.minOrNull()?.toString() ?: "—") +
                        "% · Maximum: " + (values.maxOrNull()?.toString() ?: "—") + "%", color = Muted)
                }
            }
        }
    }
}
