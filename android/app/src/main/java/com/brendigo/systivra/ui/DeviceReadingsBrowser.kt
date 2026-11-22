// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.Reading

@Composable
internal fun DeviceReadingsBrowser(route: String, readings: List<Reading>) {
    val spec = detailReferences[route] ?: return
    val context = LocalContext.current
    val fahrenheit = remember(context) { preferredFahrenheit(context) }
    var searchQuery by rememberSaveable(route) { mutableStateOf("") }
    var requestedPage by rememberSaveable(route) { mutableIntStateOf(0) }
    val filtered = filterDeviceReadings(readings, searchQuery)
    BoxWithConstraints(Modifier.fillMaxSize().testTag("readings-browser-$route")) {
        val perPage = if (maxHeight < 590.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(filtered.size, perPage) - 1)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text("${spec.title} readings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "${readings.count { !it.value.isNullOrBlank() }} of ${readings.size} Android measurements available",
                color = Muted, fontSize = 13.sp
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it; requestedPage = 0 },
                modifier = Modifier.fillMaxWidth().testTag("metric-search-$route"),
                label = { Text("Search readings") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = Muted,
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Muted,
                    cursorColor = Cyan,
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel
                )
            )
            if (filtered.isEmpty()) {
                Text(
                    if (readings.isEmpty()) "No readings are available from Android"
                    else "No readings match your search",
                    color = Muted
                )
            }
            pageItems(filtered, page, perPage).forEach { reading ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                    border = BorderStroke(1.dp, Color(0xFF233857))
                ) {
                    Column(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(reading.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            displayReading(reading, fahrenheit),
                            color = if (reading.value.isNullOrBlank()) Muted else Cyan
                        )
                        Text("Source: ${reading.source}", color = Muted, fontSize = 12.sp)
                        if (!reading.value.isNullOrBlank()) {
                            TextButton(onClick = { copyMeasurement(context, reading) }) {
                                Text("Copy measurement")
                            }
                        }
                    }
                }
            }
            PageControls(
                total = filtered.size, pageSize = perPage, page = page,
                onPageChange = { requestedPage = it }, tag = "readings-$route"
            )
        }
    }
}
