package com.systivra.app.ui

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Slider
import kotlin.math.roundToInt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.FactCheck
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systivra.app.data.RecordedSession
import com.systivra.app.data.DeviceSnapshot
import com.systivra.app.data.Reading
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val tabs = listOf(
    Triple("dashboard", "Dashboard", "▣"),
    Triple("live-monitor", "Monitor", "⌁"),
    Triple("diagnostics-center", "Tests", "◇"),
    Triple("settings", "More", "☷")
)

private val sections = listOf(
    "dashboard" to "Dashboard",
    "hardware-explorer" to "Hardware Explorer",
    "system-health" to "System Health",
    "privacy-center" to "Privacy Center",
    "cpu-details" to "CPU Details",
    "cpu-cores" to "CPU Core Inspector",
    "gpu-details" to "GPU Details",
    "memory-details" to "Memory Details",
    "storage-details" to "Storage Details",
    "battery-details" to "Battery Details",
    "charging-analysis" to "Charging Analysis",
    "thermal-details" to "Thermal Details",
    "display-details" to "Display Details",
    "device-and-android" to "Device & Android",
    "network-details" to "Network Details",
    "sensor-explorer" to "Sensor Explorer",
    "sensor-live" to "Sensor Live View",
    "app-usage" to "App Usage",
    "live-monitor" to "Live Monitor",
    "metric-fullscreen" to "Metric Fullscreen",
    "recording-session" to "Recording Session",
    "session-analysis" to "Session Analysis",
    "alerts" to "Alerts",
    "diagnostics-center" to "Diagnostics Center",
    "component-test" to "Component Test",
    "diagnostic-results" to "Diagnostic Results",
    "benchmark-center" to "Benchmark Center",
    "benchmark-running" to "Benchmark Running",
    "benchmark-results" to "Benchmark Results",
    "overlay-configuration" to "Overlay Configuration",
    "history" to "History",
    "reports-export" to "Reports & Export",
    "permission-center" to "Permission Center",
    "settings" to "Settings",
    "about" to "About SYSTIVRA",
    "device-detection" to "Device Detection",
    "system-overview" to "System Overview",
    "recording-library" to "Recording Library"
)

private val enabledRoutes = setOf(
    "dashboard", "hardware-explorer", "cpu-details", "gpu-details",
    "memory-details", "storage-details", "battery-details", "thermal-details",
    "display-details", "device-and-android", "sensor-explorer", "live-monitor",
    "diagnostics-center", "component-test", "history", "settings",
    "about", "device-detection", "system-overview",
    "system-health", "privacy-center", "cpu-cores", "charging-analysis", "metric-fullscreen", "reports-export",
    "sensor-live", "network-details", "app-usage", "alerts", "permission-center",
    "recording-session", "session-analysis", "benchmark-center", "benchmark-running", "benchmark-results", "diagnostic-results",
    "overlay-configuration", "recording-library"
)

