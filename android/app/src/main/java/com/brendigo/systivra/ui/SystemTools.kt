// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.batteryPercentage
import com.brendigo.systivra.data.thermalStatusText
import java.util.Locale

private val instrumentTypes = listOf(
    Sensor.TYPE_ACCELEROMETER to "Accelerometer",
    Sensor.TYPE_GYROSCOPE to "Gyroscope",
    Sensor.TYPE_MAGNETIC_FIELD to "Magnetometer",
    Sensor.TYPE_LIGHT to "Ambient light",
    Sensor.TYPE_PROXIMITY to "Proximity",
    Sensor.TYPE_PRESSURE to "Barometer",
    Sensor.TYPE_RELATIVE_HUMIDITY to "Humidity",
    Sensor.TYPE_AMBIENT_TEMPERATURE to "Ambient temperature"
)

@Composable
fun SensorLiveScreen() {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val available = remember(manager) {
        instrumentTypes.mapNotNull { (id, label) ->
            runCatching { manager?.getDefaultSensor(id) }.getOrNull()?.let { Triple(id, label, it) }
        }
    }
    var sensorId by rememberSaveable { mutableStateOf(available.firstOrNull()?.first ?: -1) }
    var values by remember { mutableStateOf<List<Float>>(emptyList()) }
    var accuracy by remember { mutableStateOf<Int?>(null) }
    val selected = available.firstOrNull { it.first == sensorId }?.third

    DisposableEffect(manager, selected) {
        values = emptyList()
        accuracy = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                values = event.values.toList().take(4)
            }
            override fun onAccuracyChanged(sensor: Sensor?, newAccuracy: Int) {
                accuracy = newAccuracy
            }
        }
        val enabled = if (manager != null && selected != null) {
            runCatching {
                manager.registerListener(listener, selected, SensorManager.SENSOR_DELAY_UI)
            }.getOrDefault(false)
        } else false
        onDispose {
            if (enabled) runCatching { manager?.unregisterListener(listener) }
        }
    }
    val selectedIndex = available.indexOfFirst { it.first == sensorId }.coerceAtLeast(0)
    Column(
        modifier = Modifier.fillMaxSize().testTag("sensor-live-page").padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Live Sensor Inspector", fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("Real SensorManager events; listener stops when leaving this screen.",
            color = Muted, fontSize = 13.sp)
        if (available.isEmpty()) {
            DetailCard("No supported sensors", "No supported sensor type is exposed by Android.")
        } else {
            DetailCard("Selected sensor", available[selectedIndex].second)
            PageControls(
                available.size, 1, selectedIndex,
                { page -> sensorId = available[page].first }, "sensor-selection"
            )
            DetailCard("Live values",
                if (values.isEmpty()) "Waiting for a sensor event…"
                else values.mapIndexed { i, value ->
                    listOf("X", "Y", "Z", "W").getOrElse(i) { "Value $i" } +
                        ": " + String.format(Locale.US, "%.3f", value)
                }.joinToString("\n")
            )
            DetailCard(
                "Sensor metadata",
                "Units: " + (selected?.let { unitForSensor(it.type) } ?: "Unavailable") +
                    "\nResolution: " + (selected?.resolution?.toString() ?: "Unavailable") +
                    "\nAccuracy: " + (accuracy?.toString() ?: "Unavailable") +
                    "\nVendor: " + (selected?.vendor ?: "Unavailable")
            )
        }
    }
}

private fun unitForSensor(type: Int): String = when(type) {
    Sensor.TYPE_ACCELEROMETER -> "m/s²"
    Sensor.TYPE_GYROSCOPE -> "rad/s"
    Sensor.TYPE_MAGNETIC_FIELD -> "µT"
    Sensor.TYPE_LIGHT -> "lux"
    Sensor.TYPE_PROXIMITY -> "cm"
    Sensor.TYPE_PRESSURE -> "hPa"
    Sensor.TYPE_RELATIVE_HUMIDITY -> "%"
    Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
    else -> "Device dependent"
}

