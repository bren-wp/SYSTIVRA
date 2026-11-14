package com.systivra.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systivra.app.data.RecordedSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Private recording index. Destructive actions require confirmation. */
@Composable
fun RecordingLibraryScreen(
    sessions: List<RecordedSession>,
    isRecording: Boolean,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    onDelete: (Long) -> Unit,
    open: (String) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<RecordedSession?>(null) }
    val format = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.US) }

    pendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this recording?") },
            text = {
                Text(
                    "This removes " + session.sampleCount + " locally stored readings. " +
                        "The action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(session.id)
                    pendingDelete = null
                }) { Text("Delete", color = Cyan) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Recording Library", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(
                "Select an actual recording to analyze or export. " +
                    "Samples remain private on this device.",
                fontSize = 13.sp, color = Muted
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(
                    Modifier.fillMaxWidth().padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("All saved recordings", color = Cyan, fontWeight = FontWeight.Bold)
                    Text(
                        sessions.sumOf { it.sampleCount }.toString() + " samples across " +
                            sessions.size + " sessions",
                        fontSize = 13.sp, color = Muted
                    )
                    Button(onClick = {
                        onSelect(null)
                        open("session-analysis")
                    }) { Text("Analyze all") }
                    Button(onClick = {
                        onSelect(null)
                        open("session-insights")
                    }) { Text("All session insights") }
                }
            }
        }
        if (sessions.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Text(
                        "No saved recordings yet. Start recording from the Monitor screen.",
                        modifier = Modifier.padding(18.dp), color = Muted
                    )
                }
            }
        }
        items(sessions, key = { it.id }) { session ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedId == session.id) Raised else Panel
                )
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(format.format(Date(session.startedAt)),
                        fontWeight = FontWeight.Bold, color = Cyan, fontSize = 16.sp)
                    Text(session.sampleCount.toString() + " measured samples", color = Muted, fontSize = 13.sp)
                    val duration = (session.lastSampleAt - session.startedAt).coerceAtLeast(0L) / 1000L
                    Text("Recorded span: " + duration + " seconds", color = Muted, fontSize = 12.sp)
                    Button(onClick = {
                        onSelect(session.id)
                        open("session-analysis")
                    }) { Text("Analyze") }
                    OutlinedButton(onClick = {
                        onSelect(session.id)
                        open("session-insights")
                    }) { Text("Session insights") }
                    OutlinedButton(onClick = {
                        onSelect(session.id)
                        open("reports-export")
                    }) { Text("Export CSV or JSON") }
                    OutlinedButton(
                        enabled = !isRecording,
                        onClick = { pendingDelete = session }
                    ) { Text("Delete session") }
                }
            }
        }
        item {
            Text(
                "Only the 900 most recent samples are retained across all sessions. " +
                    "Old sessions are pruned when their last sample is removed. " +
                    "Deletion is disabled while a recording is active.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}
