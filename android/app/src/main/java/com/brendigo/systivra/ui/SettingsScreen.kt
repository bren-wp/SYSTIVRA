// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Slider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import kotlin.math.roundToInt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun Settings(open: (String) -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("systivra_settings", Context.MODE_PRIVATE)
    }
    var samplingSeconds by rememberSaveable {
        mutableIntStateOf(preferences.getInt("sampling_seconds", 2).coerceIn(1, 10))
    }
    var temperatureUnit by rememberSaveable {
        mutableStateOf(preferences.getString(TEMPERATURE_PREF_KEY, "C")?.takeIf { it == "F" } ?: "C")
    }
    var activeDialog by rememberSaveable { mutableStateOf<String?>(null) }

    if (activeDialog != null) {
        AlertDialog(
            // Never allow inherited dark text on a midnight dialog surface.
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { activeDialog = null },
            title = { Text(activeDialog.orEmpty()) },
            text = {
                when (activeDialog) {
                    "Appearance" -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SYSTIVRA uses its midnight dark theme and English interface.")
                        Text("No account, onboarding or unsupported theme toggle is required.", color = Muted)
                    }
                    "Update interval" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Refresh interval: ${samplingSeconds} seconds (foreground only)")
                        Slider(
                            value = samplingSeconds.toFloat(),
                            valueRange = 1f..10f,
                            steps = 8,
                            onValueChange = { value ->
                                samplingSeconds = value.roundToInt().coerceIn(1, 10)
                                preferences.edit().putInt("sampling_seconds", samplingSeconds).apply()
                            }
                        )
                        Text("Applied to live device sampling and saved for future launches.", color = Muted)
                    }
                    "Units" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Temperature display")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(
                                modifier = Modifier.testTag("units-celsius"),
                                selected = temperatureUnit == "C",
                                onClick = {
                                    temperatureUnit = "C"
                                    preferences.edit().putString(TEMPERATURE_PREF_KEY, "C").apply()
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Panel,
                                    labelColor = TextPrimary,
                                    selectedContainerColor = Color(0xFF153B68),
                                    selectedLabelColor = TextPrimary,
                                    disabledContainerColor = Raised,
                                    disabledLabelColor = Muted
                                ),
                                label = { Text("Celsius · °C") }
                            )
                            FilterChip(
                                modifier = Modifier.testTag("units-fahrenheit"),
                                selected = temperatureUnit == "F",
                                onClick = {
                                    temperatureUnit = "F"
                                    preferences.edit().putString(TEMPERATURE_PREF_KEY, "F").apply()
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Panel,
                                    labelColor = TextPrimary,
                                    selectedContainerColor = Color(0xFF153B68),
                                    selectedLabelColor = TextPrimary,
                                    disabledContainerColor = Raised,
                                    disabledLabelColor = Muted
                                ),
                                label = { Text("Fahrenheit · °F") }
                            )
                        }
                        Text(
                            "Applies to battery and thermal readings in device details. " +
                                "Original Android measurements and exports are unchanged.",
                            color = Muted
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) { Text("Close") }
            }
        )
    }

    var shortcuts by rememberSaveable { mutableStateOf(false) }
    if (shortcuts) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { shortcuts = false },
            title = { Text("More options") },
            text = {
                Column {
                    listOf(
                        Triple("Screen catalog", "all-screens", "settings-all-tools"),
                        Triple("About SYSTIVRA", "about", "settings-about-shortcut"),
                        Triple("Device & Android", "device-and-android", "settings-device-shortcut")
                    ).forEach { (label, destination, tag) ->
                        TextButton(
                            onClick = { shortcuts = false; open(destination) },
                            modifier = Modifier.fillMaxWidth().testTag(tag)
                        ) { Text(label) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { shortcuts = false }) { Text("Close") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("settings-page").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("Settings", "Preferences", "Customize your experience")
        ReferenceHeroCard { shortcuts = true }
        Spacer(Modifier.height(0.dp))
        ReferenceMetricRow("Appearance", "◇", 0) { activeDialog = "Appearance" }
        ReferenceMetricRow("Update interval", "▤", 1) { activeDialog = "Update interval" }
        ReferenceMetricRow("Units", "◉", 2) { activeDialog = "Units" }
        ReferenceMetricRow("Privacy", "⌁", 3) { open("privacy-center") }
    }
}
