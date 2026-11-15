package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MetricDrilldownTest {
    @Test fun eachReferenceRouteFindsItsActualAndroidData() {
        val cases = listOf(
            Triple("cpu-details", "Architecture", "ABI"),
            Triple("cpu-details", "Core count", "Available processors"),
            Triple("gpu-details", "Graphics API", "OpenGL ES version"),
            Triple("memory-details", "Memory pressure", "Low memory"),
            Triple("storage-details", "Available space", "Available storage"),
            Triple("battery-details", "Charging state", "Charging status"),
            Triple("thermal-details", "Thermal status", "Thermal status"),
            Triple("display-details", "Pixel density", "Density"),
            Triple("device-and-android", "Android version", "Android"),
            Triple("sensor-explorer", "Light sensor", "Ambient Light Sensor")
        )
        cases.forEach { (route, label, name) ->
            val source = Reading(name, "Actual platform value", "Android framework")
            assertEquals(name, metricDetailForRow(route, label, listOf(source)).name)
            assertEquals("Actual platform value",
                metricDetailForRow(route, label, listOf(source)).value)
        }
    }

    @Test fun missingUnsupportedReadingsRemainExplicitlyUnavailable() {
        val missing = metricDetailForRow("display-details", "Color modes", emptyList())
        assertNull(missing.value)
        assertTrue(missing.source.contains("Not reported"))
        val gpu = metricDetailForRow("gpu-details", "GPU load",
            listOf(Reading("GPU utilization", null, "Android API unavailable")))
        assertNull(gpu.value)
        assertEquals("Android API unavailable", gpu.source)
    }

    @Test fun availabilityCountsOnlyNonemptyPlatformValues() {
        val metrics = listOf(
            Reading("A", "5", "Android"),
            Reading("B", null, "Android"),
            Reading("C", "", "Android")
        )
        assertEquals("1 of 3 readings available",
            metricDetailForRow("cpu-details", "Data availability", metrics).value)
    }

    @Test fun readingSearchMatchesNameValueAndSourceWithoutCaseSensitivity() {
        val records = listOf(
            Reading("GPU renderer", "Adreno", "OpenGL"),
            Reading("Refresh rate", "120 Hz", "DisplayManager")
        )
        assertEquals(1, filterDeviceReadings(records, "RENDER").size)
        assertEquals("Refresh rate", filterDeviceReadings(records, "120").single().name)
        assertEquals("Refresh rate", filterDeviceReadings(records, "displaymanager").single().name)
        assertEquals(2, filterDeviceReadings(records, " ").size)
        assertTrue(filterDeviceReadings(records, "no matches").isEmpty())
    }

    @Test fun memoryChartUsesOnlyRealAvailableAndTotalReadings() {
        val measured = DeviceSnapshot.empty().copy(
            ram = listOf(
                Reading("Total RAM", "4.0 GiB", "ActivityManager"),
                Reading("Available RAM", "2.4 GiB", "ActivityManager")
            )
        )
        assertEquals(40f, measuredMemoryUsage(measured) ?: -1f, 0.05f)
        assertEquals(1_073_741_824.0, parseReadableBytes("1.0 GiB") ?: -1.0, 0.01)
        assertEquals(536_870_912.0, parseReadableBytes("512.0 MiB") ?: -1.0, 0.01)
        assertNull(parseReadableBytes("Not available"))
        assertNull(measuredMemoryUsage(DeviceSnapshot.empty()))
        assertNull(measuredMemoryUsage(measured.copy(
            ram = listOf(
                Reading("Total RAM", "1.0 GiB", "Android"),
                Reading("Available RAM", "2.0 GiB", "Android")
            )
        )))
    }
}
