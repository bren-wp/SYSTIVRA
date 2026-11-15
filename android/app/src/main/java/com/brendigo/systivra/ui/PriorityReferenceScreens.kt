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
import com.brendigo.systivra.data.Reading
import kotlinx.coroutines.launch

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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
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
        scope.launch { listState.animateScrollToItem(9) }
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

    LazyColumn(
        state = listState,
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
                    open("app-usage-controls")
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
        // The genuine, separately navigable controls stay below the designed
        // 390×844 first viewport. Never put a nested LazyColumn in this scroll.
        item { Spacer(Modifier.height(116.dp)) }
        item {
            Text("Device data and controls", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Real Android measurements only; unsupported values are unavailable.",
                color = Muted, fontSize = 12.sp
            )
        }
        item(key = "selected-reading") {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("priority-selected-$route"),
                colors = CardDefaults.cardColors(containerColor = Raised),
                border = BorderStroke(1.dp, Color(0xFF36547E))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        selectedLabel ?: "Choose a reference row above",
                        fontSize = 17.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        selected?.display ?: when (route) {
                            "benchmark-running" -> benchmark.message
                            "benchmark-results" -> if (benchmark.result == null) "No completed test" else "Test complete"
                            "session-analysis" -> "${recorded.size} recorded samples available"
                            "cpu-cores" -> snapshot.cpu
                                .firstOrNull { it.name == "Available processors" }?.display ?: "Unavailable"
                            else -> "Open usage details to check Android's optional permission"
                        },
                        color = if (selected?.value == null) Muted else Mint,
                        fontSize = 15.sp
                    )
                    selected?.let { Text("Source: ${it.source}", color = Muted, fontSize = 12.sp) }
                    if (selectedLabel != null) {
                        TextButton(onClick = { selectedLabel = null }) { Text("Clear selection") }
                    }
                }
            }
        }
        item {
            if (detailRoute != null) {
                Button(
                    onClick = { open(detailRoute) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when (route) {
                            "app-usage" -> "Open local usage controls"
                            "benchmark-running" -> "View test status and controls"
                            "benchmark-results" -> "View complete benchmark results"
                            "session-analysis" -> "View complete session analysis"
                            else -> "View Android processor information"
                        }
                    )
                }
            }
        }
        if (route == "benchmark-running") {
            item {
                if (benchmark.running) {
                    TextButton(onClick = benchmark::cancel) { Text("Stop active CPU benchmark") }
                } else {
                    TextButton(onClick = { open("benchmark-results") }) {
                        Text("View results")
                    }
                }
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
