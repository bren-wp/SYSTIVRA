// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    onSelectSession: (Long?) -> Unit,
    open: (String) -> Unit
) {
    var windowName by rememberSaveable { mutableStateOf(HistoryWindow.ALL.name) }
    val window = HistoryWindow.entries.firstOrNull { it.name == windowName } ?: HistoryWindow.ALL
    var newestFirst by rememberSaveable { mutableStateOf(true) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var showSessions by rememberSaveable { mutableStateOf(false) }
    var showLive by rememberSaveable { mutableStateOf(false) }
    var sessionPage by rememberSaveable { mutableIntStateOf(0) }
    var livePage by rememberSaveable { mutableIntStateOf(0) }
    val filtered = visibleSavedSessions(savedSessions, window, System.currentTimeMillis(), newestFirst)

    if (showFilters) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showFilters = false },
            title = { Text("History filters") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Local recordings only. Select the sort direction.")
                    TextButton(onClick = { newestFirst = true; showFilters = false }) {
                        Text("Newest first")
                    }
                    TextButton(onClick = { newestFirst = false; showFilters = false }) {
                        Text("Oldest first")
                    }
                    TextButton(onClick = {
                        showFilters = false
                        livePage = 0
                        showLive = true
                    }) { Text("Recent live readings") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilters = false }) { Text("Close") }
            }
        )
    }

    if (showSessions) {
        val page = sessionPage.coerceIn(0, pageCount(filtered.size, 1) - 1)
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showSessions = false },
            title = { Text("Saved sessions · ${filtered.size}") },
            text = {
                Column(
                    Modifier.fillMaxWidth().testTag("history-sessions-page"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filtered.isEmpty()) Text("No saved recordings for this period.", color = Muted)
                    pageItems(filtered, page, 1).forEach { session ->
                        Card(
                            onClick = {
                                showSessions = false
                                onSelectSession(session.id)
                                open("recording-library")
                            },
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            border = BorderStroke(1.dp, Color(0xFF233857))
                        ) {
                            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.US)
                                        .format(Date(session.startedAt)),
                                    color = Cyan, fontWeight = FontWeight.Bold
                                )
                                Text("${session.sampleCount} recorded samples", color = Muted)
                            }
                        }
                    }
                    PageControls(
                        total = filtered.size, pageSize = 1, page = page,
                        onPageChange = { sessionPage = it }, tag = "history-sessions"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSessions = false }) { Text("Close") }
            }
        )
    }

    if (showLive) {
        val recent = samples.asReversed()
        val page = livePage.coerceIn(0, pageCount(recent.size, 1) - 1)
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showLive = false },
            title = { Text("Recent live readings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recent.isEmpty()) Text("No live Android samples available.", color = Muted)
                    pageItems(recent, page, 1).forEach { reading ->
                        Text(
                            SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(reading.time)),
                            color = Cyan
                        )
                        Text("Battery: ${reading.battery.firstOrNull()?.display ?: "Unavailable"}")
                        Text("Live readings are not saved without recording.", color = Muted)
                    }
                    PageControls(
                        total = recent.size, pageSize = 1, page = page,
                        onPageChange = { livePage = it }, tag = "history-live"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLive = false }) { Text("Close") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 48.dp)
            .testTag("history-page"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("History", "Recent sessions", "Local device records")
        ReferenceHeroCard { open("recording-library") }
        ReferenceMetricRow("Today", "◇", 0) {
            windowName = HistoryWindow.TODAY.name
            sessionPage = 0
            showSessions = true
        }
        ReferenceMetricRow("This week", "▤", 1) {
            windowName = HistoryWindow.WEEK.name
            sessionPage = 0
            showSessions = true
        }
        ReferenceMetricRow("All sessions", "◉", 2) {
            windowName = HistoryWindow.ALL.name
            sessionPage = 0
            showSessions = true
        }
        ReferenceMetricRow("Filters", "⌁", 3) { showFilters = true }
    }
}
