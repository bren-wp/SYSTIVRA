// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.measuredChargePercentages

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
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("system-health-page")) {
        val perPage = if (maxHeight < 570.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(sections.size, perPage) - 1)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("System Health", fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Actual Android measurements. No artificial health score.", color = Muted, fontSize = 13.sp)
            pageItems(sections, page, perPage).forEach { (title, readings, destination) ->
                Card(
                    onClick = { open(destination) },
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(title, color = Cyan, fontWeight = FontWeight.Bold)
                        Text("${readings.count { it.value != null }} of ${readings.size} metrics available")
                        Text("Inspect sources and limitations", fontSize = 12.sp, color = Muted)
                    }
                }
            }
            PageControls(sections.size, perPage, page, { requestedPage = it }, "system-health")
        }
    }
}

@Composable
fun PrivacyCenter() {
    val disclosures = listOf(
        "Account" to "No login or registration required.",
        "Network" to "No INTERNET permission or analytics SDK.",
        "Device information" to "Measurements are read locally from Android APIs.",
        "Storage" to "Opt-in local SQLite recordings. Up to 900 samples. Exports use Android's file chooser.",
        "Camera and microphone" to "Not requested by this build.",
        "Background monitoring" to "Sampling stops when SYSTIVRA leaves the foreground."
    )
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("privacy-center-page")) {
        val perPage = if (maxHeight < 570.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(disclosures.size, perPage) - 1)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Privacy by design", color = Mint, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            pageItems(disclosures, page, perPage).forEach { (title, detail) ->
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(detail, color = Muted, fontSize = 13.sp)
                    }
                }
            }
            PageControls(disclosures.size, perPage, page, { requestedPage = it }, "privacy-center")
        }
    }
}

@Composable
fun CpuCoreInspector(snapshot: DeviceSnapshot) {
    val count = snapshot.cpu.firstOrNull { it.name == "Logical processors" }?.value
    Column(
        modifier = Modifier.fillMaxSize().testTag("cpu-core-page").verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CPU Core Inspector", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Android exposes a processor count, not guaranteed per-core load or clocks.", color = Muted)
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Logical processors", fontWeight = FontWeight.SemiBold)
                Text(count ?: "Unavailable", fontSize = 29.sp, color = Cyan)
                Text("Per-core frequency and utilization are unavailable until verified.", color = Muted)
            }
        }
    }
}

@Composable
fun ChargingAnalysis(snapshot: DeviceSnapshot, samples: List<DeviceSnapshot>) {
    val context = LocalContext.current
    val fahrenheit = remember(context) { preferredFahrenheit(context) }
    val battery = snapshot.battery
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("charging-readings-page")) {
        val perPage = if (maxHeight < 570.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(battery.size, perPage) - 1)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Charging Analysis", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Real Android battery readings. No fabricated capacity estimate.", color = Muted)
            pageItems(battery, page, perPage).forEach { reading ->
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(reading.name, color = Muted, fontSize = 13.sp)
                        Text(displayReading(reading, fahrenheit), fontSize = 22.sp, color = Cyan, fontWeight = FontWeight.Bold)
                        Text("Source: ${reading.source}", color = Muted, fontSize = 11.sp)
                    }
                }
            }
            if (battery.isEmpty()) Text("No battery metrics available from Android.", color = Muted)
            PageControls(battery.size, perPage, page, { requestedPage = it }, "charging-readings")
            Text("Foreground samples captured: ${samples.size}", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
fun FullscreenMetric(samples: List<DeviceSnapshot>) {
    val values = samples.measuredChargePercentages()
    Column(
        modifier = Modifier.fillMaxSize().testTag("fullscreen-battery-page").verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("Battery Charge · Live", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("System-reported percentage. Graph uses actual session samples.", color = Muted)
        }
        Column {
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
                    Text(
                        "Minimum: " + (values.minOrNull()?.let { "$it%" } ?: "Unavailable") +
                            " · Maximum: " + (values.maxOrNull()?.let { "$it%" } ?: "Unavailable"),
                        color = Muted
                    )
                }
            }
        }
    }
}
