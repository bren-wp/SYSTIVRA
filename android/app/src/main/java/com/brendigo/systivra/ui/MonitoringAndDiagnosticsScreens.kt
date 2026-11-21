// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.measuredChargePercentages
import com.brendigo.systivra.data.Reading

@Composable
internal fun MetricScreen(readings: List<Reading>, subtitle: String) {
    val context = LocalContext.current
    val fahrenheit = remember(context) { preferredFahrenheit(context) }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(subtitle, fontSize = 13.sp, color = Muted) }
        if (readings.isEmpty()) item { Text("Waiting for device readings…", color = Muted) }
        items(readings) { item ->
            PanelCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text(displayReading(item, fahrenheit), color = if (item.value == null) Muted else Cyan,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                }
                Text("Source: " + item.source, fontSize = 11.sp, color = Muted)
            }
        }
    }
}

@Composable
internal fun LiveMonitor(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    recorded: List<DeviceSnapshot>,
    savedSessions: List<RecordedSession>,
    open: (String) -> Unit
) {
    val history = samples.measuredChargePercentages()
    val rows = listOf(
        Triple("Battery", "◇", "battery-details"),
        Triple("Memory", "▤", "memory-details"),
        Triple("Thermal status", "◉", "thermal-details"),
        Triple("Storage", "⌁", "storage-details")
    )
    LazyColumn(
        modifier = Modifier.testTag("live-monitor-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Live Monitor", "System readings", "Real-time activity") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(172.dp).clickable { open("metric-fullscreen") },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF152235)),
                border = BorderStroke(1.dp, Color(0xFF233857))
            ) {
                Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Text("Live data chart", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Canvas(Modifier.fillMaxWidth().height(84.dp)) {
                        if (history.size >= 2) {
                            val w = size.width / (history.size - 1)
                            history.zipWithNext().forEachIndexed { index, pair ->
                                drawLine(
                                    Mint,
                                    Offset(w * index, size.height * (1f - pair.first / 100f)),
                                    Offset(w * (index + 1), size.height * (1f - pair.second / 100f)),
                                    strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        if (history.size >= 2) "Actual battery readings · " + history.size + " samples"
                        else "Waiting for real device readings",
                        color = Muted, fontSize = 11.sp
                    )
                }
            }
        }
        item { Spacer(Modifier.height(0.dp)) }
        items(rows) { (label, symbol, path) ->
            ReferenceMetricRow(label, symbol, rows.indexOfFirst { it.first == label }) { open(path) }
        }
        item { Spacer(Modifier.height(90.dp)) }
        item { Text("Monitor tools", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item { LinkCard("History", "Last " + samples.size + " foreground readings") { open("history") } }
        item { LinkCard("Recording Library", savedSessions.size.toString() + " saved sessions") { open("recording-library") } }
        item { LinkCard("Session Insights", "Real historical trends") { open("session-insights") } }
        item { LinkCard("Session Recorder", "Explicit user-controlled recording") { open("recording-session") } }
        item { LinkCard("Performance HUD", "In-app overlay settings") { open("overlay-configuration") } }
        item {
            Text(
                "Only actual readings reported by Android are shown. No synthetic sensor graph values.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}

@Composable
internal fun Diagnostics(open: (String) -> Unit) {
    val rows = listOf(
        Triple("Display", "◇", "component-test"),
        Triple("Touch", "▤", "component-test"),
        Triple("Speakers", "◉", "component-test"),
        Triple("Sensors", "⌁", "sensor-explorer")
    )
    LazyColumn(
        modifier = Modifier.testTag("diagnostics-center-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Diagnostics Center", "Hardware checks", "Opt-in device testing") }
        item { ReferenceHeroCard { open("diagnostic-results") } }
        item { Spacer(Modifier.height(0.dp)) }
        items(rows) { (label, icon, path) ->
            ReferenceMetricRow(label, icon, rows.indexOfFirst { it.first == label }) { open(path) }
        }
        item { Spacer(Modifier.height(90.dp)) }
        item { Text("Additional tests", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item { LinkCard("Live sensors", "Read values from Android sensors") { open("sensor-live") } }
        item { LinkCard("CPU benchmark", "Bounded user-started workload") { open("benchmark-center") } }
        item { LinkCard("Diagnostic results", "User-confirmed results only") { open("diagnostic-results") } }
        item { LinkCard("Thermal status", "Android platform thermal status") { open("thermal-details") } }
        item {
            Text(
                "Tests do not automatically prove device health; results depend on user confirmation.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}

@Composable
internal fun ComponentTest() {
    val context = LocalContext.current
    val soundScope = rememberCoroutineScope()
    val verdictPrefs = remember(context) {
        context.getSharedPreferences("systivra_diagnostics", android.content.Context.MODE_PRIVATE)
    }
    var touches by rememberSaveable { mutableIntStateOf(0) }
    var color by rememberSaveable { mutableIntStateOf(0) }
    var verdict by rememberSaveable { mutableStateOf(verdictPrefs.getString("display_touch", "Not assessed") ?: "Not assessed") }
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        }
    }
    val hasVibrator = vibrator?.hasVibrator() == true
    var vibrationFeedback by rememberSaveable { mutableStateOf("Not tested") }
    var speakerFeedback by rememberSaveable { mutableStateOf("Not tested") }
    var speakerWasPlayed by rememberSaveable { mutableStateOf(false) }
    var speakerVerdict by rememberSaveable {
        mutableStateOf(verdictPrefs.getString("speaker", "Not assessed") ?: "Not assessed")
    }
    var vibrationVerdict by rememberSaveable {
        mutableStateOf(verdictPrefs.getString("vibration", "Not assessed") ?: "Not assessed")
    }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Tap the panel and inspect each color. Confirm the result manually.", color = Muted) }
        item {
            Box(
                Modifier.fillMaxWidth().height(230.dp)
                    .background(colors[color], RoundedCornerShape(18.dp))
                    .clickable { touches += 1 },
                contentAlignment = Alignment.Center
            ) {
                Text("TAP HERE", fontWeight = FontWeight.Bold, fontSize = 25.sp,
                    color = if (color == 3) Color.Black else Color.White)
            }
        }
        item {
            PanelCard {
                Text("Touches registered: " + touches)
                Text("Result: " + verdict, color = Mint)
                Button(onClick = { color = (color + 1) % colors.size }) { Text("Next color") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        verdict = "Pass (user-confirmed)"
                        verdictPrefs.edit().putString("display_touch", verdict).apply()
                    }) { Text("Pass") }
                    Button(onClick = {
                        verdict = "Fail (user-confirmed)"
                        verdictPrefs.edit().putString("display_touch", verdict).apply()
                    }) { Text("Fail") }
                }
            }
        }
        item {
            PanelCard {
                Text("Speaker audio test", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Play a short tone and confirm whether you hear it. No microphone access.", color = Muted, fontSize = 13.sp)
                Text(speakerFeedback, color = Muted, fontSize = 12.sp)
                Button(onClick = {
                    soundScope.launch {
                        speakerWasPlayed = false
                        speakerFeedback = "Testing audio output…"
                        val tone = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 45) }.getOrNull()
                        if (tone == null) {
                            speakerFeedback = "The device audio output is unavailable"
                        } else {
                            try {
                                val played = runCatching {
                                    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 350)
                                }.getOrDefault(false)
                                speakerWasPlayed = played
                                speakerFeedback = if (played) "Tone requested. Did you hear it?"
                                else "The device declined the speaker test"
                                delay(400)
                            } finally {
                                runCatching { tone.release() }
                            }
                        }
                    }
                }) { Text("Play test tone") }
                Text("User-confirmed result: " + speakerVerdict, color = Mint)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = speakerWasPlayed, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = {
                        speakerVerdict = "Pass (user-confirmed)"
                        verdictPrefs.edit().putString("speaker", speakerVerdict).apply()
                    }) { Text("Pass") }
                    Button(enabled = speakerWasPlayed, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = {
                        speakerVerdict = "Fail (user-confirmed)"
                        verdictPrefs.edit().putString("speaker", speakerVerdict).apply()
                    }) { Text("Fail") }
                }
            }
        }
        item {
            PanelCard {
                Text("Vibration test", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    if (hasVibrator) "Trigger a short pulse and confirm whether you feel it."
                    else "This device does not report a vibrator.",
                    color = Muted, fontSize = 13.sp
                )
                Text(vibrationFeedback, color = Muted, fontSize = 12.sp)
                Button(enabled = hasVibrator, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = {
                    vibrationFeedback = runCatching {
                        if (Build.VERSION.SDK_INT >= 26) {
                            vibrator?.vibrate(
                                VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(200L)
                        }
                        "Pulse requested. Did you feel it?"
                    }.getOrElse { "Vibration could not be started" }
                }) { Text("Test vibration") }
                Text("User-confirmed result: " + vibrationVerdict, color = Mint)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = hasVibrator, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = {
                        vibrationVerdict = "Pass (user-confirmed)"
                        verdictPrefs.edit().putString("vibration", vibrationVerdict).apply()
                    }) { Text("Pass") }
                    Button(enabled = hasVibrator, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = {
                        vibrationVerdict = "Fail (user-confirmed)"
                        verdictPrefs.edit().putString("vibration", vibrationVerdict).apply()
                    }) { Text("Fail") }
                }
            }
        }
    }
}
