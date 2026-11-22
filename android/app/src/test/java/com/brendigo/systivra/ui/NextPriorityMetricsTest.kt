// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NextPriorityMetricsTest {
    private val empty = DeviceSnapshot.empty()

    @Test fun fiveScreensMatchOriginalSvgTitlesAndRows() {
        assertEquals(setOf("metric-fullscreen", "privacy-center", "system-health",
            "sensor-live", "benchmark-center"), nextPriorityReferences.keys)
        assertEquals(listOf("Current value", "Minimum", "Maximum", "Data origin"),
            nextPriorityReferences.getValue("metric-fullscreen").rows)
        assertEquals(listOf("Local data only", "No analytics", "Optional Android access"),
            nextPriorityReferences.getValue("privacy-center").rows)
        assertEquals(listOf("Battery status", "Storage and memory", "Hardware diagnostics"),
            nextPriorityReferences.getValue("system-health").rows)
        assertEquals(listOf("X axis", "Y axis", "Z axis", "Sensor metadata"),
            nextPriorityReferences.getValue("sensor-live").rows)
        assertEquals(listOf("CPU test", "Memory test", "Storage test", "Test conditions"),
            nextPriorityReferences.getValue("benchmark-center").rows)
    }

    @Test fun metricTimelineUsesValidActualBatteryMeasurementsOnly() {
        val samples = listOf("40%", "60%", "110%", "not available", null).mapIndexed { i, v ->
            empty.copy(time = i.toLong() + 1,
                battery = listOf(Reading("Charge level", v, "Android battery broadcast")))
        }
        assertEquals(listOf(40f, 60f), validBatteryPercentages(samples))
        assertEquals("40.0%", nextPriorityRowReading("metric-fullscreen", "Minimum",
            empty, samples).value)
        assertEquals("60.0%", nextPriorityRowReading("metric-fullscreen", "Maximum",
            empty, samples).value)
        assertEquals("60.0%", nextPriorityRowReading("metric-fullscreen", "Current value",
            empty, samples).value)
        val latest = empty.copy(battery = listOf(
            Reading("Charge level", "72%", "ACTION_BATTERY_CHANGED")
        ))
        assertEquals("72.0%", nextPriorityRowReading("metric-fullscreen", "Current value",
            latest, samples).value)
        assertNull(nextPriorityRowReading("metric-fullscreen", "Minimum",
            empty, emptyList()).value)
    }

    @Test fun unsupportedBenchmarksStayUnavailableAndCannotLookLikeScores() {
        listOf("Memory test", "Storage test").forEach {
            val metric = nextPriorityRowReading(
                "benchmark-center", it, empty, emptyList()
            )
            assertNull(metric.value)
            assertTrue(metric.source.contains("Not implemented"))
        }
        assertTrue(nextPriorityRowReading("benchmark-center", "CPU test",
            empty, emptyList()).value!!.contains("two-second"))
    }

    @Test fun systemHealthOnlyCountsActuallyAvailableAndroidFields() {
        val snap = empty.copy(battery = listOf(
            Reading("Charge level", "83%", "BatteryManager"),
            Reading("Voltage", null, "BatteryManager")
        ))
        assertEquals("1 of 2 reported values",
            nextPriorityRowReading("system-health", "Battery status",
                snap, emptyList()).value)
        assertNull(nextPriorityRowReading("system-health", "Battery status",
            empty, emptyList()).value)
    }

    @Test fun sensorActionsNeverPretendConceptGraphIsRealSensorData() {
        val actual = nextPriorityRowReading("sensor-live", "X axis",
            empty, emptyList())
        assertNull(actual.value)
        assertTrue(actual.source.contains("SensorManager"))
    }

    @Test fun privacyNotesAreGroundedInLocalManifestCapabilities() {
        assertTrue(nextPriorityRowReading("privacy-center", "No analytics",
            empty, emptyList()).value!!.contains("INTERNET permission"))
        assertTrue(nextPriorityRowReading("privacy-center", "Optional Android access",
            empty, emptyList()).value!!.contains("optional"))
    }
}
