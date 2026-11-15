package com.brendigo.systivra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verify that restored readings match the UI/CSV field contract. */
class PersistedReadingTest {
    @Test fun restoresSavedValuesAtExpectedIndices() {
        val entry = PersistedReading(
            recordedAt = 1730000000000L,
            batteryCharge = "41.0%",
            batteryTemperature = "29.1 °C",
            availableMemory = "3.8 GiB",
            thermalStatus = "Moderate",
            cpuLoad = "25.4%"
        ).toSnapshot()
        assertEquals(1730000000000L, entry.time)
        assertEquals("41.0%", entry.battery[0].value)
        assertEquals("29.1 °C", entry.battery[2].value)
        assertEquals("3.8 GiB", entry.ram[1].value)
        assertEquals("Moderate", entry.thermal[0].value)
        assertEquals("25.4%", entry.cpu[0].value)
        assertTrue(entry.gpu.isEmpty())
    }

    @Test fun unavailableMetricsRemainNullAfterRestore() {
        val entry = PersistedReading(0L, null, null, null, null, null).toSnapshot()
        assertNull(entry.battery[0].value)
        assertNull(entry.battery[2].value)
        assertNull(entry.ram[1].value)
        assertNull(entry.thermal[0].value)
        assertNull(entry.cpu[0].value)
        assertNull(entry.battery[1].value)
        assertEquals("Unavailable", entry.battery[0].display)
    }
}
