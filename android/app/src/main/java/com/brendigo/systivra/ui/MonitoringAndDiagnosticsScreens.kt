// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.measuredChargePercentages
import com.brendigo.systivra.data.Reading

@Composable
internal fun MetricScreen(readings: List<Reading>, subtitle: String) {
    val context = LocalContext.current
    val fahrenheit = remember(context) { preferredFahrenheit(context) }
    var requestedPage by rememberSaveable(subtitle) { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("metric-reading-page")) {
        val perPage = if (maxHeight < 530.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(readings.size, perPage) - 1)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(subtitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (readings.isEmpty()) Text("Waiting for device readings…", color = Muted)
            pageItems(readings, page, perPage).forEach { reading ->
                PanelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(reading.name, modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(displayReading(reading, fahrenheit),
                            color = if (reading.value == null) Muted else Cyan,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                    }
                    Text("Source: ${reading.source}", color = Muted, fontSize = 11.sp)
                }
            }
            PageControls(readings.size, perPage, page, { requestedPage = it }, "metric-reading")
        }
    }
}

@Composable
internal fun LiveMonitor(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    recorded: List<DeviceSnapshot>,
    savedSessions: List<RecordedSession>,
    open: (String) -> Unit
) {
    val history = samples.measuredChargePercentages()
    val primary = listOf(
        Triple("Battery", "◇", "battery-details"),
        Triple("Memory", "▤", "memory-details"),
        Triple("Thermal status", "◉", "thermal-details"),
        Triple("Storage", "⌁", "storage-details")
    )
    var tools by rememberSaveable { mutableStateOf(false) }
    if (tools) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { tools = false },
            title = { Text("Monitor tools") },
            text = {
                Column {
                    listOf(
                        "History" to "history",
                        "Recording Library" to "recording-library",
                        "Session Insights" to "session-insights",
                        "Session Recorder" to "recording-session",
                        "Performance HUD" to "overlay-configuration"
                    ).forEach { (title, route) ->
                        TextButton(
                            onClick = { tools = false; open(route) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(title) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { tools = false }) { Text("Close") } }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("live-monitor-scroll").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Live Monitor", "System readings", "Real-time activity")
        Box(
            Modifier.fillMaxWidth().testTag("monitor-chart-target")
                .clickable { tools = true }
        ) {
            // Use the same SVG-coordinate guides as all other chart boards.
            // The line is plotted only from genuine Android battery samples.
            ReferenceMeasuredChart(
                history,
                footer = if (history.size >= 2)
                    "Actual Android battery readings · ${history.size} samples"
                else
                    "Waiting for real device readings"
            )
        }
        // Old 172dp card starts rows at y402. The source card is 170dp;
        // preserve the 402dp row anchor with a two-pixel inter-card spacer.
        Spacer(Modifier.height(2.dp))
        primary.forEachIndexed { index, (title, symbol, route) ->
            ReferenceMetricRow(title, symbol, index) { open(route) }
        }
    }
}

@Composable
internal fun Diagnostics(open: (String) -> Unit) {
    val rows = listOf(
        Triple("Display", "◇", "component-test"),
        Triple("Touch", "▤", "component-test"),
        Triple("Speakers", "◉", "component-test"),
        Triple("Sensors", "⌁", "sensor-explorer")
    )
    var additional by rememberSaveable { mutableStateOf(false) }
    if (additional) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { additional = false },
            title = { Text("Additional tests") },
            text = {
                Column {
                    listOf(
                        "Live sensors" to "sensor-live",
                        "CPU benchmark" to "benchmark-center",
                        "Diagnostic results" to "diagnostic-results",
                        "Thermal status" to "thermal-details"
                    ).forEach { (title, route) ->
                        TextButton(
                            onClick = { additional = false; open(route) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(title) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { additional = false }) { Text("Close") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("diagnostics-center-scroll").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Diagnostics Center", "Hardware checks", "Opt-in device testing")
        ReferenceHeroCard { additional = true }
        Spacer(Modifier.height(0.dp))
        rows.forEachIndexed { index, (title, icon, route) ->
            ReferenceMetricRow(title, icon, index) { open(route) }
        }
    }
}
