package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotSame
import org.junit.Test

class PendingReportExportTest {
    @Test fun chooserUsesOriginalSessionEvenWhenHistoryChangesWhileOpen() {
        val first = DeviceSnapshot.empty().copy(
            time = 1_000L,
            battery = listOf(Reading("Charge level", "43%", "Android"))
        )
        val later = DeviceSnapshot.empty().copy(time = 2_000L)
        val history = mutableListOf(first)
        val request = PendingReportExport()
        request.prepare(history)
        history.clear()
        history.add(later)
        val exported = request.consume()!!
        assertEquals(listOf(1_000L), exported.map { it.time })
        assertEquals("43%", exported.single().battery.single().value)
        assertNull(request.consume())
    }

    @Test fun nestedReadingListsAreFrozenAndNotReused() {
        val battery = mutableListOf(Reading("Charge level", "75%", "BatteryManager"))
        val source = DeviceSnapshot.empty().copy(battery = battery)
        val request = PendingReportExport()
        request.prepare(listOf(source))
        battery.clear()
        val exported = request.consume()!!.single()
        assertEquals("75%", exported.battery.single().value)
        assertNotSame(battery, exported.battery)
    }

    @Test fun missingPendingRequestFailsClosedInsteadOfSubstitutingCurrentSession() {
        val request = PendingReportExport()
        assertNull(request.consume())
        request.prepare(listOf(DeviceSnapshot.empty().copy(time = 10L)))
        assertEquals(10L, request.consume()!!.single().time)
        assertNull(request.consume())
    }
}
