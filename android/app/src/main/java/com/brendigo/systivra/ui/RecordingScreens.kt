// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.analyzeSessionReadings
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
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    if (confirmClear) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { confirmClear = false },
            title = { Text("Delete all recordings?") },
            text = { Text("All saved recording sessions and their samples will be permanently deleted from this device. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    enabled = ready && !active,
                    onClick = { confirmClear = false; if (ready && !active) clear() }
                ) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("recording-controls-page").verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Session Recorder", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(
                Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    if (active) "● Recording while app is visible" else "Paused / inactive",
                    color = if (active) Mint else Muted, fontWeight = FontWeight.Bold
                )
                Text("Saved samples: ${readings.size} / 900", color = Cyan)
                if (readings.isNotEmpty()) {
                    val span = (readings.last().time - readings.first().time).coerceAtLeast(0L) / 1000L
                    Text("Recorded span: ${span}s", color = Muted)
                }
                Button(
                    enabled = ready,
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Raised, disabledContentColor = Muted),
                    onClick = toggle
                ) { Text(if (active) "Stop recording" else if (ready) "Start recording" else "Loading saved history…") }
                TextButton(
                    enabled = ready && !active && readings.isNotEmpty(),
                    onClick = { confirmClear = true }
                ) { Text("Delete all saved recordings") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = readings.isNotEmpty(),
                onClick = { open("session-analysis") },
                modifier = Modifier.weight(1f)
            ) { Text("Analyze") }
            OutlinedButton(
                onClick = { open("recording-library") },
                modifier = Modifier.weight(1f)
            ) { Text("Library") }
        }
        OutlinedButton(
            enabled = readings.isNotEmpty(),
            onClick = { open("reports-export") }
        ) { Text("Export saved session") }
        if (storageError != null) Text(storageError, color = Cyan, fontSize = 12.sp)
        Text("Foreground only · Private SQLite · No automatic cloud backup.",
            color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun SessionAnalysisScreen(readings: List<DeviceSnapshot>, open: (String) -> Unit) {
    val insights = analyzeSessionReadings(readings)
    fun fmt(value: Double?, unit: String): String =
        value?.let { oneDecimal(it) + unit } ?: "Unavailable"
    val batteryRange = if (insights.batteryMinPercent != null && insights.batteryMaxPercent != null) {
        fmt(insights.batteryMinPercent, "%") + " – " + fmt(insights.batteryMaxPercent, "%")
    } else "Unavailable"
    val tempRange = if (insights.temperatureMinCelsius != null && insights.temperatureMaxCelsius != null) {
        fmt(insights.temperatureMinCelsius, " °C") + " – " + fmt(insights.temperatureMaxCelsius, " °C")
    } else "Unavailable"
    val measures = if (insights.sampleCount == 0) emptyList() else listOf(
        "Captured samples" to insights.sampleCount.toString(),
        "Duration" to (insights.elapsedMillis?.let { "${it / 1000L}s" } ?: "Unavailable"),
        "Battery range" to batteryRange,
        "Temperature range" to tempRange,
        "Average battery charge" to fmt(insights.batteryMeanPercent, "%"),
        "Thermal states" to if (insights.thermalStates.isEmpty()) "Unavailable"
            else insights.thermalStates.entries.joinToString(" · ") { "${it.key}: ${it.value}" }
    )
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("session-analysis-page")) {
        val pageSize = if (maxHeight < 570.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(measures.size, pageSize) - 1)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Session Analysis", fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text("Statistics from explicitly saved device samples.", color = Muted, fontSize = 13.sp)
            if (measures.isEmpty()) {
                SummaryCard("No session", "Start recording to collect real measurements.")
            } else {
                pageItems(measures, page, pageSize).forEach { (label, value) ->
                    SummaryCard(label, value)
                }
                PageControls(
                    total = measures.size, pageSize = pageSize, page = page,
                    onPageChange = { requestedPage = it }, tag = "session-analysis"
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { open("session-insights") }
                    ) { Text("Insights") }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { open("reports-export") }
                    ) { Text("Export") }
                }
            }
        }
    }
}

private fun oneDecimal(value: Double) = String.format(Locale.US, "%.1f", value)

/** Show only actual user-verified outcomes, never simulated diagnostic passes. */
internal fun filteredDiagnosticResults(
    results: List<Pair<String, String>>,
    statusFilter: String?
): List<Pair<String, String>> {
    return if (statusFilter == null) results else results.filter { (_, value) ->
        when (statusFilter) {
            "Passed" -> value.startsWith("Pass", ignoreCase = true)
            "Failed" -> value.startsWith("Fail", ignoreCase = true)
            "Skipped" -> value.startsWith("Skip", ignoreCase = true)
            "Unavailable" -> value.startsWith("Not assessed", ignoreCase = true) ||
                value.startsWith("Unavailable", ignoreCase = true) ||
                value.startsWith("Not implemented", ignoreCase = true)
            else -> false
        }
    }
}

@Composable
fun DiagnosticResultsScreen(statusFilter: String? = null) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("systivra_diagnostics", Context.MODE_PRIVATE)
    val results = listOf(
        "Display & touch test" to (prefs.getString("display_touch", "Not assessed") ?: "Not assessed"),
        "Vibration (user-confirmed)" to (prefs.getString("vibration", "Not assessed") ?: "Not assessed"),
        "Speaker test (user-confirmed)" to (prefs.getString("speaker", "Not assessed") ?: "Not assessed"),
        "Microphone / camera" to "Unavailable — access not requested"
    )
    val visible = filteredDiagnosticResults(results, statusFilter)
    var requestedPage by rememberSaveable(statusFilter) { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("diagnostic-results-page")) {
        val pageSize = if (maxHeight < 620.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(visible.size, pageSize) - 1)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Diagnostic Results" + (statusFilter?.let { " · $it" } ?: ""),
                fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("Only user-confirmed results appear. Unassessed tests are not passes.",
                color = Muted, fontSize = 13.sp)
            if (visible.isEmpty()) {
                SummaryCard("No ${statusFilter?.lowercase(Locale.US) ?: "recorded"} results",
                    "Run a test and confirm its outcome.")
            } else {
                pageItems(visible, page, pageSize).forEach { (label, value) ->
                    SummaryCard(label, value)
                }
                PageControls(
                    total = visible.size, pageSize = pageSize, page = page,
                    onPageChange = { requestedPage = it }, tag = "diagnostic-results"
                )
            }
            if (statusFilter == null) {
                Text("Sensor inventory is available in Hardware Explorer.", color = Muted, fontSize = 12.sp)
            }
        }
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