@Composable
fun AppRoot(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    recorded: List<DeviceSnapshot>,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    onClearRecording: () -> Unit,
    initialRoute: String? = null,
    historyReady: Boolean = true,
    historyStorageError: String? = null,
    savedSessions: List<RecordedSession> = emptyList(),
    selectedSessionId: Long? = null,
    selectedSessionSamples: List<DeviceSnapshot> = emptyList(),
    onSelectSession: (Long?) -> Unit = {},
    onDeleteSession: (Long) -> Unit = {}
) {
    AppTheme {
        val appContext = LocalContext.current.applicationContext
        val benchmark = remember { BenchmarkController(appContext) }
        val benchmarkScope = rememberCoroutineScope()
        var navigationStack by rememberSaveable {
            mutableStateOf(
                arrayListOf(initialRoute?.takeIf { id -> sections.any { it.first == id } } ?: "dashboard")
            )
        }
        val route = navigationStack.lastOrNull() ?: "dashboard"
        fun open(id: String) {
            if (route == id) return
            navigationStack = if (tabs.any { it.first == id }) {
                arrayListOf(id)
            } else {
                ArrayList(navigationStack).apply { add(id) }
            }
        }
        fun goBack() {
            navigationStack = if (navigationStack.size > 1) {
                ArrayList(navigationStack.dropLast(1))
            } else {
                arrayListOf("dashboard")
            }
        }
        BackHandler(enabled = navigationStack.size > 1) { goBack() }
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF101B2D)) {
                    tabs.forEach { (id, label, symbol) ->
                        NavigationBarItem(
                            selected = route == id,
                            onClick = { open(id) },
                            icon = {
                                Icon(imageVector = when (id) {
                                    "dashboard" -> Icons.Rounded.Dashboard
                                    "live-monitor" -> Icons.Rounded.ShowChart
                                    "diagnostics-center" -> Icons.Rounded.FactCheck
                                    else -> Icons.Rounded.Tune
                                }, contentDescription = label, modifier = Modifier.size(24.dp))
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Purple,
                                selectedTextColor = Purple,
                                indicatorColor = Color(0xFF27224B),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (route != "dashboard") {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("‹", Modifier.clickable { goBack() }.padding(end = 14.dp),
                            fontSize = 30.sp, color = Cyan)
                        Text(
                            sections.firstOrNull { it.first == route }?.second ?: "SYSTIVRA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                when (route) {
                    "dashboard" -> Dashboard(snapshot, samples, ::open)
                    "system-health" -> SystemHealth(snapshot, ::open)
                    "privacy-center" -> PrivacyCenter()
                    "cpu-cores" -> CpuCoreInspector(snapshot)
                    "charging-analysis" -> ChargingAnalysis(snapshot, samples)
                    "metric-fullscreen" -> FullscreenMetric(samples)
                    "reports-export" -> ReportExportScreen(if (selectedSessionId == null) recorded else selectedSessionSamples)
                    "sensor-live" -> SensorLiveScreen()
                    "network-details" -> NetworkDetailsScreen()
                    "app-usage" -> AppUsageScreen()
                    "alerts" -> AlertsScreen(snapshot)
                    "permission-center" -> PermissionCenterScreen()
                    "recording-session" -> RecordingSessionScreen(recorded, isRecording, historyReady, historyStorageError, onToggleRecording, onClearRecording, ::open)
                    "recording-library" -> RecordingLibraryScreen(
                        savedSessions, isRecording, selectedSessionId, onSelectSession, onDeleteSession, ::open
                    )
                    "session-analysis" -> SessionAnalysisScreen(if (selectedSessionId == null) recorded else selectedSessionSamples, ::open)
                    "benchmark-center" -> BenchmarkCenterScreen(benchmark, onStart = {
                        benchmark.start(benchmarkScope)
                        open("benchmark-running")
                    })
                    "benchmark-running" -> BenchmarkRunningScreen(benchmark, ::open)
                    "benchmark-results" -> BenchmarkResultsScreen(benchmark)
                    "diagnostic-results" -> DiagnosticResultsScreen()
                    "overlay-configuration" -> OverlayPreviewScreen(snapshot)
                    "hardware-explorer" -> HardwareExplorer(::open)
                    "live-monitor" -> LiveMonitor(snapshot, samples, recorded, savedSessions, ::open)
                    "diagnostics-center" -> Diagnostics(::open)
                    "component-test" -> ComponentTest()
                    "history" -> History(samples, ::open)
                    "settings" -> Settings(::open)
                    "about" -> About()
                    "all-screens" -> ScreenCatalog(::open)
                    "cpu-details" -> MetricScreen(snapshot.cpu, "Platform processor capabilities")
                    "gpu-details" -> MetricScreen(snapshot.gpu, "GPU readings are restricted on many devices")
                    "memory-details" -> MetricScreen(snapshot.ram, "Physical RAM reported by Android")
                    "storage-details" -> MetricScreen(snapshot.storage, "Internal data volume")
                    "battery-details" -> MetricScreen(snapshot.battery, "Battery state and charging")
                    "thermal-details" -> MetricScreen(snapshot.thermal, "Thermal state is not CPU temperature")
                    "display-details" -> MetricScreen(snapshot.display, "Current display metrics")
                    "device-and-android", "device-detection", "system-overview" ->
                        MetricScreen(snapshot.system, "Device identifiers and Android version")
                    "sensor-explorer" -> MetricScreen(snapshot.sensors, "Sensors exposed by SensorManager")
                    else -> Planned(sections.firstOrNull { it.first == route }?.second ?: "Feature")
                }
            }
        }
    }
}

@Composable
private fun Dashboard(snapshot: DeviceSnapshot, samples: List<DeviceSnapshot>, open: (String) -> Unit) {
    val cards = listOf(
        Triple("CPU", snapshot.cpu.firstOrNull { it.name == "System-wide CPU load" }?.display ?: "Unavailable", "cpu-details"),
        Triple("RAM", snapshot.ram.getOrNull(1)?.display ?: "Unavailable", "memory-details"),
        Triple("Storage", snapshot.storage.getOrNull(1)?.display ?: "Unavailable", "storage-details"),
        Triple("Battery", snapshot.battery.firstOrNull()?.display ?: "Unavailable", "battery-details")
    )
    LazyColumn(
        modifier = Modifier.testTag("dashboard-scroll"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SYSTIVRA", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                Spacer(Modifier.weight(1f))
                Text("● LIVE", fontSize = 12.sp, color = Mint, fontWeight = FontWeight.Bold)
            }
            Text(snapshot.name, color = Muted, fontSize = 14.sp)
        }
        item {
            PanelCard(onClick = { open("device-and-android") }) {
                Row(horizontalArrangement = Arrangement.spacedBy(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(58.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF128FED), Purple, Color(0xFF172E65))),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(snapshot.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Android " + (snapshot.system.firstOrNull { it.name == "Android" }?.display ?: "Unavailable"),
                            fontSize = 12.sp, color = Muted)
                        Text(if (snapshot.time > 0) "● MONITORING ACTIVE" else "Detecting device…",
                            fontSize = 11.sp, color = Mint)
                    }
                }
                Text("View system information ›", color = Cyan, fontSize = 12.sp)
            }
        }
        items(cards.chunked(2)) { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { (label, value, path) ->
                    Box(Modifier.weight(1f)) {
                        PanelCard(onClick = { open(path) }) {
                            Text(label, color = Muted, fontSize = 13.sp)
                            Text(value, color = Cyan, fontWeight = FontWeight.Bold,
                                fontSize = 19.sp, maxLines = 2)
                            Text("View details ›", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        item {
            val percentage = samples.mapNotNull {
                it.battery.firstOrNull()?.value?.removeSuffix("%")?.toFloatOrNull()
            }
            PanelCard(onClick = { open("metric-fullscreen") }) {
                Text("Battery activity", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Real-time session · last " + percentage.size + " battery readings", color = Muted, fontSize = 12.sp)
                if (percentage.size > 1) {
                    Canvas(Modifier.fillMaxWidth().height(96.dp)) {
                        val step = size.width / (percentage.size - 1)
                        percentage.zipWithNext().forEachIndexed { index, pair ->
                            drawLine(Mint, Offset(step * index, size.height * (1 - pair.first / 100f)),
                                Offset(step * (index + 1), size.height * (1 - pair.second / 100f)),
                                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                } else {
                    Text("Collecting live readings…", color = Muted, fontSize = 12.sp)
                }
                Text("View full chart ›", color = Cyan, fontSize = 12.sp)
            }
        }
        item { LinkCard("Thermal status", snapshot.thermal.firstOrNull()?.display ?: "Unavailable") { open("thermal-details") } }
        item { LinkCard("Hardware explorer", "CPU, GPU, sensors and device properties") { open("hardware-explorer") } }
        item { LinkCard("Monitoring", "Live battery, RAM and thermal readings") { open("live-monitor") } }
        item { LinkCard("Charging analysis", "Compare measured battery readings") { open("charging-analysis") } }
        item { LinkCard("CPU core inspector", "Supported cores and available metrics") { open("cpu-cores") } }
        item { LinkCard("Diagnostics", "Interactive tests, no fake health scores") { open("diagnostics-center") } }
    }
}

@Composable
private fun HardwareExplorer(open: (String) -> Unit) {
    val paths = listOf(
        "cpu-details" to "CPU", "gpu-details" to "GPU",
        "memory-details" to "Memory", "storage-details" to "Storage",
        "battery-details" to "Battery", "thermal-details" to "Thermal",
        "display-details" to "Display", "device-and-android" to "Device & Android",
        "sensor-explorer" to "Sensors"
    )
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Inspect components and their real data sources", color = Muted) }
        items(paths) { (path, title) -> LinkCard(title, "Inspect properties and availability") { open(path) } }
    }
}

@Composable
private fun MetricScreen(readings: List<Reading>, subtitle: String) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(subtitle, fontSize = 13.sp, color = Muted) }
        if (readings.isEmpty()) item { Text("Waiting for device readings…", color = Muted) }
        items(readings) { item ->
            PanelCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text(item.display, color = if (item.value == null) Muted else Cyan,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                }
                Text("Source: " + item.source, fontSize = 11.sp, color = Muted)
            }
        }
    }
}

@Composable
private fun LiveMonitor(snapshot: DeviceSnapshot, samples: List<DeviceSnapshot>, recorded: List<DeviceSnapshot>, savedSessions: List<RecordedSession>, open: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Text("Foreground live monitoring · no fabricated metrics", color = Muted, fontSize = 13.sp) }
        item {
            PanelCard {
                Text("Battery level", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(snapshot.battery.firstOrNull()?.display ?: "Unavailable",
                    color = Mint, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                val values = samples.mapNotNull {
                    it.battery.firstOrNull()?.value?.removeSuffix("%")?.toFloatOrNull()
                }
                if (values.size > 1) {
                    Canvas(Modifier.fillMaxWidth().height(112.dp)) {
                        val dx = size.width / (values.size - 1)
                        values.zipWithNext().forEachIndexed { index, pair ->
                            drawLine(Mint,
                                Offset(index * dx, size.height * (1f - pair.first / 100f)),
                                Offset((index + 1) * dx, size.height * (1f - pair.second / 100f)),
                                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                }
                Text("Battery broadcast · " + values.size + " actual samples",
                    color = Muted, fontSize = 11.sp)
            }
        }
        item { LinkCard("Available RAM", snapshot.ram.getOrNull(1)?.display ?: "Unavailable") { open("memory-details") } }
        item { LinkCard("Android thermal status", snapshot.thermal.firstOrNull()?.display ?: "Unavailable") { open("thermal-details") } }
        item { LinkCard("Live history", "Last " + samples.size + " readings · temporary") { open("history") } }
        item { LinkCard("Recording Library", savedSessions.size.toString() + " saved sessions") { open("recording-library") } }
        item { LinkCard("Session Recorder", "Explicit, foreground-only recording") { open("recording-session") } }
        item { LinkCard("Full-screen chart", "Battery history in focus") { open("metric-fullscreen") } }
        item { LinkCard("Performance HUD", "Customize the in-app overlay preview") { open("overlay-configuration") } }
    }
}

@Composable
private fun Diagnostics(open: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            PanelCard {
                Text("Hardware Diagnostics", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("No automatic healthy verdict. Results are confirmed by the user.",
                    color = Muted, fontSize = 12.sp)
            }
        }
        item { LinkCard("Display & touch test", "Interactive color and touch panel") { open("component-test") } }
        item { LinkCard("Sensors", "Inventory of detected sensor devices") { open("sensor-explorer") } }
        item { LinkCard("Live sensor readings", "Read sensor axes and metadata") { open("sensor-live") } }
        item { LinkCard("CPU benchmark", "Bounded workload with thermal cutoff") { open("benchmark-center") } }
        item { LinkCard("Diagnostic results", "Locally confirmed display/touch test") { open("diagnostic-results") } }
        item { LinkCard("Thermal status", "Android platform status, where supported") { open("thermal-details") } }
        item { LinkCard("Other planned tests", "Full development inventory and status") { open("all-screens") } }
    }
}

@Composable
private fun ComponentTest() {
    val context = LocalContext.current
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
                Text("Vibration test", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    if (hasVibrator) "Trigger a short pulse and confirm whether you feel it."
                    else "This device does not report a vibrator.",
                    color = Muted, fontSize = 13.sp
                )
                Text(vibrationFeedback, color = Muted, fontSize = 12.sp)
                Button(enabled = hasVibrator, onClick = {
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
                    Button(enabled = hasVibrator, onClick = {
                        vibrationVerdict = "Pass (user-confirmed)"
                        verdictPrefs.edit().putString("vibration", vibrationVerdict).apply()
                    }) { Text("Pass") }
                    Button(enabled = hasVibrator, onClick = {
                        vibrationVerdict = "Fail (user-confirmed)"
                        verdictPrefs.edit().putString("vibration", vibrationVerdict).apply()
                    }) { Text("Fail") }
                }
            }
        }
    }
}

@Composable
private fun Settings(open: (String) -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("systivra_settings", android.content.Context.MODE_PRIVATE)
    }
    var samplingSeconds by rememberSaveable {
        mutableIntStateOf(preferences.getInt("sampling_seconds", 2).coerceIn(1, 10))
    }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item {
            PanelCard {
                Text("Appearance", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Dark theme · English", color = Muted)
                Text("Splash → Dashboard · no sign-in or onboarding", color = Muted, fontSize = 12.sp)
            }
        }
        item {
            PanelCard {
                Text("Monitoring", fontWeight = FontWeight.Bold)
                Text("Refresh interval: " + samplingSeconds + " seconds · foreground only", color = Muted)
                Slider(
                    value = samplingSeconds.toFloat(),
                    valueRange = 1f..10f,
                    steps = 8,
                    onValueChange = {
                        samplingSeconds = it.roundToInt().coerceIn(1, 10)
                        preferences.edit().putInt("sampling_seconds", samplingSeconds).apply()
                    }
                )
            }
        }
        item { LinkCard("Reports & CSV export", "Export locally recorded session readings") { open("reports-export") } }
        item { LinkCard("Recording Library", "View, export or delete selected recordings") { open("recording-library") } }
        item { LinkCard("System Health", "Measurement availability, not a fabricated score") { open("system-health") } }
        item { LinkCard("Privacy Center", "Review access and local data policy") { open("privacy-center") } }
        item { LinkCard("All planned screens", "Browse implemented and upcoming destinations") { open("all-screens") } }
        item { LinkCard("Hardware Explorer", "See device data") { open("hardware-explorer") } }
        item { LinkCard("Network state", "See active connection without network requests") { open("network-details") } }
        item { LinkCard("App Usage", "Optional system Usage Access") { open("app-usage") } }
        item { LinkCard("Alerts", "Local battery and thermal thresholds") { open("alerts") } }
        item { LinkCard("Permission Center", "See granted Android access") { open("permission-center") } }
        item { LinkCard("About SYSTIVRA", "Version, privacy and source limitations") { open("about") } }
    }
}

@Composable
private fun History(samples: List<DeviceSnapshot>, open: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("In-memory session history · " + samples.size + " samples", color = Muted)
            Text("Not written to disk. Persistent recording is planned.", color = Muted, fontSize = 12.sp)
        }
        item { LinkCard("Export session as CSV", "Choose a destination using Android Files") { open("reports-export") } }
        items(samples.reversed()) { entry ->
            PanelCard {
                Text(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(entry.time)),
                    color = Cyan, fontWeight = FontWeight.Bold)
                Text("Battery: " + (entry.battery.firstOrNull()?.display ?: "Unavailable"),
                    fontSize = 13.sp)
                Text("Free RAM: " + (entry.ram.getOrNull(1)?.display ?: "Unavailable"),
                    fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ScreenCatalog(open: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Development inventory · planned modules are clearly marked", color = Muted) }
        items(sections) { (id, label) ->
            LinkCard(label, if (enabledRoutes.contains(id)) "Available foundation" else "Planned · not implemented") {
                open(id)
            }
        }
    }
}

@Composable
private fun Planned(title: String) {
    Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
        PanelCard {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text("This module is not implemented yet.", color = Muted)
            Text("No simulated measurements or results are shown.", color = Muted)
        }
    }
}

@Composable
private fun About() {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PanelCard {
                Text("SYSTIVRA", color = Cyan, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("Advanced Device Monitor & Diagnostics", color = Muted)
                Text("Version 0.4.0-preview.1 · MIT License · © 2026 BRENDIGO", fontSize = 12.sp, color = Muted)
            }
        }
        item {
            PanelCard {
                Text("Privacy-first", fontWeight = FontWeight.Bold)
                Text("No account, tracking, network requirement or dangerous permissions.", color = Muted)
            }
        }
        item {
            PanelCard {
                Text("Transparent measurements", fontWeight = FontWeight.Bold)
                Text("CPU and GPU temperatures are not universally readable through Android APIs. Unsupported values are shown as Unavailable.", color = Muted)
            }
        }
    }
}

@Composable
private fun PanelCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
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
private fun LinkCard(title: String, subtitle: String, open: () -> Unit) {
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
