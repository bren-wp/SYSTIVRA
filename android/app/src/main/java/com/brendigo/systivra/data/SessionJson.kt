package com.brendigo.systivra.data

/**
 * Local JSON export, format v1. Uses only explicitly recorded measurements.
 * No device IDs, package names, user accounts or network data are exported.
 * Invalid or unavailable values become JSON null, not invented zeroes.
 */
fun buildSessionJson(samples: List<DeviceSnapshot>): String = buildString {
    append("{\n  \"format\": \"systivra-session\",\n  \"schema_version\": 1,\n  \"samples\": [")
    samples.forEachIndexed { index, sample ->
        if (index > 0) append(',')
        append("\n    {")
        append("\"timestamp_ms\": ")
        append(sample.time)
        append(", \"battery_percent\": ")
        append(sample.batteryPercentage()?.toString() ?: "null")
        append(", \"battery_temperature_c\": ")
        append(sample.batteryTemperatureCelsius()?.toString() ?: "null")
        append(", \"free_ram\": ")
        append(jsonString(sample.availableRamText()))
        append(", \"thermal_status\": ")
        append(jsonString(sample.thermalStatusText()))
        append(", \"cpu_load_percent\": ")
        append(parseBatteryPercent(
            sample.cpuLoadText()
        )?.toString() ?: "null")
        append('}')
    }
    if (samples.isNotEmpty()) append('\n')
    append("  ]\n}\n")
}

/** RFC 8259 JSON string escaping without Android-only classes. */
internal fun jsonString(value: String?): String {
    if (value == null) return "null"
    return buildString {
        append('"')
        value.forEach { ch ->
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000c' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u")
                    append(ch.code.toString(16).padStart(4, '0'))
                } else append(ch)
            }
        }
        append('"')
    }
}
