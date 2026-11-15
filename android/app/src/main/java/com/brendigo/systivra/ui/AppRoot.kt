package com.brendigo.systivra.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
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
    "session-insights" to "Session Insights",
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
    "recording-library" to "Recording Library",
    "all-screens" to "Screen Catalog",
    "component-test-interactive" to "Component Test Controls",
    "device-capacity" to "Memory & Storage",
    "cpu-cores-data" to "Processor Information",
    "app-usage-controls" to "Usage Controls",
    "session-analysis-data" to "Session Measurements",
    "benchmark-running-controls" to "CPU Test Controls",
    "benchmark-results-data" to "Benchmark Measurements",
    "recording-session-controls" to "Recording Controls",
    "permission-center-controls" to "Permission Controls",
    "system-health-data" to "Health Readings",
    "privacy-center-data" to "Privacy Details",
    "sensor-live-controls" to "Live Sensor Controls",
    "metric-fullscreen-data" to "Detailed Battery Timeline",
    "benchmark-center-controls" to "Performance Lab Controls"
)

private val enabledRoutes = setOf(
    "dashboard", "hardware-explorer", "cpu-details", "gpu-details",
    "memory-details", "storage-details", "battery-details", "thermal-details",
    "display-details", "device-and-android", "sensor-explorer", "live-monitor",
    "diagnostics-center", "component-test", "history", "settings",
    "about", "device-detection", "system-overview",
    "system-health", "privacy-center", "cpu-cores", "charging-analysis", "metric-fullscreen", "reports-export",
    "sensor-live", "network-details", "app-usage", "alerts", "permission-center",
    "recording-session", "session-analysis", "session-insights", "benchmark-center", "benchmark-running", "benchmark-results", "diagnostic-results",
    "overlay-configuration", "recording-library", "all-screens",
    "component-test-interactive", "device-capacity",
    "cpu-cores-data", "app-usage-controls", "session-analysis-data",
    "benchmark-running-controls", "benchmark-results-data",
    "recording-session-controls", "permission-center-controls",
    "system-health-data", "privacy-center-data", "sensor-live-controls",
    "metric-fullscreen-data", "benchmark-center-controls"
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
        val selectedTab = navigationStack.firstOrNull()?.takeIf { entry -> tabs.any { it.first == entry } } ?: "dashboard"
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
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1F3F), Color(0xFF08111D))
                )
            ),
            containerColor = Color.Transparent,
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().height(62.dp)
                        .background(Color(0xFF0D192B)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { (id, label, _) ->
                        Box(
                            modifier = Modifier.weight(1f).fillMaxSize()
                                .clickable { open(id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                color = if (selectedTab == id)
                                    Cyan else Muted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (route !in setOf(
                    "dashboard", "hardware-explorer", "live-monitor", "diagnostics-center",
                    "settings", "about", "device-detection", "component-test",
                    "history", "recording-session", "permission-center"
                ) && route !in detailReferences.keys && route !in priorityReferenceSpecs.keys && route !in nextPriorityReferences.keys) {
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
                    "system-health" -> NextPriorityReferenceScreen(
                        "system-health", snapshot, samples, benchmark,
                        onStartBenchmark = {}, open = ::open
                    )
                    "system-health-data" -> SystemHealth(snapshot, ::open)
                    "privacy-center" -> NextPriorityReferenceScreen(
                        "privacy-center", snapshot, samples, benchmark,
                        onStartBenchmark = {}, open = ::open
                    )
                    "privacy-center-data" -> PrivacyCenter()
                    "cpu-cores" -> PriorityReferenceScreen("cpu-cores", snapshot, recorded, benchmark, ::open)
                    "cpu-cores-data" -> CpuCoreInspector(snapshot)
                    "charging-analysis" -> ChargingAnalysis(snapshot, samples)
                    "metric-fullscreen" -> NextPriorityReferenceScreen(
                        "metric-fullscreen", snapshot, samples, benchmark,
                        onStartBenchmark = {}, open = ::open
                    )
                    "metric-fullscreen-data" -> FullscreenMetric(samples)
                    "reports-export" -> ReportExportScreen(if (selectedSessionId == null) recorded else selectedSessionSamples)
                    "sensor-live" -> NextPriorityReferenceScreen(
                        "sensor-live", snapshot, samples, benchmark,
                        onStartBenchmark = {}, open = ::open
                    )
                    "sensor-live-controls" -> SensorLiveScreen()
                    "network-details" -> NetworkDetailsScreen()
                    "app-usage" -> PriorityReferenceScreen("app-usage", snapshot, recorded, benchmark, ::open)
                    "app-usage-controls" -> AppUsageScreen()
                    "alerts" -> AlertsScreen(snapshot)
                    "permission-center" -> PermissionReferenceScreen(::open)
                    "permission-center-controls" -> PermissionCenterScreen()
                    "recording-session" -> RecordingReferenceScreen(
                        recorded, savedSessions, isRecording, historyReady, historyStorageError,
                        onToggleRecording, ::open
                    )
                    "recording-session-controls" -> RecordingSessionScreen(
                        recorded, isRecording, historyReady, historyStorageError,
                        onToggleRecording, onClearRecording, ::open
                    )
                    "recording-library" -> RecordingLibraryScreen(
                        savedSessions, isRecording, selectedSessionId, onSelectSession, onDeleteSession, ::open
                    )
                    "session-analysis" -> PriorityReferenceScreen(
                        "session-analysis", snapshot,
                        if (selectedSessionId == null) recorded else selectedSessionSamples,
                        benchmark, ::open
                    )
                    "session-analysis-data" -> SessionAnalysisScreen(
                        if (selectedSessionId == null) recorded else selectedSessionSamples, ::open
                    )
                    "session-insights" -> SessionInsightsScreen(if (selectedSessionId == null) recorded else selectedSessionSamples, ::open)
                    "benchmark-center" -> NextPriorityReferenceScreen(
                        "benchmark-center", snapshot, samples, benchmark,
                        onStartBenchmark = {
                            benchmark.start(benchmarkScope)
                            open("benchmark-running")
                        },
                        open = ::open
                    )
                    "benchmark-center-controls" -> BenchmarkCenterScreen(
                        benchmark, onStart = {
                            benchmark.start(benchmarkScope)
                            open("benchmark-running")
                        }
                    )
                    "benchmark-running" -> PriorityReferenceScreen(
                        "benchmark-running", snapshot, recorded, benchmark, ::open
                    )
                    "benchmark-results" -> PriorityReferenceScreen(
                        "benchmark-results", snapshot, recorded, benchmark, ::open
                    )
                    "benchmark-running-controls" -> BenchmarkRunningScreen(benchmark, ::open)
                    "benchmark-results-data" -> BenchmarkResultsScreen(benchmark)
                    "diagnostic-results" -> DiagnosticResultsScreen()
                    "overlay-configuration" -> OverlayPreviewScreen(snapshot)
                    "hardware-explorer" -> HardwareExplorer(::open)
                    "live-monitor" -> LiveMonitor(snapshot, samples, recorded, savedSessions, ::open)
                    "diagnostics-center" -> Diagnostics(::open)
                    "component-test" -> ReferenceLanding(
                        route = "component-test", title = "Component Test",
                        subtitle = "Interactive testing", caption = "Explicit user confirmation",
                        actions = listOf(
                            ReferenceAction("Run test", "component-test-interactive"),
                            ReferenceAction("Check result", "diagnostic-results"),
                            ReferenceAction("Skip test", "diagnostics-center"),
                            ReferenceAction("Permission state", "permission-center")
                        ), open = ::open
                    )
                    "component-test-interactive" -> ComponentTest()
                    "history" -> ReferenceHistory(samples, savedSessions, onSelectSession, ::open)
                    "settings" -> Settings(::open)
                    "about" -> About(::open)
                    "all-screens" -> ScreenCatalog(::open)

                    "cpu-details" -> ReferenceDetailScreen("cpu-details", snapshot.cpu, samples, ::open)

                    "gpu-details" -> ReferenceDetailScreen("gpu-details", snapshot.gpu, samples, ::open)

                    "memory-details" -> ReferenceDetailScreen("memory-details", snapshot.ram, samples, ::open)

                    "storage-details" -> ReferenceDetailScreen("storage-details", snapshot.storage, samples, ::open)

                    "battery-details" -> ReferenceDetailScreen("battery-details", snapshot.battery, samples, ::open)

                    "thermal-details" -> ReferenceDetailScreen("thermal-details", snapshot.thermal, samples, ::open)

                    "display-details" -> ReferenceDetailScreen("display-details", snapshot.display, samples, ::open)
                    "device-and-android" -> ReferenceDetailScreen("device-and-android", snapshot.system, samples, ::open)
                    "device-detection" -> ReferenceLanding(
                        route = "device-detection", title = "Device Detection",
                        subtitle = "Hardware capabilities", caption = "Supported Android APIs",
                        actions = listOf(
                            ReferenceAction("Device model", "device-and-android"),
                            ReferenceAction("Memory and storage", "device-capacity"),
                            ReferenceAction("Available sensors", "sensor-explorer")
                        ), open = ::open
                    )
                    "device-capacity" -> MetricScreen(snapshot.ram + snapshot.storage, "Device RAM and internal storage")
                    "system-overview" -> MetricScreen(snapshot.system, "Device identifiers and Android version")

                    "sensor-explorer" -> ReferenceDetailScreen("sensor-explorer", snapshot.sensors, samples, ::open)
                    else -> Planned(sections.firstOrNull { it.first == route }?.second ?: "Feature")
                }
            }
        }
    }
}

@Composable
private fun Dashboard(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    open: (String) -> Unit
) {
    val primary = listOf(
        Triple("Memory", "◇", "memory-details"),
        Triple("Storage", "▤", "storage-details"),
        Triple("Battery", "◉", "battery-details"),
        Triple("Thermal status", "⌁", "thermal-details")
    )
    LazyColumn(
        modifier = Modifier.testTag("dashboard-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Dashboard", "Your device at a glance", "Live information") }
        item { ReferenceHeroCard { open("device-and-android") } }
        item { Spacer(Modifier.height(0.dp)) }
        items(primary) { (label, symbol, path) ->
            ReferenceMetricRow(label, symbol, primary.indexOfFirst { it.first == label }) {
                open(path)
            }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item {
            Text(
                "More tools", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item { LinkCard("Hardware explorer", "CPU, GPU, sensors and device properties") { open("hardware-explorer") } }
        item { LinkCard("Monitoring", "Real device readings from Android") { open("live-monitor") } }
        item { LinkCard("Charging analysis", "Recorded battery data") { open("charging-analysis") } }
        item { LinkCard("CPU core inspector", "Supported cores and available metrics") { open("cpu-cores") } }
        item { LinkCard("Diagnostics", "User-confirmed, on-device checks") { open("diagnostics-center") } }
        item { LinkCard("Session insights", "Analyze your saved readings") { open("session-insights") } }
        item { LinkCard("Storage and memory", "Details of actual device capacity") { open("storage-details") } }
        item {
            Text(
                "Real measurements are displayed on detail screens only when Android reports them.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}

@Composable
internal fun ReferenceHeroCard(open: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(128.dp).clickable { open() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152235)),
        border = BorderStroke(1.dp, Color(0xFF233857))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Canvas(Modifier.size(76.dp)) {
                val ringWidth = 11.dp.toPx()
                drawCircle(
                    color = Color(0xFF223E60),
                    radius = size.minDimension / 2 - ringWidth / 2,
                    style = Stroke(width = ringWidth)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(Cyan, Color(0xFF5975F5), Purple, Cyan)),
                    startAngle = -90f,
                    sweepAngle = 113f,
                    useCenter = false,
                    style = Stroke(width = ringWidth, cap = StrokeCap.Round)
                )
            }
            Column {
                Text("SYSTIVRA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("System intelligence", fontSize = 13.sp, color = Muted)
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
        modifier = Modifier.fillMaxWidth().height(69.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152235)),
        border = BorderStroke(1.dp, Color(0xFF223654))
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(34.dp).background(tone, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(glyph, color = glyphColor, fontSize = 18.sp)
            }
            Column(
                Modifier.weight(1f).padding(start = 13.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Details / availability", fontSize = 12.sp, color = Muted)
            }
            Text("›", color = Muted, fontSize = 22.sp)
        }
    }
}

@Composable
private fun HardwareExplorer(open: (String) -> Unit) {
    val primary = listOf(
        Triple("CPU and SoC", "◇", "cpu-details"),
        Triple("GPU", "▤", "gpu-details"),
        Triple("Memory", "◉", "memory-details"),
        Triple("Sensors", "⌁", "sensor-explorer")
    )
    LazyColumn(
        modifier = Modifier.testTag("hardware-explorer-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Hardware Explorer", "Explore components", "Hardware information") }
        item { ReferenceHeroCard { open("device-and-android") } }
        item { Spacer(Modifier.height(0.dp)) }
        items(primary) { (name, symbol, path) ->
            ReferenceMetricRow(name, symbol, primary.indexOfFirst { it.first == name }) {
                open(path)
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
        item { Text("Additional hardware", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item { LinkCard("Storage", "Read available and total storage") { open("storage-details") } }
        item { LinkCard("Battery", "Charge and temperature values") { open("battery-details") } }
        item { LinkCard("Thermal", "Android-reported thermal status") { open("thermal-details") } }
        item { LinkCard("Display", "Resolution, refresh rate and density") { open("display-details") } }
        item { LinkCard("Device & Android", "Read platform identifiers and OS version") { open("device-and-android") } }
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
private fun LiveMonitor(
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>,
    recorded: List<DeviceSnapshot>,
    savedSessions: List<RecordedSession>,
    open: (String) -> Unit
) {
    val history = samples.mapNotNull {
        it.battery.firstOrNull()?.value?.removeSuffix("%")?.toFloatOrNull()
            ?.takeIf { value -> value in 0f..100f }
    }
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
private fun Diagnostics(open: (String) -> Unit) {
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
private fun ComponentTest() {
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
                    Button(enabled = speakerWasPlayed, onClick = {
                        speakerVerdict = "Pass (user-confirmed)"
                        verdictPrefs.edit().putString("speaker", speakerVerdict).apply()
                    }) { Text("Pass") }
                    Button(enabled = speakerWasPlayed, onClick = {
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
        context.getSharedPreferences("systivra_settings", Context.MODE_PRIVATE)
    }
    var samplingSeconds by rememberSaveable {
        mutableIntStateOf(preferences.getInt("sampling_seconds", 2).coerceIn(1, 10))
    }
    var activeDialog by rememberSaveable { mutableStateOf<String?>(null) }

    if (activeDialog != null) {
        AlertDialog(
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
                    "Units" -> Text("Measurements use the units supplied by Android. Unit conversion is not currently supported, so no nonfunctional selector is shown.")
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) { Text("Close") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.testTag("settings-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("Settings", "Preferences", "Customize your experience") }
        item { ReferenceHeroCard { open("device-and-android") } }
        item { Spacer(Modifier.height(0.dp)) }
        item { ReferenceMetricRow("Appearance", "◇", 0) { activeDialog = "Appearance" } }
        item { ReferenceMetricRow("Update interval", "▤", 1) { activeDialog = "Update interval" } }
        item { ReferenceMetricRow("Units", "◉", 2) { activeDialog = "Units" } }
        item { ReferenceMetricRow("Privacy", "⌁", 3) { open("privacy-center") } }
        item { Spacer(Modifier.height(116.dp)) }
        item { Text("More tools", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        item { LinkCard("Reports · CSV & JSON", "Export locally recorded readings") { open("reports-export") } }
        item { LinkCard("Session Insights", "Battery trends and measured thermal statistics") { open("session-insights") } }
        item { LinkCard("Recording Library", "View, export or delete selected recordings") { open("recording-library") } }
        item { LinkCard("System Health", "Measurement availability, not a fabricated score") { open("system-health") } }
        item { LinkCard("Privacy Center", "Review access and local data policy") { open("privacy-center") } }
        item { LinkCard("Screen catalog", "Browse available screens and tools") { open("all-screens") } }
        item { LinkCard("Hardware Explorer", "See device data") { open("hardware-explorer") } }
        item { LinkCard("Network state", "See active connection without network requests") { open("network-details") } }
        item { LinkCard("App Usage", "Optional system Usage Access") { open("app-usage") } }
        item { LinkCard("Alerts", "Local battery and thermal thresholds") { open("alerts") } }
        item { LinkCard("Permission Center", "See granted Android access") { open("permission-center") } }
        item { LinkCard("About SYSTIVRA", "Version, privacy and source limitations") { open("about") } }
    }
}

@Composable
private fun ScreenCatalog(open: (String) -> Unit) {
    LazyColumn(modifier = Modifier.testTag("screen-catalog-scroll"), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Available screens and device tools", color = Muted) }
        items(sections.filter { enabledRoutes.contains(it.first) }) { (id, label) ->
            LinkCard(label, "Open $label") {
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
private fun About(open: (String) -> Unit) {
    val context = LocalContext.current
    var browserError by remember { mutableStateOf(false) }
    fun openBrowser(url: String) {
        browserError = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.isFailure
    }

    LazyColumn(
        modifier = Modifier.testTag("about-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("About SYSTIVRA", "Version and licenses", "Open development") }
        item { ReferenceHeroCard { open("device-and-android") } }
        item { Spacer(Modifier.height(0.dp)) }
        item { ReferenceMetricRow("Source code", "◇", 0) { openBrowser("https://github.com/bren-wp/SYSTIVRA") } }
        item { ReferenceMetricRow("MIT license", "▤", 1) { openBrowser("https://github.com/bren-wp/SYSTIVRA/blob/main/LICENSE") } }
        item { ReferenceMetricRow("Device support", "◉", 2) { open("device-and-android") } }
        item { ReferenceMetricRow("Privacy", "⌁", 3) { open("privacy-center") } }
        if (browserError) {
            item { Text("No browser could open the link. Check that a web browser is installed.", color = Muted) }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item {
            PanelCard {
                Text("SYSTIVRA by BRENDIGO", color = Cyan, fontWeight = FontWeight.Bold)
                Text("Version 0.10.0-preview.2 · MIT License · © 2026 BRENDIGO", fontSize = 12.sp, color = Muted)
                Text("Privacy-first Android system monitor and diagnostics", color = Muted)
            }
        }
        item {
            PanelCard {
                Text("Transparent measurements", fontWeight = FontWeight.Bold)
                Text("Unsupported CPU or GPU temperatures and other restricted Android values are shown as Unavailable.", color = Muted)
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
