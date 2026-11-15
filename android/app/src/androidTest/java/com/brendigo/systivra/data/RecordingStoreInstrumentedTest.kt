package com.brendigo.systivra.data

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordingStoreInstrumentedTest {
    private fun snapshot(at: Long): DeviceSnapshot = DeviceSnapshot(
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
        display = emptyList(), system = emptyList(), sensors = emptyList()
    )

    @Test fun sessionsCanBeReadAfterReopenAndIndividuallyDeleted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-sessions.db"
        context.deleteDatabase(name)
        var id1 = 0L
        var id2 = 0L
        RecordingStore(context, name).use { store ->
            id1 = store.startSession(1730000000000L)
            store.insert(id1, snapshot(1730000000000L))
            store.stopSession(id1, 1730000001000L)
            id2 = store.startSession(1730000002000L)
            store.insert(id2, snapshot(1730000002000L))
            store.stopSession(id2, 1730000003000L)
            assertEquals(2, store.listSessions().size)
        }
        RecordingStore(context, name).use { reopened ->
            assertEquals(2, reopened.readRecent().size)
            assertEquals(1, reopened.readSession(id1).size)
            assertEquals("51.0%", reopened.readSession(id2).single().battery[0].value)
            reopened.deleteSession(id1)
            assertEquals(1, reopened.listSessions().size)
            assertEquals(id2, reopened.listSessions().single().id)
            assertEquals(1, reopened.readRecent().size)
            reopened.clear()
            assertTrue(reopened.listSessions().isEmpty())
            assertTrue(reopened.readRecent().isEmpty())
        }
        context.deleteDatabase(name)
    }

    @Test fun databaseV1IsMigratedWithoutDiscardingRecordedReadings() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-migration.db"
        context.deleteDatabase(name)
        val old = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
        try {
            old.execSQL(
                """CREATE TABLE metric_samples (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    recorded_at INTEGER NOT NULL,
                    battery_charge TEXT, battery_temp TEXT,
                    free_ram TEXT, thermal_status TEXT, cpu_load TEXT
                )""".trimIndent()
            )
            old.insertOrThrow("metric_samples", null, ContentValues().apply {
                put("recorded_at", 1730000000000L)
                put("battery_charge", "63.0%")
                put("free_ram", "3.0 GiB")
            })
            old.version = 1
        } finally {
            old.close()
        }
        RecordingStore(context, name).use { migrated ->
            assertEquals(1, migrated.listSessions().size)
            assertEquals(1L, migrated.listSessions().first().id)
            assertEquals("63.0%", migrated.readRecent().single().battery[0].value)
            assertEquals("3.0 GiB", migrated.readSession(1L).single().ram[1].value)
            val second = migrated.startSession(1730000005000L)
            migrated.insert(second, snapshot(1730000005000L))
            assertEquals(2, migrated.listSessions().size)
        }
        context.deleteDatabase(name)
    }

    @Test fun emptySessionHasNoFakeMeasurement() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-empty.db"
        context.deleteDatabase(name)
        RecordingStore(context, name).use { store ->
            val id = store.startSession(1730000000000L)
            store.stopSession(id)
            assertTrue(store.readSession(id).isEmpty())
            assertTrue(store.listSessions().isEmpty())
        }
        context.deleteDatabase(name)
    }
}
