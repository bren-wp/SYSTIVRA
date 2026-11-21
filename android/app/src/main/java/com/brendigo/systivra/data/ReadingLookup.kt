// SYSTIVRA by Brendigo
package com.brendigo.systivra.data

/**
 * Look up a measured value by its stable label, not by its position in a list.
 * Persisted v1 sessions and test fixtures may use older labels; aliases allow
 * those saved measurements to remain readable without inventing values.
 */
private fun List<Reading>.valueFor(vararg labels: String): String? =
    firstOrNull { item -> labels.any { it.equals(item.name, ignoreCase = true) } }
        ?.value?.takeIf(String::isNotBlank)

fun DeviceSnapshot.chargeLevelText(): String? =
    battery.valueFor("Charge level", "Level", "Charge")

fun DeviceSnapshot.batteryTemperatureText(): String? =
    battery.valueFor("Battery temperature", "Temperature", "Temp")

fun DeviceSnapshot.availableRamText(): String? =
    ram.valueFor("Available RAM", "Available")

fun DeviceSnapshot.thermalStatusText(): String? =
    thermal.valueFor("Thermal status", "Status", "Thermal")

fun DeviceSnapshot.cpuLoadText(): String? =
    cpu.valueFor("System-wide CPU load")

/** Do not treat missing values, locale-ambiguous numbers or malformed units as zero. */
fun DeviceSnapshot.batteryPercentage(): Double? = parseBatteryPercent(chargeLevelText())

fun DeviceSnapshot.batteryTemperatureCelsius(): Double? =
    parseBatteryTemperature(batteryTemperatureText())


/**
 * Draw only genuine, Android-reported charge samples. The battery list order
 * varies by API/device and persisted sessions may contain historic field labels.
 * A temperature or battery-current value must never become chart percentage.
 */
fun List<DeviceSnapshot>.measuredChargePercentages(): List<Float> =
    mapNotNull { it.batteryPercentage()?.takeIf { percent ->
        percent.isFinite() && percent in 0.0..100.0
    }?.toFloat() }
