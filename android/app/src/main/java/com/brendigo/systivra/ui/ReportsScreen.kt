// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.buildSessionJson
import com.brendigo.systivra.data.chargeLevelText
import com.brendigo.systivra.data.batteryTemperatureText
import com.brendigo.systivra.data.availableRamText
import com.brendigo.systivra.data.thermalStatusText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Foreground-only CSV export through Android's Storage Access Framework.
 * No storage permissions, Internet access or automatic file writes.
 */
@Composable
fun ReportExportScreen(
    samples: List<DeviceSnapshot>,
    preferredFormat: String? = null,
    onStartRecording: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val formats = listOf("csv", "json", "pdf", "share")
    var selectedFormat by rememberSaveable {
        mutableStateOf(preferredFormat?.takeIf { it in formats } ?: "csv")
    }
    LaunchedEffect(preferredFormat) {
        preferredFormat?.takeIf { it in formats }?.let { selectedFormat = it }
    }
    var csvResult by remember { mutableStateOf("No report exported yet.") }
    var jsonResult by remember { mutableStateOf("") }
    var pdfResult by remember { mutableStateOf("") }
    var shareResult by remember { mutableStateOf("") }
    val snapshot = samples.toList()
    val csvPending = remember { PendingReportExport() }
    val jsonPending = remember { PendingReportExport() }
    val pdfPending = remember { PendingReportExport() }
    val csvDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val captured = csvPending.consume()
        if (uri == null) {
            csvResult = "Export canceled."
        } else if (captured.isNullOrEmpty()) {
            csvResult = "Export request expired. Choose CSV export again."
        } else {
            csvResult = "Exporting CSV…"
            scope.launch {
                csvResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val csv = buildSessionCsv(captured)
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("The selected document cannot be opened.")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
                        "Exported " + captured.size + " CSV samples successfully."
                    }.getOrElse { "Export failed. Check the selected document location and try again." }
                }
            }
        }
    }
    val jsonDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val captured = jsonPending.consume()
        if (uri == null) {
            jsonResult = "Export canceled."
        } else if (captured.isNullOrEmpty()) {
            jsonResult = "Export request expired. Choose JSON export again."
        } else {
            jsonResult = "Exporting JSON…"
            scope.launch {
                jsonResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("The selected document cannot be opened.")
                        output.bufferedWriter(Charsets.UTF_8).use {
                            it.write(buildSessionJson(captured))
                        }
                        "Exported " + captured.size + " JSON samples successfully."
                    }.getOrElse { "Export failed. Check the selected document location and try again." }
                }
            }
        }
    }
    val pdfDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        val captured = pdfPending.consume()
        if (uri == null) {
            pdfResult = "PDF export canceled."
        } else if (captured.isNullOrEmpty()) {
            pdfResult = "Export request expired. Choose PDF export again."
        } else {
            pdfResult = "Exporting PDF…"
            scope.launch {
                pdfResult = withContext(Dispatchers.IO) {
                    runCatching {
                        val document = buildSessionPdf(captured)
                        try {
                            val output = context.contentResolver.openOutputStream(uri)
                                ?: error("The selected document cannot be opened.")
                            output.use { document.writeTo(it) }
                        } finally {
                            document.close()
                        }
                        "Exported " + captured.size + " readings as PDF."
                    }.getOrElse { "PDF export failed. Check available storage and try again." }
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("report-export-focused-screen").padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            Text("Session Report", fontWeight = FontWeight.Bold, fontSize = 25.sp)
            Text("Choose one format. Only the selected export action is shown.", color = Muted)
        }
        Column {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("report-format-selector"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                formats.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { format ->
                            FilterChip(
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format },
                                modifier = Modifier.weight(1f).testTag("report-format-tab-$format"),
                                label = {
                                    Text(
                                        when (format) {
                                            "csv" -> "CSV"
                                            "json" -> "JSON"
                                            "pdf" -> "PDF"
                                            else -> "Share"
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Panel,
                                    labelColor = TextPrimary,
                                    selectedContainerColor = Color(0xFF153B68),
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
        if (snapshot.isEmpty()) {
            Column {
                // Keep the chosen action visible without vertical scrolling on
                // compact phones. Empty-state guidance occupies only one row.
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("report-empty-state"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "No recorded samples",
                        modifier = Modifier.weight(1f),
                        color = Muted,
                        fontSize = 13.sp
                    )
                    if (onStartRecording != null) {
                        TextButton(
                            modifier = Modifier.testTag("report-start-recording"),
                            onClick = onStartRecording
                        ) { Text("Open Recording") }
                    }
                }
            }
        }
        if (selectedFormat == "csv") Column {
            Card(
                modifier = Modifier.testTag("report-export-format-csv"),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CSV Export", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Cyan)
                    Text("Available samples: " + snapshot.size)
                    Text("Includes UTC timestamp, battery charge, temperature, free RAM, and Android thermal state.", color = Muted, fontSize = 13.sp)
                    Button(
                        enabled = snapshot.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Raised,
                            disabledContentColor = Muted
                        ),
                        onClick = {
                            csvPending.prepare(snapshot)
                            csvDocument.launch("SYSTIVRA-session.csv")
                        }
                    ) { Text("Choose export location") }
                    Text(csvResult, color = Muted, fontSize = 13.sp)
                }
            }
        }
        if (selectedFormat == "json") Column {
            Card(
                modifier = Modifier.testTag("report-export-format-json"),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("JSON Export", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Cyan)
                    Text("Export timestamps, battery, memory, thermal and measured CPU load as structured JSON.", color = Muted, fontSize = 13.sp)
                    Button(
                        enabled = snapshot.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Raised,
                            disabledContentColor = Muted
                        ),
                        onClick = {
                            jsonPending.prepare(snapshot)
                            jsonDocument.launch("SYSTIVRA-session.json")
                        }
                    ) { Text("Save JSON report") }
                    if (jsonResult.isNotEmpty()) Text(jsonResult, color = Muted, fontSize = 13.sp)
                }
            }
        }
        if (selectedFormat == "pdf") Column {
            Card(
                modifier = Modifier.testTag("report-export-format-pdf"),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PDF Export", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Cyan)
                    Text("Generate a multipage PDF from measured UTC timestamps, charge, temperature, RAM and thermal state.", color = Muted, fontSize = 13.sp)
                    Button(
                        enabled = snapshot.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Raised,
                            disabledContentColor = Muted
                        ),
                        onClick = {
                            pdfPending.prepare(snapshot)
                            pdfDocument.launch("SYSTIVRA-session.pdf")
                        }
                    ) { Text("Create PDF document") }
                    if (pdfResult.isNotEmpty()) Text(pdfResult, color = Muted, fontSize = 13.sp)
                }
            }
        }
        if (selectedFormat == "share") Column {
            Card(
                modifier = Modifier.testTag("report-export-format-share"),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Android Sharesheet", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Cyan)
                    Text("Share the recorded CSV as text only after choosing the receiving app. No background uploads.", color = Muted, fontSize = 13.sp)
                    Button(enabled = snapshot.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Raised,
                            disabledContentColor = Muted
                        ), onClick = {
                        shareResult = runCatching {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "SYSTIVRA recording")
                                putExtra(Intent.EXTRA_TEXT, buildSessionCsv(snapshot))
                            }
                            context.startActivity(Intent.createChooser(share, "Share SYSTIVRA readings"))
                            "Android Sharesheet opened. You control whether any data is sent."
                        }.getOrElse {
                            "Could not open the Android Sharesheet. Try again or use a file export."
                        }
                    }) { Text("Open Android Sharesheet") }
                    if (shareResult.isNotEmpty()) Text(shareResult, color = Muted, fontSize = 13.sp)
                }
            }
        }
        Column {
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
        // Some spreadsheet importers ignore BOM, zero-width and control prefixes.
        // Preserve the source reading, but force dangerous formulas to be plain text.
        // Normalize all line/control separators inside source readings:
        // quoted CSV cells must not create extra physical spreadsheet rows.
        val flattened = value.map { ch ->
            if (ch.isISOControl() || ch == '\u2028' || ch == '\u2029') ' ' else ch
        }.joinToString("")
        val visible = flattened.dropWhile { ch ->
            ch.isWhitespace() || ch == '\uFEFF' || ch == '\u200B' ||
                ch == '\u200C' || ch == '\u200D' || ch == '\u2060' ||
                ch == '\u200E' || ch == '\u200F' ||
                ch in '\u202A'..'\u202E' || ch in '\u2066'..'\u2069'
        }
        // Strip ignorable prefixes before a dangerous formula, so an apostrophe
        // is immediately visible to spreadsheet importers. Benign values stay intact.
        val firstVisible = visible.firstOrNull()
        val protected = if (firstVisible == '=' || firstVisible == '+' ||
            firstVisible == '-' || firstVisible == '@'
        ) "'$visible" else flattened
        return "\"" + protected.replace("\"", "\"\"") + "\""
    }
    val header = "timestamp_utc,battery_percent,battery_temp_c,free_ram,thermal_status\n"
    return buildString {
        append(header)
        samples.forEach { snap ->
            val fields = listOf(
                format.format(Date(snap.time)),
                snap.chargeLevelText().orEmpty(),
                snap.batteryTemperatureText().orEmpty(),
                snap.availableRamText().orEmpty(),
                snap.thermalStatusText().orEmpty()
            )
            append(fields.joinToString(",") { csv(it) })
            append('\n')
        }
    }
}
