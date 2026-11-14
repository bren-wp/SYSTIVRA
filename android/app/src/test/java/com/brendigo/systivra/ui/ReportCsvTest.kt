package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportCsvTest {
    private fun entry(battery: String?, temperature: String?): DeviceSnapshot =
        DeviceSnapshot(
            time = 0L,
            name = "Test device",
            cpu = emptyList(),
            gpu = emptyList(),
            ram = listOf(
                Reading("Total RAM", "8 GiB", "ActivityManager"),
                Reading("Available RAM", "2 GiB", "ActivityManager")
            ),
            storage = emptyList(),
            battery = listOf(
                Reading("Charge", battery, "BatteryManager"),
                Reading("Charging", "Charging", "BatteryManager"),
                Reading("Temperature", temperature, "BatteryManager")
            ),
            thermal = listOf(Reading("Thermal", "Moderate", "PowerManager")),
            display = emptyList(),
            system = emptyList(),
            sensors = emptyList()
        )

    @Test fun exportsUtcAndRealData() {
        val csv = buildSessionCsv(listOf(entry("55.0%", "30.1 °C")))
        assertTrue(csv.startsWith("timestamp_utc,battery_percent,battery_temp_c,free_ram,thermal_status"))
        assertTrue(csv.contains("1970-01-01T00:00:00.000Z"))
        assertTrue(csv.contains("\"55.0%\""))
        assertTrue(csv.contains("\"30.1 °C\""))
    }

    @Test fun missingReadingsStayEmpty() {
        val csv = buildSessionCsv(listOf(entry(null, null)))
        assertTrue(csv.contains("\"\",\"\""))
        assertEquals(2, csv.trimEnd().lines().size)
    }

    @Test fun emptyHistoryContainsHeaderOnly() {
        assertEquals(1, buildSessionCsv(emptyList()).trimEnd().lines().size)
    }

    @Test fun spreadsheetFormulasAreEscaped() {
        val csv = buildSessionCsv(listOf(entry("=SUM(1,2)", "35")))
        assertTrue(csv.contains("\"'=SUM(1,2)\""))
    }
}
