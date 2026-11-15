package com.brendigo.systivra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.parseBatteryPercent
import kotlinx.coroutines.launch

/**
 * Aligns original #23 (Recording Session) and #35 (Permission Center) designs.
 * The original working controls are preserved behind dedicated details routes.
 */
internal val recordingReferenceRows =
    listOf("Start", "Pause", "Duration", "Storage policy")
internal val permissionReferenceRows =
    listOf("Notifications", "Overlay", "Camera", "Microphone")

internal fun recordedBatteryPoints(samples: List<DeviceSnapshot>): List<Float> =
    samples.mapNotNull { reading ->
        parseBatteryPercent(reading.battery.firstOrNull { it.name == "Charge level" }?.value)
            ?.toFloat()
    }

internal fun latestSessionSpanSeconds(sessions: List<RecordedSession>): Long? =
    sessions.maxByOrNull { it.startedAt }?.let {
        (it.lastSampleAt - it.startedAt).coerceAtLeast(0L) / 1000L
    }

@Composable
internal fun RecordingReferenceScreen(
    recorded: List<DeviceSnapshot>,
    sessions: List<RecordedSession>,
    recording: Boolean,
    ready: Boolean,
    storageError: String?,
    toggle: () -> Unit,
    open: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val span = latestSessionSpanSeconds(sessions)
    val value = when (chosen) {
        "Start" -> if (recording) "Recording while SYSTIVRA is visible"
            else if (ready) "Ready to begin a new private recording" else "Loading saved history"
        "Pause" -> if (recording) "Stop active foreground recording"
            else "No active recording. A new Start creates a new session."
        "Duration" -> span?.let { "$it seconds of stored samples" }
            ?: "No saved session available"
        "Storage policy" -> "Maximum 900 samples; app-private SQLite; no cloud upload or backup"
        else -> if (recording) "Recording while app is visible"
            else "Foreground-only recording is inactive"
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.testTag("recording-reference-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ReferenceHeading("Recording Session", "Session capture", "User-controlled recording")
        }
        item { ReferenceMeasuredChart(recordedBatteryPoints(recorded)) }
        item { Spacer(Modifier.height(0.dp)) }
        items(recordingReferenceRows.size) { index ->
            val label = recordingReferenceRows[index]
            ReferenceMetricRow(label, listOf("◇", "▤", "◉", "⌁")[index], index) {
                chosen = label
                if (ready && label == "Start" && !recording) toggle()
                if (ready && label == "Pause" && recording) toggle()
                scope.launch { listState.animateScrollToItem(9) }
            }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item {
            Text("Actual session controls", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Stopping ends the current saved session. Starting again creates a new one.",
                fontSize = 12.sp, color = Muted
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("recording-reference-data"),
                colors = CardDefaults.cardColors(containerColor = Raised),
                border = BorderStroke(1.dp, Color(0xFF36547E))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(chosen ?: "Recording status", fontWeight = FontWeight.Bold)
                    Text(value, color = Cyan, fontSize = 14.sp)
                    Text("Stored snapshots: ${recorded.size} / 900", fontSize = 12.sp, color = Muted)
                    if (storageError != null) Text(storageError, fontSize = 12.sp, color = Muted)
                }
            }
        }
        item {
            Button(
                onClick = { open("recording-session-controls") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Open recording, export and deletion controls") }
        }
        item {
            TextButton(onClick = { open("recording-library") }) {
                Text("Open saved Recording Library")
            }
        }
    }
}

@Composable
internal fun PermissionReferenceScreen(open: (String) -> Unit) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(
        state = listState,
        modifier = Modifier.testTag("permission-reference-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Permission Center", "Access overview", "Optional features") }
        item { ReferenceHeroCard { open("permission-center-controls") } }
        item { Spacer(Modifier.height(0.dp)) }
        items(permissionReferenceRows.size) { index ->
            ReferenceMetricRow(
                permissionReferenceRows[index], listOf("◇", "▤", "◉", "⌁")[index], index
            ) {
                chosen = permissionReferenceRows[index]
                scope.launch { listState.animateScrollToItem(9) }
            }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item { Text("Permission details", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("permission-reference-data"),
                colors = CardDefaults.cardColors(containerColor = Raised),
                border = BorderStroke(1.dp, Color(0xFF36547E))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(chosen ?: "Optional permissions", fontWeight = FontWeight.Bold)
                    Text(
                        if (chosen == null)
                            "SYSTIVRA does not request camera, microphone, overlay or notification permissions."
                        else "$chosen is not requested by this Android build.",
                        color = Muted, fontSize = 14.sp
                    )
                    Text("Source: AndroidManifest.xml", color = Muted, fontSize = 12.sp)
                }
            }
        }
        item {
            Button(
                onClick = { open("permission-center-controls") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Review Android Usage Access and app permissions") }
        }
    }
}
