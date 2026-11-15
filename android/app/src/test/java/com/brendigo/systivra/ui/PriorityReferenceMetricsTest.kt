package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriorityReferenceMetricsTest {
    private val empty = DeviceSnapshot.empty()

    @Test fun fivePriorityBoardsMatchTheirFourOriginalActionLabels() {
        assertEquals(
            setOf("benchmark-results", "app-usage", "session-analysis",
                "benchmark-running", "cpu-cores"),
            priorityReferenceSpecs.keys
        )
        priorityReferenceSpecs.values.forEach {
            assertEquals(4, it.rows.size)
            assertEquals(4, it.rows.toSet().size)
            assertTrue(it.title.isNotBlank())
            assertTrue(it.caption.isNotBlank())
        }
        assertEquals(
            listOf("Core 0", "Core 1", "Core 2", "Core 3"),
            priorityReferenceSpecs.getValue("cpu-cores").rows
        )
    }

    @Test fun benchmarkRowsUseOnlyCompletedLocalMeasurement() {
        val actual = CpuBenchResult(4096L, 2000L, 123L, "0", false)
        val measured = priorityRowReading(
            "benchmark-results", "Elapsed time", empty, emptyList(), actual, false, "Completed"
        )
        assertEquals("2000 ms", measured.value)
        val missing = priorityRowReading(
            "benchmark-results", "Elapsed time", empty, emptyList(), null, false, "Ready"
        )
        assertNull(missing.value)
        assertEquals("Benchmark results are not persisted in this version",
            priorityRowReading("benchmark-results", "Past sessions", empty, emptyList(),
                null, false, "Ready").value)
    }

    @Test fun sessionAnalysisUsesActualRecordedBatteryPercentages() {
        fun sample(time: Long, value: String) = empty.copy(
            time = time, battery = listOf(Reading("Charge level", value, "Battery broadcast"))
        )
        val samples = listOf(sample(1000L, "42%"), sample(2000L, "58%"))
        assertEquals(listOf(42f, 58f), priorityChartPoints("session-analysis", samples))
        assertEquals("50.0%", priorityRowReading(
            "session-analysis", "Average", empty, samples, null, false, "Ready").value)
        assertEquals("42.0%", priorityRowReading(
            "session-analysis", "Minimum", empty, samples, null, false, "Ready").value)
        assertEquals("58.0%", priorityRowReading(
            "session-analysis", "Maximum", empty, samples, null, false, "Ready").value)
        assertTrue(priorityChartPoints("benchmark-results", samples).isEmpty())
        assertTrue(priorityChartPoints("cpu-cores", samples).isEmpty())
        assertNull(priorityRowReading(
            "session-analysis", "Average", empty, emptyList(), null, false, "Ready").value)
    }

    @Test fun cpuCoreCardDoesNotInventPerCoreFrequencyOrTemperature() {
        val snapshot = empty.copy(
            cpu = listOf(Reading("Available processors", "8", "Runtime.availableProcessors"))
        )
        val reading = priorityRowReading(
            "cpu-cores", "Core 3", snapshot, emptyList(), null, false, "Ready"
        )
        assertNull(reading.value)
        assertTrue(reading.source.contains("8 process-wide"))
    }

    @Test fun runningBenchmarkTemperatureIsFromBatteryBroadcast() {
        val snapshot = empty.copy(
            thermal = listOf(
                Reading("Thermal status", "Moderate", "PowerManager"),
                Reading("Battery temperature", "36.8 °C", "ACTION_BATTERY_CHANGED")
            )
        )
        assertEquals("36.8 °C", priorityRowReading(
            "benchmark-running", "Temperature", snapshot, emptyList(),
            null, true, "Running").value)
        assertEquals("Moderate", priorityRowReading(
            "benchmark-running", "Thermal status", snapshot, emptyList(),
            null, true, "Running").value)
        assertNull(priorityRowReading(
            "benchmark-running", "Temperature", empty, emptyList(),
            null, true, "Running").value)
    }
}
