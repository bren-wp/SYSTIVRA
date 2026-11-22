// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PanelCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
        ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Color(0xFF233A5A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Raised, Panel)))
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
internal fun LinkCard(title: String, subtitle: String, open: () -> Unit) {
    val icon: ImageVector = when {
        title.contains("CPU", ignoreCase = true) || title.contains("Memory", ignoreCase = true)
            || title.contains("RAM", ignoreCase = true) -> Icons.Rounded.Memory
        title.contains("Storage", ignoreCase = true) -> Icons.Rounded.Storage
        title.contains("Battery", ignoreCase = true) || title.contains("Charging", ignoreCase = true)
            -> Icons.Rounded.BatteryChargingFull
        title.contains("Thermal", ignoreCase = true) -> Icons.Rounded.Thermostat
        title.contains("Sensor", ignoreCase = true) -> Icons.Rounded.Sensors
        title.contains("Network", ignoreCase = true) -> Icons.Rounded.NetworkCheck
        title.contains("Diagnostic", ignoreCase = true) -> Icons.Rounded.BugReport
        title.contains("Report", ignoreCase = true) || title.contains("Benchmark", ignoreCase = true)
            -> Icons.Rounded.Analytics
        title.contains("About", ignoreCase = true) -> Icons.Rounded.Info
        else -> Icons.Rounded.Tune
    }
    val accent = when {
        title.contains("Battery", ignoreCase = true) || title.contains("Thermal", ignoreCase = true)
            -> Mint
        title.contains("Diagnostic", ignoreCase = true) || title.contains("Report", ignoreCase = true)
            -> Purple
        else -> Cyan
    }
    PanelCard(open) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Raised, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Text("›", color = Muted, fontSize = 25.sp)
        }
    }
}
