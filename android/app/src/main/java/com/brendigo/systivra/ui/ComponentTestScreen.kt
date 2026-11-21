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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton

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
    var section by rememberSaveable { mutableStateOf("Display") }
    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp).testTag("component-test-page"),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Display", "Speaker", "Vibration").forEach { label ->
                OutlinedButton(
                    onClick = { section = label },
                    modifier = Modifier.weight(1f)
                        .testTag("component-tab-${label.lowercase()}")
                ) { Text(label, fontSize = 12.sp) }
            }
        }
        when (section) {
            "Display" -> {
                Text("Tap the panel and inspect each color. Confirm the result manually.", color = Muted)
                
Box(
                Modifier.fillMaxWidth().height(190.dp)
                    .background(colors[color], RoundedCornerShape(18.dp))
                    .clickable { touches += 1 },
                contentAlignment = Alignment.Center
            ) {
                Text("TAP HERE", fontWeight = FontWeight.Bold, fontSize = 25.sp,
                    color = if (color == 3) Color.Black else Color.White)
            }
                
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
            "Speaker" -> {
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
            else -> {
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
}
