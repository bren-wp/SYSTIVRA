// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.brendigo.systivra.data.measuredChargePercentages
import com.brendigo.systivra.data.chargeLevelText

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
    val chargeSeries = samples.measuredChargePercentages()
    val actions = listOf(
        "Charge timeline" to "metric-fullscreen",
        "Current if available" to "charging-analysis-data",
        "Temperature" to "battery-details",
        "Session duration" to "session-analysis"
    )
    Column(
        modifier = Modifier.fillMaxSize().testTag("reference-charging-analysis")
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Charging Analysis", "Power session", "Battery trends")
        Box(
            Modifier.fillMaxWidth()
                .testTag("reference-charging-chart-target")
                .clickable(role = Role.Button, onClickLabel = "Open measured battery timeline") {
                    open("metric-fullscreen-data")
                }
        ) {
            ReferenceMeasuredChart(chargeSeries)
        }
        Spacer(Modifier.height(0.dp))
        actions.forEachIndexed { index, (label, destination) ->
            ReferenceMetricRow(label, listOf("◇", "▤", "◉", "⌁")[index], index) {
                open(destination)
            }
        }
    }
}
