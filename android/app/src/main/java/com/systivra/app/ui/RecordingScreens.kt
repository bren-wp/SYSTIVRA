package com.systivra.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systivra.app.data.DeviceSnapshot
import java.util.Locale

/**
 * User-initiated foreground capture backed by a private local SQLite store.
 * MainActivity only samples in STARTED lifecycle and retains at most 900 readings.
 */
@Composable
fun RecordingSessionScreen(
    readings: List<DeviceSnapshot>,
    active: Boolean,
    ready: Boolean,
    storageError: String?,
    toggle: () -> Unit,
    clear: () -> Unit,
    open: (String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Session Recorder", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Foreground-only sampling. Your saved samples remain in private local storage across app restarts until you clear them.",
                color = Muted, fontSize = 13.sp)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (active) "● Recording while app is visible" else "Paused / inactive",
                        color = if (active) Mint else Muted, fontWeight = FontWeight.Bold)
                    Text("Stored samples: " + readings.size + " / 900", fontSize = 20.sp, color = Cyan)
                    if (readings.isNotEmpty()) {
                        val elapsed = (readings.last().time - readings.first().time).coerceAtLeast(0L)
                        Text("Recorded span: " + (elapsed / 1000L).toString() + " seconds", color = Muted)
                    }
                    Button(enabled = ready, onClick = toggle) { Text(if (active) "Stop recording" else if (ready) "Start recording" else "Loading saved history…") }
                    Button(enabled = ready && !active && readings.isNotEmpty(), onClick = clear) { Text("Clear recorded samples") }
                }
            }
        }
        item { Button(enabled = readings.isNotEmpty(), onClick = { open("session-analysis") }) { Text("Analyze recorded samples") } }
        item { Button(onClick = { open("recording-library") }) { Text("Open Recording Library") } }
        item { Button(enabled = readings.isNotEmpty(), onClick = { open("reports-export") }) { Text("Export current session") } }
        item { Text("Sampling pauses when SYSTIVRA leaves the foreground. At most 900 entries are retained locally. Database backup is disabled.", color = Muted, fontSize = 12.sp) }
        if (storageError != null) item { Text(storageError, color = Cyan, fontSize = 12.sp) }
    }
}

@Composable
fun SessionAnalysisScreen(readings: List<DeviceSnapshot>, open: (String) -> Unit) {
    val charge = readings.mapNotNull { it.battery.firstOrNull()?.value?.removeSuffix("%")?.toDoubleOrNull() }
    val batteryTemp = readings.mapNotNull { it.battery.getOrNull(2)?.value?.removeSuffix(" °C")?.toDoubleOrNull() }
    val status = readings.mapNotNull { it.thermal.firstOrNull()?.value }.groupingBy { it }.eachCount()
    val start = readings.firstOrNull()?.time
    val end = readings.lastOrNull()?.time
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Session Analysis", fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text("Derived only from samples saved by an explicit foreground recording.", color = Muted)
        }
        if (readings.isEmpty()) {
            item { SummaryCard("No session", "Start a recording from the Monitor screen to collect readings.") }
        } else {
            item { SummaryCard("Captured samples", readings.size.toString()) }
            item { SummaryCard("Duration", ((end!! - start!!).coerceAtLeast(0L) / 1000L).toString() + " s") }
            item { SummaryCard("Battery range", range(charge, "%")) }
            item { SummaryCard("Battery temperature range", range(batteryTemp, " °C")) }
            item { SummaryCard("Average battery charge", if (charge.isNotEmpty()) oneDecimal(charge.average()) + "%" else "Unavailable") }
            item { SummaryCard("Thermal states", if (status.isEmpty()) "Unavailable" else
                status.entries.joinToString("\n") { it.key + ": " + it.value.toString() }) }
            item { Button(onClick = { open("session-insights") }) { Text("Detailed session insights") } }
            item { Button(onClick = { open("reports-export") }) { Text("Export CSV or JSON") } }
        }
    }
}

private fun range(values: List<Double>, unit: String): String =
    if (values.isEmpty()) "Unavailable"
    else oneDecimal(values.minOrNull()!!) + unit + " – " + oneDecimal(values.maxOrNull()!!) + unit

private fun oneDecimal(value: Double) = String.format(Locale.US, "%.1f", value)

@Composable
fun DiagnosticResultsScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("systivra_diagnostics", Context.MODE_PRIVATE)
    val result = prefs.getString("display_touch", "Not assessed") ?: "Not assessed"
    val vibration = prefs.getString("vibration", "Not assessed") ?: "Not assessed"
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Diagnostic Results", fontSize = 23.sp, fontWeight = FontWeight.Bold) }
        item { Text("Only user-confirmed test results appear. No fabricated health scores.", color = Muted) }
        item { SummaryCard("Display & touch test", result) }
        item { SummaryCard("Vibration (user-confirmed)", vibration) }
        item { SummaryCard("Speaker / microphone / camera", "Not implemented — no result") }
        item { SummaryCard("Sensor inventory", "Available in the Hardware Explorer; not an automatic pass/fail test") }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Muted, fontSize = 13.sp)
            Text(value, color = Cyan, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
        }
    }
}
