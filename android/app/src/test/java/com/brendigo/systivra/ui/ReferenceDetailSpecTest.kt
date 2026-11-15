package com.brendigo.systivra.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceDetailSpecTest {
    @Test
    fun nineOriginalDeviceDetailRoutesHaveFourReferenceActions() {
        val expected = setOf(
            "cpu-details", "gpu-details", "memory-details", "storage-details",
            "battery-details", "thermal-details", "display-details",
            "device-and-android", "sensor-explorer"
        )
        assertEquals(expected, detailReferences.keys)
        detailReferences.forEach { (_, spec) ->
            assertFalse(spec.title.isBlank())
            assertFalse(spec.subtitle.isBlank())
            assertFalse(spec.description.isBlank())
            assertEquals(4, spec.rows.size)
            assertTrue(spec.rows.all(String::isNotBlank))
            assertEquals(4, spec.rows.distinct().size)
        }
    }

    @Test
    fun chartOnlyAppearsOnOriginalChartReferenceRoutes() {
        val chartRoutes = detailReferences.filterValues { it.hasChart }.keys
        assertEquals(setOf("cpu-details", "memory-details", "battery-details", "thermal-details"), chartRoutes)
    }
}
