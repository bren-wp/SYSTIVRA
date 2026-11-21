// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading

/**
 * Connects the four original SVG action labels with truthful Android readings.
 * This is intentionally separate from Compose so every mapping is unit-testable.
 * Unsupported fields return Unavailable, not a synthetic device measurement.
 */
internal fun metricDetailForRow(
    route: String,
    label: String,
    readings: List<Reading>
): Reading {
    if (label == "Data availability" || label == "Availability") {
        val available = readings.count { !it.value.isNullOrBlank() }
        return Reading(label, "$available of ${readings.size} readings available", "Android platform reporting")
    }
    if (label == "Safety guidance") {
        return Reading(
            label,
            "Check the Android thermal status and allow the device to cool if Android reports severe heat.",
            "Android PowerManager thermal status"
        )
    }
    val fields = when (route) {
        "cpu-details" -> when (label) {
            "Architecture" -> listOf("ABI")
            "Core count" -> listOf("Available processors")
            "CPU load" -> listOf("System-wide CPU load")
            else -> emptyList()
        }
        "gpu-details" -> when (label) {
            "Renderer" -> listOf("GPU renderer")
            "Graphics API" -> listOf("OpenGL ES version")
            "GPU load" -> listOf("GPU utilization")
            else -> emptyList()
        }
        "memory-details" -> when (label) {
            "Total RAM" -> listOf("Total RAM")
            "Available RAM" -> listOf("Available RAM")
            "Memory pressure" -> listOf("Low memory")
            else -> emptyList()
        }
        "storage-details" -> when (label) {
            "Total storage" -> listOf("Total internal storage")
            "Available space" -> listOf("Available storage")
            "Used space" -> listOf("Used storage")
            "Volume details" -> listOf("Total internal storage", "Available storage")
            else -> emptyList()
        }
        "battery-details" -> when (label) {
            "Charge level" -> listOf("Charge level")
            "Charging state" -> listOf("Charging status")
            "Battery temperature" -> listOf("Battery temperature")
            "Current" -> listOf("Battery current")
            else -> emptyList()
        }
        "thermal-details" -> when (label) {
            "Thermal status" -> listOf("Thermal status")
            "Battery temperature" -> listOf("Battery temperature")
            else -> emptyList()
        }
        "display-details" -> when (label) {
            "Resolution" -> listOf("Logical resolution")
            "Pixel density" -> listOf("Density")
            "Refresh rate" -> listOf("Reported refresh rate")
            else -> emptyList()
        }
        "device-and-android" -> when (label) {
            "Manufacturer" -> listOf("Manufacturer")
            "Model" -> listOf("Device model")
            "Android version" -> listOf("Android")
            "Security patch" -> listOf("Security patch")
            else -> emptyList()
        }
        "sensor-explorer" -> when (label) {
            "Light sensor" -> listOf("light", "illuminance")
            else -> listOf(label)
        }
        else -> emptyList()
    }
    val reading = if (route == "sensor-explorer") {
        readings.firstOrNull { reading ->
            fields.any { field -> reading.name.contains(field, ignoreCase = true) }
        }
    } else {
        fields.firstNotNullOfOrNull { field -> readings.firstOrNull { it.name == field } }
    }
    return reading ?: Reading(
        label,
        null,
        "Not reported by this device through the Android platform APIs"
    )
}

internal fun filterDeviceReadings(readings: List<Reading>, query: String): List<Reading> {
    val needle = query.trim()
    if (needle.isEmpty()) return readings
    return readings.filter {
        it.name.contains(needle, ignoreCase = true) ||
            it.display.contains(needle, ignoreCase = true) ||
            it.source.contains(needle, ignoreCase = true)
    }
}

/** Parses ONLY units produced by DeviceMetrics.bytes(). No synthetic readings. */
internal fun parseReadableBytes(value: String?): Double? {
    val match = Regex("""^([0-9]+(?:\.[0-9]+)?) (GiB|MiB)$""").matchEntire(value ?: "")
        ?: return null
    val amount = match.groupValues[1].toDoubleOrNull() ?: return null
    val factor = if (match.groupValues[2] == "GiB") 1_073_741_824.0 else 1_048_576.0
    val bytes = amount * factor
    return bytes.takeIf { it.isFinite() && it >= 0.0 }
}

/**
 * Display-only RAM percentage from rounded ActivityManager MemoryInfo strings.
 * No result is shown when Android does not supply both total and available memory.
 */
internal fun measuredMemoryUsage(snapshot: DeviceSnapshot): Float? {
    val total = parseReadableBytes(snapshot.ram.firstOrNull { it.name == "Total RAM" }?.value)
        ?: return null
    val free = parseReadableBytes(snapshot.ram.firstOrNull { it.name == "Available RAM" }?.value)
        ?: return null
    if (total <= 0.0 || free > total) return null
    return ((total - free) * 100.0 / total).toFloat()
        .takeIf { it.isFinite() && it in 0f..100f }
}
