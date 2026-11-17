package com.brendigo.systivra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.batteryPercentage

/**
 * Original #13 Charging Analysis screen geometry:
 * heading x=24, chart x=20 y=208 h=170, four 69dp rows at y=402,484,566,648.
 * The SVG's illustrative zigzag MUST NOT masquerade as measured battery data.
 * Only draw a trend when Android provided at least two actual charge readings.
 * All actions open working, non-simulated Android readings / saved session data.
 */
@Composable
internal fun ReferenceChargingAnalysis(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    open: (String) -> Unit
) {
    val chargeSeries = samples.mapNotNull { it.batteryPercentage()?.toFloat() }
    val actions = listOf(
        "Charge timeline" to "metric-fullscreen",
        "Current if available" to "charging-analysis-data",
        "Temperature" to "battery-details",
        "Session duration" to "session-analysis"
    )
    LazyColumn(
        modifier = Modifier.testTag("reference-charging-analysis"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Charging Analysis", "Power session", "Battery trends") }
        item {
            Box(
                Modifier.fillMaxWidth()
                    .testTag("reference-charging-chart-target")
                    .clickable(role = Role.Button, onClickLabel = "Open measured battery timeline") {
                        open("metric-fullscreen")
                    }
            ) {
                ReferenceMeasuredChart(chargeSeries)
            }
        }
        item { Spacer(Modifier.height(0.dp)) }
        items(actions.size) { index ->
            val (label, destination) = actions[index]
            ReferenceMetricRow(label, listOf("◇", "▤", "◉", "⌁")[index], index) {
                open(destination)
            }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item {
            Text(
                if (chargeSeries.size >= 2)
                    "Graph shows Android-reported charge samples, not an estimated charging curve."
                else
                    "A graph is unavailable until at least two genuine battery readings exist."
            )
        }
        item {
            Text("Current values, temperature and session timing are available in their detailed screens.")
        }
    }
}
