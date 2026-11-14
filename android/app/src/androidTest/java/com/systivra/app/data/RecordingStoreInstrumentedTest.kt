package com.systivra.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordingStoreInstrumentedTest {
    private fun testSnapshot(at: Long): DeviceSnapshot = DeviceSnapshot(
        time = at,
        name = "Test device",
        cpu = listOf(Reading("System-wide CPU load", "25.0%", "Test")),
        gpu = emptyList(),
        ram = listOf(
            Reading("Total RAM", null, "Not saved"),
            Reading("Available RAM", "2.5 GiB", "Test")
        ),
        storage = emptyList(),
        battery = listOf(
            Reading("Charge level", "51.0%", "Test"),
            Reading("Charging status", null, "Not saved"),
            Reading("Battery temperature", "31.2 °C", "Test")
        ),
        thermal = listOf(Reading("Thermal status", "Light", "Test")),
        display = emptyList(),
        system = emptyList(),
        sensors = emptyList()
    )

    @Test fun savedDataCanBeReadAfterDatabaseReopenAndCleared() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        RecordingStore(context).use { first ->
            first.clear()
            first.insert(testSnapshot(1730000000000L))
            first.insert(testSnapshot(1730000002000L))
            assertEquals(2, first.readRecent().size)
        }
        RecordingStore(context).use { reopened ->
            val history = reopened.readRecent()
            assertEquals(2, history.size)
            assertEquals(1730000002000L, history[1].time)
            assertEquals("51.0%", history[1].battery[0].value)
            assertEquals("31.2 °C", history[1].battery[2].value)
            assertEquals("2.5 GiB", history[1].ram[1].value)
            assertEquals("Light", history[1].thermal[0].value)
            reopened.clear()
            assertTrue(reopened.readRecent().isEmpty())
        }
    }
}
