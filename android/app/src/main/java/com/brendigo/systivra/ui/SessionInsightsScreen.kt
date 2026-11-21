// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.analyzeSessionReadings
import com.brendigo.systivra.data.batteryPercentage
import java.util.Locale

/** Statistics based only on readings saved by the user. No health scores. */
@Composable
fun SessionInsightsScreen(samples: List<DeviceSnapshot>, open: (String) -> Unit) {
    val insights = analyzeSessionReadings(samples)
    val batteryPoints = samples.mapNotNull { reading ->
        if (reading.time <= 0L) null
        else reading.batteryPercentage()
            ?.let { reading.time to it.toFloat() }
    }.sortedBy { it.first }

    fun fmt(value: Double?, suffix: String = ""): String =
        value?.let { String.format(Locale.US, "%.1f", it) + suffix } ?: "Unavailable"

    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Session Insights", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Descriptive statistics from saved readings. No estimated capacity, battery health score or invented metrics.",
                color = Muted, fontSize = 12.sp)
        }
        if (insights.sampleCount == 0) {
            item { InsightCard("No recorded data", "Start a recording in the Monitor tab, then return to this screen.") }
        } else {
            item {
                InsightCard(
                    "Recording coverage",
                    insights.sampleCount.toString() + " actual readings · " +
                        ((insights.elapsedMillis ?: 0L) / 1000L) + " seconds elapsed"
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("Battery charge trend", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Cyan)
                        if (batteryPoints.size >= 2) {
                            val first = batteryPoints.first().first
                            val duration = (batteryPoints.last().first - first).coerceAtLeast(1L)
                            Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                                val coords = batteryPoints.map {
                                    Offset(
                                        x = ((it.first - first).toFloat() / duration).coerceIn(0f, 1f) * size.width,
                                        y = size.height * (1f - it.second.coerceIn(0f, 100f) / 100f)
                                    )
                                }
                                coords.zipWithNext().forEach { (from, to) ->
                                    drawLine(Mint, from, to, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                                }
                            }
                            Text("X: elapsed time · Y: Android battery percentage", color = Muted, fontSize = 11.sp)
                        } else Text("At least two valid battery readings are required.", color = Muted)
                    }
                }
            }
            item {
                InsightCard(
                    "Battery level",
                    "Range: " + fmt(insights.batteryMinPercent, "%") + " – " +
                        fmt(insights.batteryMaxPercent, "%") +
                        "\nMean: " + fmt(insights.batteryMeanPercent, "%") +
                        "\nValid readings: " + insights.batterySampleCount
                )
            }
            item {
                InsightCard(
                    "Net battery change",
                    fmt(insights.batteryDeltaPercentPoints, " percentage points") +
                        "\nSampled net rate: " +
                        fmt(insights.batteryNetPercentPointsPerHour, " pp/hour")
                )
            }
            item {
                InsightCard(
                    "Battery temperature",
                    "Range: " + fmt(insights.temperatureMinCelsius, " °C") + " – " +
                        fmt(insights.temperatureMaxCelsius, " °C") +
                        "\nMean: " + fmt(insights.temperatureMeanCelsius, " °C") +
                        "\nValid readings: " + insights.temperatureSampleCount
                )
            }
            item {
                InsightCard(
                    "Reported thermal states",
                    insights.thermalStates.entries.joinToString("\n") {
                        it.key + ": " + it.value + " readings"
                    }.ifEmpty { "Unavailable" }
                )
            }
            item { Button(onClick = { open("reports-export") }) { Text("Export saved readings") } }
            item {
                Text(
                    "The sampled net rate is displayed only when two valid percentage readings are at least " +
                        "one minute apart. Foreground pauses, charging changes and sparse readings may make " +
                        "comparisons misleading. These statistics do not assess battery health.",
                    color = Muted, fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun InsightCard(title: String, description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(description, color = Muted, fontSize = 14.sp)
        }
    }
}
