// SYSTIVRA by Brendigo
package com.brendigo.systivra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionInsightsTest {
    private fun reading(
        time: Long,
        percent: String?,
        temperature: String?,
        thermal: String? = null,
        cpuLoad: String? = null,
        ram: String? = null
    ): DeviceSnapshot = DeviceSnapshot(
        time = time,
        name = "Test",
        cpu = listOf(Reading("System-wide CPU load", cpuLoad, "Test")),
        gpu = emptyList(),
        ram = listOf(Reading("Total", null, "Test"), Reading("Available", ram, "Test")),
        storage = emptyList(),
        battery = listOf(
            Reading("Level", percent, "Test"),
            Reading("State", null, "Test"),
            Reading("Temp", temperature, "Test")
        ),
        thermal = listOf(Reading("Status", thermal, "Test")),
        display = emptyList(),
        system = emptyList(),
        sensors = emptyList()
    )

    @Test fun computesNetRateUsingRealElapsedTime() {
        val result = analyzeSessionReadings(listOf(
            reading(3_660_000L, "65.0%", "29.0 °C", "Light"),
            reading(60_000L, "75.0%", "30.0 °C", "None"),
            reading(1_860_000L, "70.0%", "31.0 °C", "Light")
        ))
        assertEquals(3, result.sampleCount)
        assertEquals(60_000L, result.firstAt)
        assertEquals(3_660_000L, result.lastAt)
        assertEquals(65.0, result.batteryMinPercent!!, 0.0001)
        assertEquals(75.0, result.batteryMaxPercent!!, 0.0001)
        assertEquals(70.0, result.batteryMeanPercent!!, 0.0001)
        assertEquals(-10.0, result.batteryDeltaPercentPoints!!, 0.0001)
        assertEquals(-10.0, result.batteryNetPercentPointsPerHour!!, 0.0001)
        assertEquals(30.0, result.temperatureMeanCelsius!!, 0.0001)
        assertEquals(2, result.thermalStates["Light"])
    }

    @Test fun neverInventsUnavailableMetricsOrRates() {
        val readings = listOf(
            reading(1_000L, null, null),
            reading(2_000L, "Unavailable", "no °C"),
            reading(3_000L, "105%", "NaN °C")
        )
        val result = analyzeSessionReadings(readings)
        assertEquals(0, result.batterySampleCount)
        assertNull(result.batteryMeanPercent)
        assertNull(result.batteryDeltaPercentPoints)
        assertNull(result.batteryNetPercentPointsPerHour)
        assertNull(result.temperatureMeanCelsius)
        assertTrue(result.thermalStates.isEmpty())
    }

    @Test fun shortIntervalsDoNotProduceMisleadingHourlyRates() {
        val result = analyzeSessionReadings(listOf(
            reading(1_000L, "10%", "25 °C"),
            reading(2_000L, "11%", "25 °C")
        ))
        assertEquals(1.0, result.batteryDeltaPercentPoints!!, 0.0001)
        assertNull(result.batteryNetPercentPointsPerHour)
    }

    @Test fun jsonDoesNotLeakDeviceIdentityAndEscapesText() {
        val json = buildSessionJson(listOf(
            reading(1_000L, "66.5%", null, "Alert \"hot\"\nLine", "25.4%", "3\\ GB")
        ))
        assertTrue(json.contains("\"schema_version\": 1"))
        assertTrue(json.contains("\"timestamp_ms\": 1000"))
        assertTrue(json.contains("\"battery_percent\": 66.5"))
        assertTrue(json.contains("\"battery_temperature_c\": null"))
        assertTrue(json.contains("\"cpu_load_percent\": 25.4"))
        assertTrue(json.contains("Alert \\\"hot\\\"\\nLine"))
        assertTrue(json.contains("3\\\\ GB"))
        assertTrue(!json.contains("\"name\""))
        assertTrue(!json.contains("serial_number"))
    }

    @Test fun jsonSpecialCharactersAreEscaped() {
        assertEquals("\"x\\\"y\\\\z\\n\"", jsonString("x\"y\\z\n"))
        assertEquals("\"\\u0001\"", jsonString("\u0001"))
        assertEquals("null", jsonString(null))
    }

    @Test fun jsonEmptyHistoryHasValidArray() {
        val json = buildSessionJson(emptyList())
        assertTrue(json.contains("\"samples\": [  ]"))
    }
}
