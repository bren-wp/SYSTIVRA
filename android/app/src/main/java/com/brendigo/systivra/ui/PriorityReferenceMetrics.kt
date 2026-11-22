// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.data.analyzeSessionReadings
import com.brendigo.systivra.data.parseBatteryPercent
import java.util.Locale

/** Literal titles, subtitles and four actions from the five original 390×844 SVG boards. */
internal data class PriorityReferenceSpec(
    val title: String,
    val subtitle: String,
    val caption: String,
    val rows: List<String>,
    val chart: Boolean
)

internal val priorityReferenceSpecs = mapOf(
    "benchmark-results" to PriorityReferenceSpec(
        "Benchmark Results", "Performance summary", "Methodology and context",
        listOf("Test version", "Elapsed time", "Thermal state", "Past sessions"), true
    ),
    "app-usage" to PriorityReferenceSpec(
        "App Usage", "Usage statistics", "Optional Android permission",
        listOf("Permission required", "Local statistics", "Time range", "Privacy"), false
    ),
    "session-analysis" to PriorityReferenceSpec(
        "Session Analysis", "Session overview", "Statistics and metadata",
        listOf("Average", "Minimum", "Maximum", "Export"), true
    ),
    "benchmark-running" to PriorityReferenceSpec(
        "Benchmark Running", "Test in progress", "Stop at any time",
        listOf("Elapsed time", "Temperature", "Thermal status", "Stop test"), true
    ),
    "cpu-cores" to PriorityReferenceSpec(
        "CPU Core Inspector", "Core information", "Values vary by device",
        listOf("Core 0", "Core 1", "Core 2", "Core 3"), true
    )
)

private fun fmt(value: Double?, unit: String = ""): String? =
    value?.takeIf(Double::isFinite)?.let { String.format(Locale.US, "%.1f%s", it, unit) }

internal fun priorityChartPoints(
    route: String,
    samples: List<DeviceSnapshot>
): List<Float> = when (route) {
    // No synthetic benchmark progress or per-core data exists in the Android API.
    "session-analysis" -> samples.mapNotNull {
        parseBatteryPercent(it.battery.firstOrNull { b -> b.name == "Charge level" }?.value)
            ?.toFloat()?.takeIf(Float::isFinite)
    }
    else -> emptyList()
}

internal fun priorityRowReading(
    route: String,
    label: String,
    snapshot: DeviceSnapshot,
    recorded: List<DeviceSnapshot>,
    benchmarkResult: CpuBenchResult?,
    running: Boolean,
    benchmarkMessage: String
): Reading {
    val insights = if (route == "session-analysis") analyzeSessionReadings(recorded) else null
    return when (route to label) {
        "benchmark-results" to "Test version" -> Reading(
            label, "SYSTIVRA 2-second single-worker integer test",
            "Local BenchmarkController implementation, not a standardized score"
        )
        "benchmark-results" to "Elapsed time" -> Reading(
            label, benchmarkResult?.elapsedMs?.let { "$it ms" },
            "SystemClock.elapsedRealtimeNanos"
        )
        "benchmark-results" to "Thermal state" -> Reading(
            label, benchmarkResult?.thermalStatus,
            "Android PowerManager during completed test"
        )
        "benchmark-results" to "Past sessions" -> Reading(
            label, "Benchmark results are not persisted in this version",
            "Volatile BenchmarkController state"
        )
        "app-usage" to "Permission required" -> Reading(
            label, "Optional Usage Access; check current permission in the detailed view",
            "Android AppOpsManager"
        )
        "app-usage" to "Local statistics" -> Reading(
            label, "Available only if Android grants Usage Access",
            "UsageStatsManager, on-device processing"
        )
        "app-usage" to "Time range" -> Reading(
            label, "Last 24 hours", "UsageStatsManager query window"
        )
        "app-usage" to "Privacy" -> Reading(
            label, "No usage data is sent to a server", "SYSTIVRA local-only implementation"
        )
        "session-analysis" to "Average" -> Reading(
            "Average battery charge", fmt(insights?.batteryMeanPercent, "%"),
            "Measured recorded battery percentages"
        )
        "session-analysis" to "Minimum" -> Reading(
            "Minimum battery charge", fmt(insights?.batteryMinPercent, "%"),
            "Measured recorded battery percentages"
        )
        "session-analysis" to "Maximum" -> Reading(
            "Maximum battery charge", fmt(insights?.batteryMaxPercent, "%"),
            "Measured recorded battery percentages"
        )
        "session-analysis" to "Export" -> Reading(
            label, "Export only user-recorded local samples as CSV or JSON",
            "Private SQLite recordings"
        )
        "benchmark-running" to "Elapsed time" -> Reading(
            label, benchmarkResult?.elapsedMs?.let { "$it ms" },
            "SystemClock; available after benchmark completes"
        )
        "benchmark-running" to "Temperature" -> {
            val real = snapshot.thermal.firstOrNull { it.name == "Battery temperature" }
            Reading(label, real?.value, real?.source ?: "Battery broadcast, if reported")
        }
        "benchmark-running" to "Thermal status" -> {
            val real = snapshot.thermal.firstOrNull { it.name == "Thermal status" }
            Reading(label, real?.value, real?.source ?: "Android PowerManager, API 29+")
        }
        "benchmark-running" to "Stop test" -> Reading(
            label,
            if (running) "Cancel the active workload" else benchmarkMessage,
            "User-controlled BenchmarkController cancellation"
        )
        else -> {
            if (route == "cpu-cores" && label in priorityReferenceSpecs.getValue(route).rows) {
                val count = snapshot.cpu.firstOrNull { it.name == "Available processors" }?.value
                Reading(
                    label, null,
                    "Per-core frequency/load unavailable via standard Android APIs; " +
                        "Runtime.availableProcessors reports ${count ?: "Unavailable"} process-wide"
                )
            } else Reading(label, null, "No supported Android data source for this metric")
        }
    }
}
