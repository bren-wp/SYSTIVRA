package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
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
    var moreActions by rememberSaveable { mutableStateOf(false) }
    if (moreActions) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { moreActions = false },
            title = { Text("Recording options") },
            text = {
                Column {
                    TextButton(onClick = {
                        moreActions = false
                        open("recording-session-controls")
                    }, modifier = Modifier.fillMaxWidth().testTag("recording-open-controls")) {
                        Text("Recording controls")
                    }
                    TextButton(onClick = {
                        moreActions = false
                        open("recording-library")
                    }, modifier = Modifier.fillMaxWidth().testTag("recording-open-library")) {
                        Text("Saved recordings")
                    }
                    if (storageError != null) {
                        Text(storageError, color = Muted, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { moreActions = false }) { Text("Close") } }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("recording-reference-scroll")
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Recording Session", "Session capture", "User-controlled recording")
        Box(Modifier.fillMaxWidth().clickable { moreActions = true }) {
            ReferenceMeasuredChart(recordedBatteryPoints(recorded))
        }
        Spacer(Modifier.height(0.dp))
        recordingReferenceRows.forEachIndexed { index, label ->
            ReferenceMetricRow(label, listOf("◇", "▤", "◉", "⌁")[index], index) {
                chosen = label
                if (ready && label == "Start" && !recording) toggle()
                if (ready && label == "Pause" && recording) toggle()
            }
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
    Column(
        modifier = Modifier.fillMaxSize().testTag("permission-reference-scroll")
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Permission Center", "Access overview", "Optional features")
        ReferenceHeroCard { open("permission-center-controls") }
        Spacer(Modifier.height(0.dp))
        permissionReferenceRows.forEachIndexed { index, label ->
            ReferenceMetricRow(label, listOf("◇", "▤", "◉", "⌁")[index], index) {
                chosen = label
            }
        }
    }
}
