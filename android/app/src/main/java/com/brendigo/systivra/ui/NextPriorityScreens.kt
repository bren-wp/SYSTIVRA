// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.brendigo.systivra.ui.LocalizedText as Text
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

/**
 * Genuine reference-first layout for originals #02, #04, #19, #22, #29.
 *
 * The first viewport uses the same 390x844 geometry as the original SVG
 * concepts; the operational controls remain available below the fold and
 * through a separate functional route. Missing hardware data is never invented.
 */
@Composable
internal fun NextPriorityReferenceScreen(
    route: String,
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    benchmark: BenchmarkController,
    onStartBenchmark: () -> Unit,
    open: (String) -> Unit
) {
    val spec = nextPriorityReferences[route] ?: return
    var chosen by rememberSaveable(route) { mutableStateOf<String?>(null) }
    val selected = chosen?.let {
        nextPriorityRowReading(route, it, snapshot, samples)
    }
    val detailRoute = when (route) {
        "system-health" -> "system-health-data"
        "privacy-center" -> "privacy-center-data"
        "sensor-live" -> "sensor-live-controls"
        "metric-fullscreen" -> "metric-fullscreen-data"
        "benchmark-center" -> "benchmark-center-controls"
        else -> null
    }

    fun select(label: String) {
        when (route to label) {
            "system-health" to "Battery status" -> open("battery-details")
            "system-health" to "Storage and memory" -> open("device-capacity")
            "system-health" to "Hardware diagnostics" -> open("diagnostics-center")
            "privacy-center" to "Optional Android access" -> open("permission-center")
            "sensor-live" to "Sensor metadata" -> open("sensor-explorer")
            "sensor-live" to "X axis",
            "sensor-live" to "Y axis",
            "sensor-live" to "Z axis" -> open("sensor-live-controls")
            "benchmark-center" to "CPU test" -> {
                if (benchmark.running) open("benchmark-running")
                else onStartBenchmark()
            }
            else -> {
                chosen = label
            }
        }
    }

    if (chosen != null) {
        ReferenceQuickResultDialog(
            testTag = "next-selected-$route",
            title = chosen.orEmpty(),
            value = selected?.display ?: "Unavailable",
            source = selected?.source,
            onDismiss = { chosen = null }
        )
    }

    var showActions by rememberSaveable(route) { mutableStateOf(false) }
    if (showActions) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showActions = false },
            title = { Text("Performance options") },
            text = {
                Column {
                    if (detailRoute != null) {
                        TextButton(onClick = {
                            showActions = false
                            open(detailRoute)
                        }, modifier = Modifier.fillMaxWidth().testTag("next-controls-$route")) {
                            Text("Open full controls")
                        }
                    }
                    if (route == "benchmark-center" && benchmark.result != null) {
                        TextButton(onClick = {
                            showActions = false
                            open("benchmark-results")
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("View last CPU result")
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
        modifier = Modifier.fillMaxSize().testTag("next-reference-$route").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading(spec.title, spec.subtitle, spec.caption)
        if (spec.hasChart) {
            Box(
                Modifier.fillMaxWidth().clickable { open(detailRoute ?: "dashboard") }
            ) {
                ReferenceMeasuredChart(
                    if (route == "metric-fullscreen") validBatteryPercentages(samples)
                    else emptyList()
                )
            }
        } else {
            ReferenceHeroCard {
                if (route == "benchmark-center") showActions = true
                else open(detailRoute ?: "dashboard")
            }
        }
        Spacer(Modifier.height(if (spec.hasChart) 1.dp else 0.dp))
        spec.rows.forEachIndexed { index, label ->
            ReferenceMetricRow(
                label, listOf("◇", "▤", "◉", "⌁")[index], index
            ) { select(label) }
        }
    }
}
