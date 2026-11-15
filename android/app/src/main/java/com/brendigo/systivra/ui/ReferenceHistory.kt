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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Local-calendar filtering avoids DST errors from subtracting fixed 24-hour durations. */
internal enum class HistoryWindow { TODAY, WEEK, ALL }

internal fun historyWindowStart(
    window: HistoryWindow,
    now: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    if (window == HistoryWindow.ALL) return Long.MIN_VALUE
    val start = Calendar.getInstance(timeZone).apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (window == HistoryWindow.WEEK) {
        val daysSinceMonday = (start.get(Calendar.DAY_OF_WEEK) + 5) % 7
        start.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
    }
    return start.timeInMillis
}

internal fun visibleSavedSessions(
    sessions: List<RecordedSession>,
    window: HistoryWindow,
    now: Long,
    newestFirst: Boolean = true,
    timeZone: TimeZone = TimeZone.getDefault()
): List<RecordedSession> {
    val since = historyWindowStart(window, now, timeZone)
    val filtered = sessions.filter { it.startedAt in since..now }
    return if (newestFirst) filtered.sortedByDescending { it.startedAt }
    else filtered.sortedBy { it.startedAt }
}

@Composable
internal fun ReferenceHistory(
    samples: List<DeviceSnapshot>,
    savedSessions: List<RecordedSession>,
    open: (String) -> Unit
) {
    var windowName by rememberSaveable { mutableStateOf(HistoryWindow.ALL.name) }
    val window = HistoryWindow.entries.firstOrNull { it.name == windowName } ?: HistoryWindow.ALL
    var newestFirst by rememberSaveable { mutableStateOf(true) }
    var showFilter by rememberSaveable { mutableStateOf(false) }
    val now = System.currentTimeMillis()
    val filtered = visibleSavedSessions(savedSessions, window, now, newestFirst)

    if (showFilter) {
        AlertDialog(
            onDismissRequest = { showFilter = false },
            title = { Text("History filters") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saved recordings are stored privately on this device.")
                    TextButton(onClick = { newestFirst = true; showFilter = false }) {
                        Text("Newest first")
                    }
                    TextButton(onClick = { newestFirst = false; showFilter = false }) {
                        Text("Oldest first")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilter = false }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.testTag("history-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("History", "Recent sessions", "Local device records") }
        item { ReferenceHeroCard { open("recording-library") } }
        item { Spacer(Modifier.height(0.dp)) }
        item { ReferenceMetricRow("Today", "◇", 0) { windowName = HistoryWindow.TODAY.name } }
        item { ReferenceMetricRow("This week", "▤", 1) { windowName = HistoryWindow.WEEK.name } }
        item { ReferenceMetricRow("All sessions", "◉", 2) { windowName = HistoryWindow.ALL.name } }
        item { ReferenceMetricRow("Filters", "⌁", 3) { showFilter = true } }
        item { Spacer(Modifier.height(116.dp)) }
        item {
            Text("Saved recordings · ${filtered.size} sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Period: ${when (window) {
                HistoryWindow.TODAY -> "Today"
                HistoryWindow.WEEK -> "This week"
                HistoryWindow.ALL -> "All sessions"
            }}", fontSize = 13.sp, color = Muted)
        }
        item {
            TextButton(onClick = { open("recording-library") }) {
                Text("Open Recording Library")
            }
        }
        if (filtered.isEmpty()) {
            item { Text("No saved recordings for this period.", fontSize = 13.sp, color = Muted) }
        }
        items(filtered, key = { "saved-${it.id}" }) { session ->
            Card(
                onClick = { open("recording-library") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Color(0xFF233857))
            ) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.US)
                            .format(Date(session.startedAt)),
                        fontWeight = FontWeight.Bold, color = Cyan
                    )
                    Text("${session.sampleCount} recorded samples", color = Muted, fontSize = 13.sp)
                }
            }
        }
        item {
            Text("Recent in-memory readings · ${samples.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("These live samples are not saved unless recording is started.", fontSize = 12.sp, color = Muted)
        }
        items(samples.asReversed().take(10)) { reading ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(reading.time)), color = Cyan)
                    Text("Battery: ${reading.battery.firstOrNull()?.display ?: "Unavailable"}", fontSize = 13.sp)
                }
            }
        }
    }
}
