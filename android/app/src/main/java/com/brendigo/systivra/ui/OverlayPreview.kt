// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.chargeLevelText
import com.brendigo.systivra.data.availableRamText
import com.brendigo.systivra.data.thermalStatusText

/**
 * All four editor actions are functional IN APP: metrics, opacity, size and
 * position. This is not an Android SYSTEM_ALERT_WINDOW overlay; we do not
 * request broad draw-over-other-apps access or run a background service.
 */
@Composable
fun OverlayPreviewScreen(snapshot: DeviceSnapshot) {
    var showBattery by rememberSaveable { mutableStateOf(true) }
    var showMemory by rememberSaveable { mutableStateOf(true) }
    var showThermal by rememberSaveable { mutableStateOf(true) }
    var opacity by rememberSaveable { mutableFloatStateOf(0.9f) }
    var sizeFraction by rememberSaveable { mutableFloatStateOf(1f) }
    var position by rememberSaveable { mutableStateOf("Top left") }
    val alignment = when (position) {
        "Top right" -> Alignment.TopEnd
        "Bottom left" -> Alignment.BottomStart
        "Bottom right" -> Alignment.BottomEnd
        else -> Alignment.TopStart
    }

    var section by rememberSaveable { mutableStateOf("Metrics") }
    Column(
        modifier = Modifier.fillMaxSize().testTag("hud-preview-controls").verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Performance HUD", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Interactive in-app preview. No permission to draw over other apps.",
            color = Muted, fontSize = 12.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Box(
                Modifier.fillMaxWidth().height(208.dp).padding(12.dp),
                contentAlignment = alignment
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(sizeFraction).testTag("hud-preview-card"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF152235).copy(alpha = opacity)
                    )
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("SYSTIVRA · Live Preview", color = Cyan, fontWeight = FontWeight.Bold)
                        if (showBattery) Text("Battery  " + (snapshot.chargeLevelText() ?: "Unavailable"))
                        if (showMemory) Text("Free RAM  " + (snapshot.availableRamText() ?: "Unavailable"))
                        if (showThermal) Text("Thermal  " + (snapshot.thermalStatusText() ?: "Unavailable"))
                        if (!showBattery && !showMemory && !showThermal) {
                            Text("Select at least one metric.", color = Muted)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("Metrics", "Appearance", "Position").forEach { label ->
                OutlinedButton(
                    modifier = Modifier.weight(1f).testTag("hud-tab-${label.lowercase()}"),
                    onClick = { section = label }
                ) { Text(label, fontSize = 11.sp, maxLines = 1) }
            }
        }
        when (section) {
            "Metrics" -> {
                MetricToggle("Battery", showBattery) { showBattery = it }
                MetricToggle("Memory", showMemory) { showMemory = it }
                MetricToggle("Thermal status", showThermal) { showThermal = it }
            }
            "Appearance" -> {
                Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                    Column(
                        Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Preview opacity: ${(opacity * 100).toInt()}%")
                        Slider(
                            value = opacity, onValueChange = { opacity = it },
                            valueRange = 0.4f..1f,
                            modifier = Modifier.testTag("hud-opacity-slider")
                        )
                        Text("Preview size: ${(sizeFraction * 100).toInt()}%")
                        Slider(
                            value = sizeFraction, onValueChange = { sizeFraction = it },
                            valueRange = 0.65f..1f,
                            modifier = Modifier.testTag("hud-size-slider")
                        )
                    }
                }
            }
            else -> {
                Text("Preview position: $position", color = Cyan, fontWeight = FontWeight.SemiBold)
                listOf(
                    listOf("Top left", "Top right"),
                    listOf("Bottom left", "Bottom right")
                ).forEach { options ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        options.forEach { choice ->
                            OutlinedButton(
                                modifier = Modifier.weight(1f).testTag(
                                    "hud-position-" + choice.lowercase().replace(" ", "-")
                                ),
                                onClick = { position = choice }
                            ) { Text(choice, fontSize = 12.sp, maxLines = 1) }
                        }
                    }
                }
            }
        }
        Text("Size, position and opacity affect only this local preview.",
            color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun MetricToggle(name: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = Cyan, fontWeight = FontWeight.Medium)
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}