@Composable
fun NetworkDetailsScreen() {
    val context = LocalContext.current
    val cm = remember(context) {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }
    val network = runCatching { cm?.activeNetwork }.getOrNull()
    val caps = runCatching { network?.let { cm?.getNetworkCapabilities(it) } }.getOrNull()
    val transport = when {
        caps == null -> "No active network"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        else -> "Other transport"
    }
    val entries = listOf(
        "Transport" to transport,
        "Validated Internet" to (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ?.let { if (it) "Yes" else "No" } ?: "Unavailable"),
        "Metered network" to (if (network == null || cm == null) "Unavailable" else
            runCatching { if (cm.isActiveNetworkMetered) "Yes" else "No" }.getOrDefault("Unavailable")),
        "Reported downstream bandwidth" to
            (caps?.linkDownstreamBandwidthKbps?.takeIf { it > 0 }?.toString()?.plus(" Kbps") ?: "Unavailable")
    )
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("network-details-page")) {
        val perPage = if (maxHeight < 550.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(entries.size, perPage) - 1)
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Network status", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Reads connection state only. No internet request or speed test is performed.",
                color = Muted)
            pageItems(entries, page, perPage).forEach { (label, value) ->
                DetailCard(label, value)
            }
            PageControls(entries.size, perPage, page, { requestedPage = it }, "network-details")
            Text("Link bandwidth is an Android estimate, not a measured transfer speed.",
                color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
fun AppUsageScreen() {
    val context = LocalContext.current
    val appOps = remember(context) { context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager }
    val usage = remember(context) { context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager }
    var refresh by remember { mutableStateOf(0) }
    var showUsageDisclosure by remember { mutableStateOf(false) }
    val allowed = remember(refresh) { hasUsageAccess(appOps, context) }
    val top by produceState<List<Pair<String, Long>>>(
        initialValue = emptyList(), key1 = refresh, key2 = allowed
    ) {
        value = if (!allowed) emptyList() else withContext(Dispatchers.IO) {
            runCatching {
                val now = System.currentTimeMillis()
                usage?.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    now - 24L * 60 * 60 * 1000, now
                ).orEmpty()
                    .filter { it.totalTimeInForeground > 0 }
                    .groupBy { it.packageName }
                    .map { (pkg, events) -> pkg to events.sumOf { it.totalTimeInForeground } }
                    .sortedByDescending { it.second }
                    .take(15)
            }.getOrDefault(emptyList())
        }
    }
    if (showUsageDisclosure) UsageAccessConsentDialog(
        dismiss = { showUsageDisclosure = false },
        approve = {
            showUsageDisclosure = false
            openUsageSettings(context)
        }
    )
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(Modifier.fillMaxSize().testTag("app-usage-page")) {
        val perPage = if (maxHeight < 570.dp) 1 else 2
        val page = requestedPage.coerceIn(0, pageCount(top.size, perPage) - 1)
        Column(Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("App usage — last 24 hours", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Optional Usage Access. Statistics stay on this device.", color = Muted)
            if (!allowed) {
                DetailCard("Permission required", "Allow Usage Access in Android settings.")
                Button(onClick = { showUsageDisclosure = true }) {
                    Text("Open Usage Access settings")
                }
            } else {
                Button(onClick = { requestedPage = 0; refresh++ }) { Text("Refresh usage") }
                if (top.isEmpty()) {
                    DetailCard("No readings", "Android returned no usage data for this period.")
                }
                pageItems(top, page, perPage).forEach { (pkg, ms) ->
                    DetailCard(pkg, "${ms / 60000L} minutes in foreground")
                }
                PageControls(top.size, perPage, page, { requestedPage = it }, "app-usage")
            }
            Text("Package names are shown without requesting extra app discovery access.",
                fontSize = 12.sp, color = Muted)
        }
    }
}

private fun hasUsageAccess(appOps: AppOpsManager?, context: Context): Boolean {
    if (appOps == null) return false
    return runCatching {
        val mode = if (Build.VERSION.SDK_INT >= 29)
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        else appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        mode == AppOpsManager.MODE_ALLOWED
    }.getOrDefault(false)
}

@Composable
fun AlertsScreen(snapshot: DeviceSnapshot) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("systivra_alerts", Context.MODE_PRIVATE)
    }
    var enabled by remember { mutableStateOf(prefs.getBoolean("enabled", true)) }
    var threshold by remember { mutableFloatStateOf(prefs.getFloat("battery", 20f).coerceIn(5f, 40f)) }
    val charge = snapshot.batteryPercentage()?.toFloat()
    val thermal = snapshot.thermalStatusText()
    val warning = when {
        !enabled -> "On-screen alerts disabled"
        thermal in listOf("Severe", "Critical", "Emergency", "Shutdown") -> "Android thermal warning: " + thermal
        charge != null && charge <= threshold -> "Low battery: " + charge + "%"
        else -> "No threshold warnings at this time"
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("alerts-control-page").padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Live Alerts", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Local in-app warnings only. No background monitoring or notifications.",
            color = Muted, fontSize = 13.sp)
        Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enable on-screen warnings")
                Switch(checked = enabled, onCheckedChange = {
                    enabled = it
                    prefs.edit().putBoolean("enabled", it).apply()
                })
                Text("Battery threshold: ${threshold.toInt()}%", color = Cyan)
                Slider(
                    value = threshold, valueRange = 5f..40f, steps = 6, enabled = enabled,
                    onValueChange = {
                        threshold = it
                        prefs.edit().putFloat("battery", it).apply()
                    }
                )
            }
        }
        DetailCard("Current status", warning)
        DetailCard("Android thermal state", thermal ?: "Unavailable")
    }
}

