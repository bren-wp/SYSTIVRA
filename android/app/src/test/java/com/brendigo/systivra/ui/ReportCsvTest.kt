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
    @Test fun exportReadsNamedFieldsWhenAndroidOrdersThemDifferently() {
        val original = entry("77.0%", "29.0 °C")
        val shuffled = original.copy(
            battery = original.battery.reversed(),
            ram = original.ram.reversed(),
            thermal = listOf(
                Reading("Battery temperature", "29.0 °C", "Android"),
                Reading("Thermal status", "Light", "Android")
            )
        )
        val csv = buildSessionCsv(listOf(shuffled))
        assertTrue(csv.contains("\"77.0%\",\"29.0 °C\",\"2 GiB\",\"Light\""))
    }

    @Test fun formulaWithLeadingWhitespaceIsNotExecutedOnSpreadsheetImport() {
        val csv = buildSessionCsv(listOf(entry("  =HYPERLINK(1,2)", "30.0 °C")))
        assertTrue(csv.contains("\"'  =HYPERLINK(1,2)\""))
    }

    @Test fun formulaBehindInvisibleUnicodePrefixIsEscaped() {
        val csv = buildSessionCsv(listOf(entry("\uFEFF=HYPERLINK(1,2)", "\u200B \t@SUM(1)")))
        assertTrue(csv.contains("\"'\uFEFF=HYPERLINK(1,2)\""))
        assertTrue(csv.contains("\"'\u200B \t@SUM(1)\""))
    }

    @Test fun controlPrefixAndUnicodeWordJoinerCannotHideFormula() {
        val csv = buildSessionCsv(listOf(entry("\u0000+1+2", "\u2060-4+5")))
        assertTrue(csv.contains("\"'\u0000+1+2\""))
        assertTrue(csv.contains("\"'\u2060-4+5\""))
    }

    @Test fun safeLeadingUnicodeAndQuotedFieldsArePreserved() {
        val csv = buildSessionCsv(listOf(entry("\u200B35.0%", "35\" Celsius")))
        assertTrue(csv.contains("\"\u200B35.0%\""))
        assertTrue(csv.contains("\"35\"\" Celsius\""))
    }
}
