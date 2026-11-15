package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.data.RecordedSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdditionalReferenceScreensTest {
    @Test fun designRowsMatchRealRecordingAndPermissionSVGs() {
        assertEquals(listOf("Start", "Pause", "Duration", "Storage policy"),
            recordingReferenceRows)
        assertEquals(listOf("Notifications", "Overlay", "Camera", "Microphone"),
            permissionReferenceRows)
    }

    @Test fun onlyActualRecordedBatteryPercentagesAreGraphed() {
        val base = DeviceSnapshot.empty()
        val samples = listOf(
            base.copy(battery = listOf(Reading("Charge level", "25%", "Android"))),
            base.copy(battery = listOf(Reading("Charge level", null, "Android"))),
            base.copy(battery = listOf(Reading("Charge level", "80%", "Android"))),
            base.copy(battery = listOf(Reading("Charge level", "not-measured", "Android")))
        )
        assertEquals(listOf(25f, 80f), recordedBatteryPoints(samples))
        assertTrue(recordedBatteryPoints(emptyList()).isEmpty())
    }

    @Test fun latestStoredSessionSpanUsesRealSessionTimestamp() {
        val sessions = listOf(
            RecordedSession(1L, 500L, 1500L, 2),
            RecordedSession(2L, 2000L, 8000L, 4)
        )
        assertEquals(6L, latestSessionSpanSeconds(sessions))
        assertNull(latestSessionSpanSeconds(emptyList()))
    }
}
