package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.parseBatteryPercent

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
    if (chosen != null) {
        ReferenceQuickResultDialog(
            testTag = "recording-reference-data",
            title = chosen.orEmpty(),
            value = value,
            source = "Android foreground sampling and app-private SQLite",
            onDismiss = { chosen = null }
        )
    }
    LazyColumn(
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
            }
        }
        if (storageError != null) {
            item {
                Text(
                    storageError,
                    modifier = Modifier.fillMaxWidth().testTag("recording-storage-error"),
                    color = Muted,
                    fontSize = 13.sp
                )
            }
        }
        item {
            TextButton(
                onClick = { open("recording-session-controls") },
                modifier = Modifier.fillMaxWidth().testTag("recording-open-controls")
            ) { Text("Recording controls") }
        }
        item {
            TextButton(
                onClick = { open("recording-library") },
                modifier = Modifier.fillMaxWidth().testTag("recording-open-library")
            ) { Text("Saved recordings") }
        }
    }
}

@Composable
internal fun PermissionReferenceScreen(open: (String) -> Unit) {
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    if (chosen != null) {
        ReferenceQuickResultDialog(
            testTag = "permission-reference-data",
            title = chosen.orEmpty(),
            value = "${chosen.orEmpty()} is not requested by this Android build.",
            source = "AndroidManifest.xml",
            onDismiss = { chosen = null }
        )
    }
    LazyColumn(
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
            }
        }
        item {
            TextButton(
                onClick = { open("permission-center-controls") },
                modifier = Modifier.fillMaxWidth().testTag("permission-open-controls")
            ) { Text("Review Android permissions") }
        }
    }
}
