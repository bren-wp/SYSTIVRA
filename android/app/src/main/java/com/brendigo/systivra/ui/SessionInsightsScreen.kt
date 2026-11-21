// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
        else reading.batteryPercentage()?.let { reading.time to it.toFloat() }
    }.sortedBy { it.first }
    fun fmt(value: Double?, suffix: String = ""): String =
        value?.let { String.format(Locale.US, "%.1f", it) + suffix } ?: "Unavailable"
    val items = if (insights.sampleCount == 0) emptyList() else listOf(
        "Recording coverage" to "${insights.sampleCount} actual readings · ${(insights.elapsedMillis ?: 0L) / 1000L} seconds elapsed",
        "Battery charge trend" to "",
        "Battery level" to (
            "Range: " + fmt(insights.batteryMinPercent, "%") + " – " +
                fmt(insights.batteryMaxPercent, "%") +
                "\nMean: " + fmt(insights.batteryMeanPercent, "%") +
                "\nValid readings: " + insights.batterySampleCount
        ),
        "Net battery change" to (
            fmt(insights.batteryDeltaPercentPoints, " percentage points") +
                "\nSampled net rate: " + fmt(insights.batteryNetPercentPointsPerHour, " pp/hour")
        ),
        "Battery temperature" to (
            "Range: " + fmt(insights.temperatureMinCelsius, " °C") + " – " +
                fmt(insights.temperatureMaxCelsius, " °C") +
                "\nMean: " + fmt(insights.temperatureMeanCelsius, " °C") +
                "\nValid readings: " + insights.temperatureSampleCount
        ),
        "Reported thermal states" to insights.thermalStates.entries
            .joinToString("\n") { "${it.key}: ${it.value} readings" }.ifEmpty { "Unavailable" }
    )
    var page by rememberSaveable { mutableIntStateOf(0) }
    val current = page.coerceIn(0, pageCount(items.size, 1) - 1)
    Column(
        modifier = Modifier.fillMaxSize().testTag("session-insights-page").padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Session Insights", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Statistics from genuine saved readings, never estimated health scores.",
            color = Muted, fontSize = 12.sp)
        if (items.isEmpty()) {
            InsightCard("No recorded data", "Start a recording from Monitor first.")
        } else {
            val (label, description) = items[current]
            if (label == "Battery charge trend") {
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(label, color = Cyan, fontWeight = FontWeight.Bold)
                        if (batteryPoints.size >= 2) {
                            val first = batteryPoints.first().first
                            val duration = (batteryPoints.last().first - first).coerceAtLeast(1L)
                            Canvas(Modifier.fillMaxWidth().height(155.dp)) {
                                val points = batteryPoints.map {
                                    Offset(
                                        ((it.first - first).toFloat() / duration).coerceIn(0f, 1f) * size.width,
                                        size.height * (1f - it.second.coerceIn(0f, 100f) / 100f)
                                    )
                                }
                                points.zipWithNext().forEach { (a, b) ->
                                    drawLine(Mint, a, b, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                                }
                            }
                            Text("X: recorded time · Y: Android battery percentage", color = Muted, fontSize = 12.sp)
                        } else Text("At least two valid battery samples are required.", color = Muted)
                    }
                }
            } else {
                InsightCard(label, description)
            }
            PageControls(items.size, 1, current, { page = it }, "session-insights")
            OutlinedButton(onClick = { open("reports-export") }) { Text("Export saved readings") }
            Text(
                "Sampling pauses and charging changes affect observed trends; this is not a battery health assessment.",
                color = Muted, fontSize = 12.sp
            )
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
