// SYSTIVRA by Brendigo
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import kotlin.math.roundToInt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.brendigo.systivra.BuildConfig
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.measuredChargePercentages
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
    "charging-analysis-data" to "Charging Readings",
    "thermal-details" to "Thermal Details",
    "display-details" to "Display Details",
    "device-and-android" to "Device & Android",
    "network-details" to "Network Details",
    "sensor-explorer" to "Sensor Explorer",
    "cpu-details-readings" to "CPU Readings",
    "gpu-details-readings" to "GPU Readings",
    "memory-details-readings" to "Memory Readings",
    "storage-details-readings" to "Storage Readings",
    "battery-details-readings" to "Battery Readings",
    "thermal-details-readings" to "Thermal Readings",
    "display-details-readings" to "Display Readings",
    "device-and-android-readings" to "Device Readings",
    "sensor-explorer-readings" to "Sensor Readings",
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
    "benchmark-center-controls" to "Performance Lab Controls",
    "network-details-data" to "Network Connection Readings",
    "alerts-controls" to "Live Alerts Controls",
    "reports-export-controls" to "Report Export Controls",
    "overlay-configuration-controls" to "HUD Preview Controls",
    "diagnostic-results-passed" to "Passed Diagnostic Results",
    "diagnostic-results-failed" to "Failed Diagnostic Results",
    "diagnostic-results-skipped" to "Skipped Diagnostic Results",
    "diagnostic-results-unavailable" to "Unavailable Diagnostic Results"
)

