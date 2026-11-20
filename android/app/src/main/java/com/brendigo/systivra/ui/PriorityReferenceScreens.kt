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
import androidx.compose.material3.Button
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

    LazyColumn(
        modifier = Modifier.testTag("priority-reference-$route"),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading(spec.title, spec.subtitle, spec.caption) }
        item {
            if (spec.chart) {
                ReferenceMeasuredChart(chartPoints)
            } else {
                ReferenceHeroCard {
                    // Each reference page opens its own real functional controls.
                    open(detailRoute ?: "all-screens")
                }
            }
        }
        item { Spacer(Modifier.height(0.dp)) }
        items(spec.rows.size) { index ->
            ReferenceMetricRow(
                label = spec.rows[index],
                glyph = listOf("◇", "▤", "◉", "⌁")[index],
                index = index
            ) { select(spec.rows[index]) }
        }
        // Four reference actions remain on this screen. Full controls get their
        // own route instead of a 116dp filler and a second vertical page.
        if (detailRoute != null) {
            item {
                TextButton(
                    onClick = { open(detailRoute) },
                    modifier = Modifier.fillMaxWidth().testTag("priority-open-controls-$route")
                ) { Text("Open full controls") }
            }
        }
        if (route == "benchmark-results") {
            item {
                TextButton(onClick = { open("benchmark-center") }) {
                    Text("Start a new CPU benchmark")
                }
            }
        }
        if (route == "session-analysis") {
            item {
                TextButton(onClick = { open("session-insights") }) {
                    Text("View session insights")
                }
            }
        }
    }
}
