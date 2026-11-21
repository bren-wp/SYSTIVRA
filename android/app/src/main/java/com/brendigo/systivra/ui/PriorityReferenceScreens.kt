// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
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
import com.brendigo.systivra.data.Reading

/**
 * The top viewport follows one of five 390×844 original vector boards.
 * Functional and diagnostic content is retained below the reference composition,
 * rather than being hidden or replaced by decorative fake measurements.
 */
@Composable
internal fun PriorityReferenceScreen(
    route: String,
    snapshot: DeviceSnapshot,
    recorded: List<DeviceSnapshot>,
    benchmark: BenchmarkController,
    open: (String) -> Unit
) {
    val spec = priorityReferenceSpecs[route] ?: return
    var selectedLabel by rememberSaveable(route) { mutableStateOf<String?>(null) }

    fun select(label: String) {
        when {
            route == "benchmark-running" && label == "Stop test" -> benchmark.cancel()
            route == "session-analysis" && label == "Export" -> {
                open("reports-export")
                return
            }
            route == "app-usage" && label == "Privacy" -> {
                open("privacy-center")
                return
            }
        }
        selectedLabel = label
    }

    val selected = selectedLabel?.let {
        priorityRowReading(
            route = route, label = it, snapshot = snapshot,
            recorded = recorded, benchmarkResult = benchmark.result,
            running = benchmark.running, benchmarkMessage = benchmark.message
        )
    }
    val chartPoints = priorityChartPoints(route, recorded)
    val detailRoute = when (route) {
        "benchmark-results" -> "benchmark-results-data"
        "benchmark-running" -> "benchmark-running-controls"
        "app-usage" -> "app-usage-controls"
        "session-analysis" -> "session-analysis-data"
        "cpu-cores" -> "cpu-cores-data"
        else -> null
    }

    if (selectedLabel != null) {
        ReferenceQuickResultDialog(
            testTag = "priority-selected-$route",
            title = selectedLabel.orEmpty(),
            value = selected?.display ?: "Unavailable",
            source = selected?.source,
            onDismiss = { selectedLabel = null }
        )
    }

    var showActions by rememberSaveable(route) { mutableStateOf(false) }
    if (showActions) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showActions = false },
            title = { Text("Additional actions") },
            text = {
                Column {
                    if (detailRoute != null) {
                        TextButton(onClick = {
                            showActions = false
                            open(detailRoute)
                        }, modifier = Modifier.fillMaxWidth().testTag("priority-open-controls-$route")) {
                            Text("Open full controls")
                        }
                    }
                    if (route == "benchmark-results") {
                        TextButton(onClick = {
                            showActions = false
                            open("benchmark-center")
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Start a new CPU benchmark")
                        }
                    }
                    if (route == "session-analysis") {
                        TextButton(onClick = {
                            showActions = false
                            open("session-insights")
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("View session insights")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActions = false }) { Text("Close") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("priority-reference-$route")
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading(spec.title, spec.subtitle, spec.caption)
        if (spec.chart) {
            Box(Modifier.fillMaxWidth().clickable { showActions = true }) {
                ReferenceMeasuredChart(chartPoints)
            }
        } else {
            ReferenceHeroCard {
                if (route == "session-analysis") showActions = true
                else open(detailRoute ?: "all-screens")
            }
        }
        Spacer(Modifier.height(0.dp))
        spec.rows.forEachIndexed { index, label ->
            ReferenceMetricRow(
                label = label,
                glyph = listOf("◇", "▤", "◉", "⌁")[index],
                index = index
            ) { select(label) }
        }
    }
}
