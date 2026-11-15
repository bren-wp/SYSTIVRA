package com.brendigo.systivra.data

/**
 * Descriptive statistics over real recorded samples. Not a battery-health
 * diagnosis and not an estimate of battery capacity or charging efficiency.
 */
data class SessionInsights(
    val sampleCount: Int,
    val firstAt: Long?,
    val lastAt: Long?,
    val batterySampleCount: Int,
    val batteryMinPercent: Double?,
    val batteryMaxPercent: Double?,
    val batteryMeanPercent: Double?,
    val batteryDeltaPercentPoints: Double?,
    val batteryNetPercentPointsPerHour: Double?,
    val temperatureSampleCount: Int,
    val temperatureMinCelsius: Double?,
    val temperatureMaxCelsius: Double?,
    val temperatureMeanCelsius: Double?,
    val thermalStates: Map<String, Int>
) {
    val elapsedMillis: Long?
        get() = if (firstAt == null || lastAt == null) null
        else (lastAt - firstAt).coerceAtLeast(0L)
}

/**
 * Only accepts values explicitly reported as battery percentage and °C.
 * Missing / invalid readings are skipped rather than converted to zero.
 */
internal fun parseBatteryPercent(text: String?): Double? {
    val value = text?.trim()?.takeIf { it.endsWith("%") }
        ?.dropLast(1)?.trim()?.toDoubleOrNull()
    return value?.takeIf { it.isFinite() && it in 0.0..100.0 }
}

internal fun parseBatteryTemperature(text: String?): Double? {
    val value = text?.trim()?.takeIf { it.endsWith("°C") }
        ?.removeSuffix("°C")?.trim()?.toDoubleOrNull()
    // A deliberately broad numeric sanity range, not a diagnosis.
    return value?.takeIf { it.isFinite() && it in -50.0..100.0 }
}

fun analyzeSessionReadings(samples: List<DeviceSnapshot>): SessionInsights {
    val ordered = samples.asSequence()
        .filter { it.time > 0L }
        .sortedBy { it.time }
        .toList()

    val battery = ordered.mapNotNull { sample ->
        parseBatteryPercent(sample.battery.getOrNull(0)?.value)
            ?.let { sample.time to it }
    }
    val temperatures = ordered.mapNotNull {
        parseBatteryTemperature(it.battery.getOrNull(2)?.value)
    }
    val states = ordered.mapNotNull {
        it.thermal.getOrNull(0)?.value?.trim()?.takeIf(String::isNotEmpty)
    }.groupingBy { it }.eachCount().toSortedMap()

    val delta = if (battery.size >= 2) battery.last().second - battery.first().second
        else null
    val duration = if (battery.size >= 2) battery.last().first - battery.first().first
        else 0L
    // Reporting a rate over tiny or duplicate intervals would be misleading.
    val hourlyRate = if (delta != null && duration >= 60_000L)
        delta * 3_600_000.0 / duration else null

    return SessionInsights(
        sampleCount = ordered.size,
        firstAt = ordered.firstOrNull()?.time,
        lastAt = ordered.lastOrNull()?.time,
        batterySampleCount = battery.size,
        batteryMinPercent = battery.minOfOrNull { it.second },
        batteryMaxPercent = battery.maxOfOrNull { it.second },
        batteryMeanPercent = if (battery.isEmpty()) null else battery.map { it.second }.average(),
        batteryDeltaPercentPoints = delta,
        batteryNetPercentPointsPerHour = hourlyRate,
        temperatureSampleCount = temperatures.size,
        temperatureMinCelsius = temperatures.minOrNull(),
        temperatureMaxCelsius = temperatures.maxOrNull(),
        temperatureMeanCelsius = if (temperatures.isEmpty()) null else temperatures.average(),
        thermalStates = states
    )
}
