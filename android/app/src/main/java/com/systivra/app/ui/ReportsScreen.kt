package com.systivra.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systivra.app.data.DeviceSnapshot
import com.systivra.app.data.buildSessionJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Foreground-only CSV export through Android's Storage Access Framework.
 * No storage permissions, Internet access or automatic file writes.
 */
@Composable
fun ReportExportScreen(samples: List<DeviceSnapshot>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("No report exported yet.") }
    val snapshot = samples.toList()
    val csvDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri == null) {
            result = "Export canceled."
        } else {
            result = "Exporting CSV…"
            scope.launch {
                result = withContext(Dispatchers.IO) {
                    runCatching {
                        val csv = buildSessionCsv(snapshot)
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("The selected document cannot be opened.")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
                        "Exported " + snapshot.size + " CSV samples successfully."
                    }.getOrElse { "Export failed: " + (it.localizedMessage ?: "Unknown error") }
                }
            }
        }
    }
    val jsonDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) {
            result = "Export canceled."
        } else {
            result = "Exporting JSON…"
            scope.launch {
                result = withContext(Dispatchers.IO) {
                    runCatching {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("The selected document cannot be opened.")
                        output.bufferedWriter(Charsets.UTF_8).use {
                            it.write(buildSessionJson(snapshot))
                        }
                        "Exported " + snapshot.size + " JSON samples successfully."
                    }.getOrElse { "Export failed: " + (it.localizedMessage ?: "Unknown error") }
                }
            }
        }
    }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Session Report", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Saved recordings remain on this device unless you explicitly export them.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CSV Export", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Cyan)
                    Text("Available samples: " + snapshot.size)
                    Text("Includes UTC timestamp, battery charge, temperature, free RAM, and Android thermal state.", color = Muted, fontSize = 13.sp)
                    Button(
                        enabled = snapshot.isNotEmpty(),
                        onClick = { csvDocument.launch("SYSTIVRA-session.csv") }
                    ) { Text("Choose export location") }
                    Text(result, color = Muted, fontSize = 13.sp)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("JSON Export", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Cyan)
                    Text("Export timestamps, battery, memory, thermal and measured CPU load as structured JSON.", color = Muted, fontSize = 13.sp)
                    Button(
                        enabled = snapshot.isNotEmpty(),
                        onClick = { jsonDocument.launch("SYSTIVRA-session.json") }
                    ) { Text("Save JSON report") }
                }
            }
        }
        item {
            Text(
                "Saved readings persist privately in SQLite until deleted. Exports are copies written " +
                    "to a location you choose; they may contain sensitive device activity details. " +
                    "No file is uploaded automatically.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}

internal fun buildSessionCsv(samples: List<DeviceSnapshot>): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    fun csv(value: String): String {
        val protected = if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") ||
            value.startsWith("@")) "'" + value else value
        return "\"" + protected.replace("\"", "\"\"") + "\""
    }
    val header = "timestamp_utc,battery_percent,battery_temp_c,free_ram,thermal_status\n"
    return buildString {
        append(header)
        samples.forEach { snap ->
            val fields = listOf(
                format.format(Date(snap.time)),
                snap.battery.getOrNull(0)?.value.orEmpty(),
                snap.battery.getOrNull(2)?.value.orEmpty(),
                snap.ram.getOrNull(1)?.value.orEmpty(),
                snap.thermal.getOrNull(0)?.value.orEmpty()
            )
            append(fields.joinToString(",") { csv(it) })
            append('\n')
        }
    }
}