private val enabledRoutes = setOf(
    "dashboard", "hardware-explorer", "cpu-details", "gpu-details",
    "memory-details", "storage-details", "battery-details", "thermal-details",
    "display-details", "device-and-android", "sensor-explorer", "live-monitor",
    "cpu-details-readings", "gpu-details-readings", "memory-details-readings",
    "storage-details-readings", "battery-details-readings", "thermal-details-readings",
    "display-details-readings", "device-and-android-readings",
    "sensor-explorer-readings",
    "diagnostics-center", "component-test", "history", "settings",
    "about", "device-detection", "system-overview",
    "system-health", "privacy-center", "cpu-cores", "charging-analysis", "metric-fullscreen", "reports-export",
    "sensor-live", "network-details", "app-usage", "alerts", "permission-center",
    "recording-session", "session-analysis", "session-insights", "benchmark-center", "benchmark-running", "benchmark-results", "diagnostic-results",
    "overlay-configuration", "recording-library", "all-screens",
    "component-test-interactive", "device-capacity",
    "cpu-cores-data", "app-usage-controls", "session-analysis-data",
    "charging-analysis-data",
    "benchmark-running-controls", "benchmark-results-data",
    "recording-session-controls", "permission-center-controls",
    "system-health-data", "privacy-center-data", "sensor-live-controls",
    "metric-fullscreen-data", "benchmark-center-controls",
    "network-details-data", "alerts-controls", "reports-export-controls",
    "overlay-configuration-controls", "diagnostic-results-passed",
    "diagnostic-results-failed", "diagnostic-results-skipped",
    "diagnostic-results-unavailable"
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
    selectedSessionLoading: Boolean = false,
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
        // Preserve the user's chosen export format through the route transition
        // without changing the approved Reports & Export reference screen.
        var reportPreferredFormat by rememberSaveable { mutableStateOf<String?>(null) }
        fun open(id: String) {
            val formatRequest = id.removePrefix("reports-export-controls:")
                .takeIf { id.startsWith("reports-export-controls:") &&
                    it in setOf("csv", "json", "pdf", "share") }
            val destination = if (formatRequest != null) "reports-export-controls" else id
            if (destination == "reports-export-controls") {
                reportPreferredFormat = formatRequest
            }
            if (route == destination) return
            navigationStack = pushNavigationRoute(
                navigationStack, destination, tabs.any { it.first == destination }
            )
        }
        fun goBack() {
            navigationStack = popNavigationRoute(navigationStack)
        }
        BackHandler(enabled = navigationStack.size > 1) { goBack() }
        Scaffold(
            modifier = Modifier.background(ReferenceBackgroundBrush),
            containerColor = Color.Transparent,
            bottomBar = { ReferenceBottomBar(tabs, selectedTab, ::open) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (route !in setOf(
                    "dashboard", "hardware-explorer", "live-monitor", "diagnostics-center",
                    "settings", "about", "device-detection", "component-test",
                    "history", "recording-session", "permission-center",
                    "network-details", "alerts", "diagnostic-results",
                    "overlay-configuration", "reports-export", "charging-analysis"
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
                if (selectedSessionLoading && route in setOf(
                    "reports-export-controls", "session-analysis", "session-analysis-data",
                    "session-insights"
                )) {
                    Box(
                        Modifier.fillMaxSize().testTag("selected-session-loading"),
                        contentAlignment = Alignment.Center
                    ) { Text("Loading selected recording…", color = Muted) }
                } else when (route) {
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
                    "charging-analysis" -> ReferenceChargingAnalysis(snapshot, samples, ::open)
                    "charging-analysis-data" -> ChargingAnalysis(snapshot, samples)
                    "metric-fullscreen" -> NextPriorityReferenceScreen(
                        "metric-fullscreen", snapshot, samples, benchmark,
                        onStartBenchmark = {}, open = ::open
                    )
                    "metric-fullscreen-data" -> FullscreenMetric(samples)
                    "reports-export" -> ReferenceLanding(
                        route = "reports-export", title = "Reports & Export",
                        subtitle = "Export your data", caption = "Choose fields and format",
                        actions = listOf(
                            ReferenceAction("CSV", "reports-export-controls:csv"),
                            ReferenceAction("JSON", "reports-export-controls:json"),
                            ReferenceAction("PDF", "reports-export-controls:pdf"),
                            ReferenceAction("Android Sharesheet", "reports-export-controls:share")
                        ), open = ::open
                    )
                    "reports-export-controls" -> ReportExportScreen(
                        if (selectedSessionId == null) recorded else selectedSessionSamples,
                        preferredFormat = reportPreferredFormat,
                        onStartRecording = { open("recording-session") }
                    )
                    "sensor-live" -> NextPriorityReferenceScreen(
                        "sensor-live", snapshot, samples, benchmark,
                        onStartBenchmark = {}, open = ::open
                    )
                    "sensor-live-controls" -> SensorLiveScreen()
                    "network-details" -> ReferenceLanding(
                        route = "network-details", title = "Network Details",
                        subtitle = "Connection overview", caption = "Available interface details",
                        actions = listOf(
                            ReferenceAction("Connection type", "network-details-data"),
                            ReferenceAction("Network state", "network-details-data"),
                            ReferenceAction("Wi-Fi information", "network-details-data"),
                            ReferenceAction("Availability", "network-details-data")
                        ), open = ::open
                    )
                    "network-details-data" -> NetworkDetailsScreen()
                    "app-usage" -> PriorityReferenceScreen("app-usage", snapshot, recorded, benchmark, ::open)
                    "app-usage-controls" -> AppUsageScreen()
                    "alerts" -> ReferenceLanding(
                        route = "alerts", title = "Alerts",
                        subtitle = "Device notifications", caption = "Configurable thresholds",
                        actions = listOf(
                            ReferenceAction("Battery level", "alerts-controls"),
                            ReferenceAction("Thermal warnings", "alerts-controls"),
                            ReferenceAction("Storage capacity", "storage-details"),
                            ReferenceAction("Notification access", "permission-center")
                        ), open = ::open
                    )
                    "alerts-controls" -> AlertsScreen(snapshot)
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
                    "diagnostic-results" -> ReferenceLanding(
                        route = "diagnostic-results", title = "Diagnostic Results",
                        subtitle = "Test summary", caption = "Only completed tests count",
                        actions = listOf(
                            ReferenceAction("Passed", "diagnostic-results-passed"),
                            ReferenceAction("Failed", "diagnostic-results-failed"),
                            ReferenceAction("Skipped", "diagnostic-results-skipped"),
                            ReferenceAction("Unavailable", "diagnostic-results-unavailable")
                        ), open = ::open
                    )
                    "diagnostic-results-passed" -> DiagnosticResultsScreen(statusFilter = "Passed")
                    "diagnostic-results-failed" -> DiagnosticResultsScreen(statusFilter = "Failed")
                    "diagnostic-results-skipped" -> DiagnosticResultsScreen(statusFilter = "Skipped")
                    "diagnostic-results-unavailable" -> DiagnosticResultsScreen(statusFilter = "Unavailable")
                    "overlay-configuration" -> ReferenceLanding(
                        route = "overlay-configuration", title = "Overlay Configuration",
                        subtitle = "Floating monitor", caption = "Requires Android overlay access",
                        actions = listOf(
                            ReferenceAction("Metric selection", "overlay-configuration-controls"),
                            ReferenceAction("Opacity", "overlay-configuration-controls"),
                            ReferenceAction("Size", "overlay-configuration-controls"),
                            ReferenceAction("Position", "overlay-configuration-controls")
                        ), open = ::open
                    )
                    "overlay-configuration-controls" -> OverlayPreviewScreen(snapshot)
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
                    "all-screens" -> ScreenCatalog(sections, enabledRoutes, ::open)

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
                    "cpu-details-readings" -> DeviceReadingsBrowser("cpu-details", snapshot.cpu)
                    "gpu-details-readings" -> DeviceReadingsBrowser("gpu-details", snapshot.gpu)
                    "memory-details-readings" -> DeviceReadingsBrowser("memory-details", snapshot.ram)
                    "storage-details-readings" -> DeviceReadingsBrowser("storage-details", snapshot.storage)
                    "battery-details-readings" -> DeviceReadingsBrowser("battery-details", snapshot.battery)
                    "thermal-details-readings" -> DeviceReadingsBrowser("thermal-details", snapshot.thermal)
                    "display-details-readings" -> DeviceReadingsBrowser("display-details", snapshot.display)
                    "device-and-android-readings" -> DeviceReadingsBrowser("device-and-android", snapshot.system)
                    "sensor-explorer-readings" -> DeviceReadingsBrowser("sensor-explorer", snapshot.sensors)
                    else -> Planned(sections.firstOrNull { it.first == route }?.second ?: "Feature")
                }
            }
        }
    }
}
