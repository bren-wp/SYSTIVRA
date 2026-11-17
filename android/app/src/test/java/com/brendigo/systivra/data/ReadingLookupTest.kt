package com.brendigo.systivra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingLookupTest {
    private fun sample(
        time: Long,
        battery: List<Reading>,
        ram: List<Reading> = emptyList(),
        thermal: List<Reading> = emptyList()
    ) = DeviceSnapshot(
        time = time,
        name = "Test",
        cpu = emptyList(),
        gpu = emptyList(),
        ram = ram,
        storage = emptyList(),
        battery = battery,
        thermal = thermal,
        display = emptyList(),
        system = emptyList(),
        sensors = emptyList()
    )

    @Test fun shuffledFieldsStillProduceRealSessionStatisticsAndJson() {
        val first = sample(
            60_000L,
            listOf(
                Reading("Battery temperature", "30.0 °C", "Android"),
                Reading("Technology", "Li-ion", "Android"),
                Reading("Charge level", "80.0%", "Android")
            ),
            ram = listOf(
                Reading("Available RAM", "3 GiB", "Android"),
                Reading("Total RAM", "8 GiB", "Android")
            ),
            thermal = listOf(
                Reading("Battery temperature", "30.0 °C", "Android"),
                Reading("Thermal status", "Light", "Android")
            )
        )
        val second = sample(
            3_660_000L,
            listOf(
                Reading("Charging status", "Discharging", "Android"),
                Reading("Charge level", "70.0%", "Android"),
                Reading("Battery temperature", "31.0 °C", "Android")
            )
        )
        val insights = analyzeSessionReadings(listOf(second, first))
        assertEquals(2, insights.batterySampleCount)
        assertEquals(70.0, insights.batteryMinPercent!!, 0.0001)
        assertEquals(80.0, insights.batteryMaxPercent!!, 0.0001)
        assertEquals(30.5, insights.temperatureMeanCelsius!!, 0.0001)
        assertEquals(1, insights.thermalStates["Light"])
        val json = buildSessionJson(listOf(first))
        assertTrue(json.contains("\"battery_percent\": 80.0"))
        assertTrue(json.contains("\"battery_temperature_c\": 30.0"))
        assertTrue(json.contains("\"free_ram\": \"3 GiB\""))
        assertTrue(json.contains("\"thermal_status\": \"Light\""))
    }

    @Test fun invalidAndMissingFieldsStayUnavailableRatherThanUsingOtherRows() {
        val reading = sample(
            1L,
            listOf(
                Reading("Charging status", "Charging", "Android"),
                Reading("Battery current", "1200 mA", "Android"),
                Reading("Battery temperature", "NaN °C", "Android")
            )
        )
        assertNull(reading.batteryPercentage())
        assertNull(reading.batteryTemperatureCelsius())
        assertNull(reading.chargeLevelText())
        assertNull(reading.availableRamText())
        val report = buildSessionJson(listOf(reading))
        assertTrue(report.contains("\"battery_percent\": null"))
        assertTrue(report.contains("\"battery_temperature_c\": null"))
    }

    @Test fun oldPersistedNamesRemainReadable() {
        val reading = sample(
            2L,
            listOf(
                Reading("Temp", "25 °C", "Android"),
                Reading("Level", "45%", "Android")
            ),
            thermal = listOf(Reading("Status", "None", "Android"))
        )
        assertEquals(45.0, reading.batteryPercentage()!!, 0.0001)
        assertEquals(25.0, reading.batteryTemperatureCelsius()!!, 0.0001)
        assertEquals("None", reading.thermalStatusText())
    }
}