@Composable
fun PermissionCenterScreen() {
    val context = LocalContext.current
    val ops = remember(context) { context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager }
    var refresh by remember { mutableStateOf(0) }
    var showUsageDisclosure by remember { mutableStateOf(false) }
    val usageAllowed = remember(refresh) { hasUsageAccess(ops, context) }
    if (showUsageDisclosure) UsageAccessConsentDialog(
        dismiss = { showUsageDisclosure = false },
        approve = {
            showUsageDisclosure = false
            openUsageSettings(context)
        }
    )
    var details by rememberSaveable { mutableStateOf(false) }
    if (details) {
        AlertDialog(
            containerColor = Raised, titleContentColor = TextPrimary, textContentColor = TextPrimary,
            onDismissRequest = { details = false },
            title = { Text("Permissions and privacy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monitoring uses Android system APIs without dangerous runtime permissions.")
                    Text("Network state is a normal system permission; INTERNET is not requested.")
                    Text("Camera, microphone and cross-app overlays are not requested.")
                }
            },
            confirmButton = {
                TextButton(onClick = { details = false }) { Text("Close") }
            }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("permission-controls-page").padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Permission Center", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        DetailCard("App Usage Access", if (usageAllowed) "Allowed" else "Not granted")
        Button(onClick = { showUsageDisclosure = true }) { Text("Manage Usage Access") }
        Button(onClick = { refresh++ }) { Text("Refresh status") }
        TextButton(onClick = { details = true }) { Text("Review other permissions") }
        Text("Device monitoring remains usable without optional Usage Access.",
            color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun DetailCard(title: String, content: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Cyan, fontSize = 16.sp)
            HorizontalDivider(color = Raised)
            Text(content, fontSize = 13.sp, color = Muted)
        }
    }
}

private fun openUsageSettings(context: Context) {
    runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
}

@Composable
private fun UsageAccessConsentDialog(dismiss: () -> Unit, approve: () -> Unit) {
    AlertDialog(
            // Never allow inherited dark text on a midnight dialog surface.
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
        onDismissRequest = dismiss,
        title = { Text("Optional app usage access") },
        text = {
            Text(
                "SYSTIVRA can read which apps were in the foreground and for how long. " +
                    "Access is optional and used only to display local app activity. " +
                    "You can revoke it in Android Settings at any time. No usage data is uploaded. " +
                    "Core hardware monitoring functions without this access."
            )
        },
        confirmButton = { TextButton(onClick = approve) { Text("Continue to Settings") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("Not now") } }
    )
}
