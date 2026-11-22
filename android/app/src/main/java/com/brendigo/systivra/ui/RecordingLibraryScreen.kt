// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.RecordedSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var actionsFor by remember { mutableStateOf<RecordedSession?>(null) }
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    val format = remember { SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.US) }

    pendingDelete?.let { session ->
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this recording?") },
            text = { Text("Delete ${session.sampleCount} saved readings? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    enabled = !isRecording,
                    onClick = {
                        if (!isRecording) onDelete(session.id)
                        pendingDelete = null
                    }
                ) { Text("Delete", color = Cyan) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
    actionsFor?.let { session ->
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { actionsFor = null },
            title = { Text("Recording actions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("${session.sampleCount} measured samples")
                    TextButton(
                        onClick = {
                            actionsFor = null
                            onSelect(session.id)
                            open("session-analysis")
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Analyze") }
                    TextButton(
                        onClick = {
                            actionsFor = null
                            onSelect(session.id)
                            open("session-insights")
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Session insights") }
                    TextButton(
                        onClick = {
                            actionsFor = null
                            onSelect(session.id)
                            open("reports-export")
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Export CSV, JSON or PDF") }
                    TextButton(
                        enabled = !isRecording,
                        onClick = {
                            actionsFor = null
                            pendingDelete = session
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Delete session") }
                }
            },
            confirmButton = {
                TextButton(onClick = { actionsFor = null }) { Text("Close") }
            }
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize().testTag("recording-library-page")) {
        val perPage = if (maxHeight < 720.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(sessions.size, perPage) - 1)
        Column(
            Modifier.fillMaxSize().testTag("recording-library-scroll-content")
                .verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text("Recording Library", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(
                "${sessions.sumOf { it.sampleCount }} samples across ${sessions.size} saved sessions",
                fontSize = 13.sp, color = Muted
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    enabled = sessions.isNotEmpty(),
                    modifier = Modifier.weight(1f).testTag("recordings-analyze-all"),
                    onClick = { onSelect(null); open("session-analysis") }
                ) { Text("Analyze all") }
                OutlinedButton(
                    enabled = sessions.isNotEmpty(),
                    modifier = Modifier.weight(1f).testTag("recordings-insights-all"),
                    onClick = { onSelect(null); open("session-insights") }
                ) { Text("All insights") }
            }
            if (sessions.isEmpty()) {
                Text("No saved recordings yet. Start recording from Monitor.", color = Muted)
            }
            pageItems(sessions, page, perPage).forEach { session ->
                Card(
                    onClick = { actionsFor = session },
                    modifier = Modifier.fillMaxWidth().testTag("recording-session-${session.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedId == session.id) Raised else Panel
                    )
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(format.format(Date(session.startedAt)),
                            fontWeight = FontWeight.Bold, color = Cyan)
                        Text(
                            "${session.sampleCount} saved samples · " +
                                "${(session.lastSampleAt - session.startedAt).coerceAtLeast(0L) / 1000L}s",
                            fontSize = 13.sp, color = Muted
                        )
                        Text("Tap for analysis, export or deletion", fontSize = 12.sp, color = Muted)
                    }
                }
            }
            PageControls(
                total = sessions.size, pageSize = perPage, page = page,
                onPageChange = { requestedPage = it }, tag = "recording-library"
            )
            Text(
                "Saved locally. Up to 900 total readings. Deletion is disabled during recording.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}
