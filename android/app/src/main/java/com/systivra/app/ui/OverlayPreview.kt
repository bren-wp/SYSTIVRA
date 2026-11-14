package com.systivra.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systivra.app.data.DeviceSnapshot

/**
 * In-app HUD layout editor. Does not claim SYSTEM_ALERT_WINDOW permission, a
 * background service or cross-application overlay behavior.
 */
@Composable
fun OverlayPreviewScreen(snapshot: DeviceSnapshot) {
    var showBattery by rememberSaveable { mutableStateOf(true) }
    var showMemory by rememberSaveable { mutableStateOf(true) }
    var showThermal by rememberSaveable { mutableStateOf(true) }
    var opacity by rememberSaveable { mutableFloatStateOf(0.9f) }
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Performance HUD", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(
                "In-app overlay preview and customization. Cross-app floating window is not available in this build.",
                color = Muted, fontSize = 13.sp
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(
                containerColor = Color(0xFF152235).copy(alpha = opacity)
            )) {
                Column(Modifier.fillMaxWidth().padding(19.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SYSTIVRA · Live Preview", color = Cyan, fontWeight = FontWeight.Bold)
                    if (showBattery) Text("Battery   " + (snapshot.battery.firstOrNull()?.display ?: "Unavailable"))
                    if (showMemory) Text("Free RAM   " + (snapshot.ram.getOrNull(1)?.display ?: "Unavailable"))
                    if (showThermal) Text("Thermal   " + (snapshot.thermal.firstOrNull()?.display ?: "Unavailable"))
                    if (!showBattery && !showMemory && !showThermal) Text("Choose a metric below.", color = Muted)
                }
            }
        }
        item { MetricToggle("Battery", showBattery) { showBattery = it } }
        item { MetricToggle("Memory", showMemory) { showMemory = it } }
        item { MetricToggle("Thermal status", showThermal) { showThermal = it } }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview opacity: " + (opacity * 100).toInt() + "%", fontWeight = FontWeight.Bold)
                    Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.4f..1f)
                    Text("Only this preview changes. No permission for drawing over other apps is requested.", color = Muted, fontSize = 12.sp)
                }
            }
        }
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
