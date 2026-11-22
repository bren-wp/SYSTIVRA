// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.data.parseBatteryPercent
import java.util.Locale

/**
 * Source-of-truth labels from brending/screens/{02,04,19,22,29}.
 * The 390x844 SVGs are designs, never a source of synthetic hardware values.
 */
internal data class NextPrioritySpec(
    val title: String,
    val subtitle: String,
    val caption: String,
    val rows: List<String>,
    val hasChart: Boolean
)

internal val nextPriorityReferences = mapOf(
    "system-health" to NextPrioritySpec(
        "System Health", "Your system at a glance", "Real device data overview",
        listOf("Battery status", "Storage and memory", "Hardware diagnostics"), false
    ),
    "privacy-center" to NextPrioritySpec(
        "Privacy Center", "Privacy and control", "Account-free monitoring",
        listOf("Local data only", "No analytics", "Optional Android access"), false
    ),
    "sensor-live" to NextPrioritySpec(
        "Sensor Live View", "Sensor timeline", "Real-time values",
        listOf("X axis", "Y axis", "Z axis", "Sensor metadata"), true
    ),
    "metric-fullscreen" to NextPrioritySpec(
        "Metric Fullscreen", "Detailed timeline", "Units and data source",
        listOf("Current value", "Minimum", "Maximum", "Data origin"), true
    ),
    "benchmark-center" to NextPrioritySpec(
        "Benchmark Center", "Performance tests", "Bounded safe workloads",
        listOf("CPU test", "Memory test", "Storage test", "Test conditions"), true
    )
)

/** Real bounded percentages; Android framework battery values, not invented chart data. */
internal fun validBatteryPercentages(samples: List<DeviceSnapshot>): List<Float> =
    samples.mapNotNull { snapshot ->
        snapshot.battery.firstOrNull { it.name == "Charge level" }?.value
            ?.let(::parseBatteryPercent)?.toFloat()
            ?.takeIf { it.isFinite() && it in 0f..100f }
    }

internal fun nextPriorityRowReading(
    route: String,
    label: String,
    snapshot: DeviceSnapshot,
    samples: List<DeviceSnapshot>
): Reading {
    if (route == "metric-fullscreen") {
        val points = validBatteryPercentages(samples)
        val current = snapshot.battery.firstOrNull { it.name == "Charge level" }
        val present = current?.value?.let(::parseBatteryPercent)
        val value = when (label) {
            "Current value" -> present?.let { String.format(Locale.US, "%.1f%%", it) }
                ?: points.lastOrNull()?.let { String.format(Locale.US, "%.1f%%", it) }
            "Minimum" -> points.minOrNull()?.let { String.format(Locale.US, "%.1f%%", it) }
            "Maximum" -> points.maxOrNull()?.let { String.format(Locale.US, "%.1f%%", it) }
            "Data origin" -> "Android battery broadcast; captured only while the app is visible"
            else -> null
        }
        val origin = if (label == "Current value") {
            current?.source ?: "Android battery broadcast; most recent foreground sample"
        } else "Android ACTION_BATTERY_CHANGED; in-memory foreground samples"
        return Reading(label, value, origin)
    }
    if (route == "benchmark-center") {
        return when (label) {
            "CPU test" -> Reading(label, "Bounded two-second single-worker integer workload",
                "BenchmarkController, cancellable, thermal stop at severe status")
            "Memory test" -> Reading(label, null,
                "Not implemented; no fabricated memory throughput score")
            "Storage test" -> Reading(label, null,
                "Not implemented; no fabricated disk benchmark or destructive writes")
            "Test conditions" -> Reading(label,
                "Foreground-only, at most two seconds; stop before overheating",
                "Android PowerManager thermal status and user cancellation")
            else -> Reading(label, null, "Not provided by this Android version")
        }
    }
    if (route == "sensor-live") {
        return Reading(label, null,
            "View live Android SensorManager events in the sensor controls; " +
                "the design concept contains no authentic axis measurement")
    }
    if (route == "system-health") {
        val categories = when (label) {
            "Battery status" -> snapshot.battery
            "Storage and memory" -> snapshot.storage + snapshot.ram
            "Hardware diagnostics" -> snapshot.cpu + snapshot.thermal
            else -> emptyList()
        }
        val available = categories.count { !it.value.isNullOrBlank() }
        return Reading(label,
            if (categories.isEmpty()) null else "$available of ${categories.size} reported values",
            "Actual Android framework readings; not a fabricated device health score")
    }
    if (route == "privacy-center") {
        return when (label) {
            "Local data only" -> Reading(label,
                "No account. Saved sessions are in private on-device SQLite; export requires a user action.",
                "Application storage and privacy design")
            "No analytics" -> Reading(label,
                "No analytics SDK or Android INTERNET permission.",
                "AndroidManifest.xml and app dependencies")
            "Optional Android access" -> Reading(label,
                "Usage Access is optional. Core monitoring needs no Usage Access.",
                "Android AppOpsManager and user-managed system settings")
            else -> Reading(label, null, "No source available")
        }
    }
    return Reading(label, null, "Unsupported reference route")
}
