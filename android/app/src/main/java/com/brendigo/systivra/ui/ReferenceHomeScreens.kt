// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot

@Composable
internal fun Dashboard(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    open: (String) -> Unit
) {
    var showActions by rememberSaveable { mutableStateOf(false) }
    val primary = listOf(
        Triple("Memory", "◇", "memory-details"),
        Triple("Storage", "▤", "storage-details"),
        Triple("Battery", "◉", "battery-details"),
        Triple("Thermal status", "⌁", "thermal-details")
    )
    if (showActions) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showActions = false },
            title = { Text("Device shortcuts") },
            text = {
                Column {
                    TextButton(onClick = {
                        showActions = false
                        open("device-and-android")
                    }, modifier = Modifier.fillMaxWidth()) { Text("Device & Android") }
                    TextButton(onClick = {
                        showActions = false
                        open("all-screens")
                    }, modifier = Modifier.fillMaxWidth().testTag("dashboard-all-tools")) {
                        Text("All tools")
                    }
                    TextButton(onClick = {
                        showActions = false
                        open("hardware-explorer")
                    }, modifier = Modifier.fillMaxWidth().testTag("dashboard-hardware-shortcut")) {
                        Text("Hardware explorer")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActions = false }) { Text("Close") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("dashboard-page")
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Dashboard", "Your device at a glance", "Live information")
        ReferenceHeroCard { showActions = true }
        Spacer(Modifier.height(0.dp))
        primary.forEachIndexed { index, (label, symbol, destination) ->
            ReferenceMetricRow(label, symbol, index) { open(destination) }
        }
    }
}

@Composable
internal fun ReferenceHeroCard(open: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(128.dp)
            .testTag("reference-hero").clickable { open() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152235)),
        border = BorderStroke(1.dp, Color(0xFF233857))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(21.dp)
        ) {
            Canvas(Modifier.size(88.dp).testTag("reference-hero-ring")) {
                // SVG: circle cx=78 cy=272 r=38 stroke=11; foreground path
                // starts at 12 o'clock and finishes at roughly +25 degrees.
                // The previous drawArc used the full 88dp Canvas as its bounds
                // (r=44), while the background circle was only r~38. This
                // shifted the cyan arc 6px outwards and broke the 3D ring.
                val ringWidth = 11.dp.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension - 12.dp.toPx()) / 2f
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                drawCircle(
                    color = Color(0xFF223E60),
                    radius = radius,
                    style = Stroke(width = ringWidth)
                )
                val gradientStart = Offset(center.x, center.y - radius)
                val gradientExtent = svgDiagonalGradientEnd(
                    radius, radius * 1.423f
                )
                drawArc(
                    brush = Brush.linearGradient(
                        0f to Cyan,
                        0.6f to Color(0xFF5975F5),
                        1f to Color(0xFFB052EC),
                        start = gradientStart,
                        end = gradientStart + gradientExtent
                    ),
                    startAngle = -90f,
                    sweepAngle = 115f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = ringWidth, cap = StrokeCap.Round)
                )
            }
            Column {
                Text("SYSTIVRA", modifier = Modifier.testTag("reference-hero-title")
                    .offset(y = (-1).dp),
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(5.dp))
                Text("System intelligence", modifier = Modifier.testTag("reference-hero-subtitle")
                    .offset(y = (-5).dp),
                    fontSize = 13.sp, color = Muted)
            }
        }
    }
}

@Composable
internal fun ReferenceMetricRow(
    label: String,
    glyph: String,
    index: Int,
    onClick: () -> Unit
) {
    val tone = if (index % 2 == 0) Color(0xFF153B68) else Color(0xFF34245B)
    val glyphColor = if (index % 2 == 0) Cyan else Color(0xFFC39BFF)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(69.dp)
            // SVG action rows start 82px apart; LazyColumn uses a 12px gap.
            // Apply a -1dp hero-to-row correction, then +1dp per subsequent row.
            .offset(y = (index - 1).dp)
            .testTag("reference-row-" + label.lowercase().replace(" ", "-")),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152235)),
        border = BorderStroke(1.dp, Color(0xFF223654))
    ) {
        Row(
            Modifier.fillMaxSize().padding(start = 14.dp, end = 21.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(34.dp).background(tone, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                ReferenceSymbol(glyph, glyphColor, "reference-row-icon-" +
                    label.lowercase().replace(" ", "-"))
            }
            Column(
                Modifier.weight(1f).padding(start = 13.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(label,
                    modifier = Modifier.testTag("reference-row-title-" + label.lowercase().replace(" ", "-"))
                        .offset(x = 1.dp, y = 4.dp),
                    // Native Roboto label glyphs render slightly wider than the
                    // original Arial-based 16sp SVG row titles.
                    style = LocalTextStyle.current.copy(textGeometricTransform = TextGeometricTransform(scaleX = 0.98f)),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Details / availability",
                    modifier = Modifier.testTag("reference-row-subtitle-" + label.lowercase().replace(" ", "-")),
                    fontSize = 12.sp, color = Muted)
            }
            Text("›", color = Muted, fontSize = 22.sp)
        }
    }
}

@Composable
internal fun HardwareExplorer(open: (String) -> Unit) {
    var showMore by rememberSaveable { mutableStateOf(false) }
    val primary = listOf(
        Triple("CPU and SoC", "◇", "cpu-details"),
        Triple("GPU", "▤", "gpu-details"),
        Triple("Memory", "◉", "memory-details"),
        Triple("Sensors", "⌁", "sensor-explorer")
    )
    if (showMore) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showMore = false },
            title = { Text("Additional hardware") },
            text = {
                Column {
                    listOf(
                        "Storage" to "storage-details",
                        "Battery" to "battery-details",
                        "Thermal" to "thermal-details",
                        "Display" to "display-details",
                        "Device & Android" to "device-and-android"
                    ).forEach { (label, destination) ->
                        TextButton(
                            onClick = { showMore = false; open(destination) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(label) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMore = false }) { Text("Close") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("hardware-explorer-page")
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Hardware Explorer", "Explore components", "Hardware information")
        ReferenceHeroCard { showMore = true }
        Spacer(Modifier.height(0.dp))
        primary.forEachIndexed { index, (label, symbol, destination) ->
            ReferenceMetricRow(label, symbol, index) { open(destination) }
        }
    }
}
