// SYSTIVRA by Brendigo
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

    @Test fun repeatedStartStopWithoutSamplesDoesNotAccumulateHiddenRows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-empty-cleanup.db"
        context.deleteDatabase(name)
        try {
            RecordingStore(context, name).use { store ->
                repeat(40) {
                    val id = store.startSession(1730000000000L + it)
                    store.stopSession(id, 1730000001000L + it)
                }
                store.readableDatabase.rawQuery(
                    "SELECT COUNT(*) FROM recording_sessions", null
                ).use { rows ->
                    assertTrue(rows.moveToFirst())
                    assertEquals(0L, rows.getLong(0))
                }
                assertTrue(store.listSessions().isEmpty())
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test fun openingExistingDatabaseCleansOnlyOldEmptyEndedSessions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-legacy-empty.db"
        context.deleteDatabase(name)
        try {
            var recordedId = 0L
            var unfinishedId = 0L
            RecordingStore(context, name).use { store ->
                recordedId = store.startSession(1730000000000L)
                store.insert(recordedId, snapshot(1730000000000L))
                store.stopSession(recordedId)
                unfinishedId = store.startSession(1730000005000L)
                val oldEmpty = store.startSession(1730000010000L)
                store.writableDatabase.execSQL(
                    "UPDATE recording_sessions SET ended_at = ? WHERE id = ?",
                    arrayOf(1730000011000L, oldEmpty)
                )
            }
            RecordingStore(context, name).use { reopened ->
                val ids = mutableListOf<Long>()
                reopened.readableDatabase.rawQuery(
                    "SELECT id FROM recording_sessions ORDER BY id", null
                ).use { rows ->
                    while (rows.moveToNext()) ids += rows.getLong(0)
                }
                assertEquals(listOf(recordedId, unfinishedId), ids)
                assertEquals(1, reopened.readSession(recordedId).size)
                assertTrue(reopened.readSession(unfinishedId).isEmpty())
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test fun processRestartSealsInterruptedSessionsButRetainsRealSamples() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-interrupted-sessions.db"
        context.deleteDatabase(name)
        try {
            var interruptedId = 0L
            var emptyId = 0L
            RecordingStore(context, name).use { store ->
                interruptedId = store.startSession(1730000000000L)
                store.insert(interruptedId, snapshot(1730000001000L))
                // Simulate process death: no Stop action reached SQLite.
                emptyId = store.startSession(1730000002000L)
            }
            RecordingStore(context, name).use { recovered ->
                recovered.recoverInterruptedSessions()
                assertEquals(1, recovered.listSessions().size)
                assertEquals(interruptedId, recovered.listSessions().single().id)
                assertEquals("51.0%", recovered.readSession(interruptedId).single().battery[0].value)
                recovered.readableDatabase.rawQuery(
                    "SELECT ended_at FROM recording_sessions WHERE id = ?",
                    arrayOf(interruptedId.toString())
                ).use { rows ->
                    assertTrue(rows.moveToFirst())
                    assertEquals(1730000001000L, rows.getLong(0))
                }
                recovered.readableDatabase.rawQuery(
                    "SELECT COUNT(*) FROM recording_sessions WHERE id = ?",
                    arrayOf(emptyId.toString())
                ).use { rows ->
                    assertTrue(rows.moveToFirst())
                    assertEquals(0L, rows.getLong(0))
                }
                // Idempotent on repeated app initialization.
                recovered.recoverInterruptedSessions()
                assertEquals(1, recovered.listSessions().size)
            }
        } finally {
            context.deleteDatabase(name)
        }
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
    @Test fun shuffledReadingOrderIsPersistedByNameRatherThanByIndex() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "systivra-test-shuffled-fields.db"
        context.deleteDatabase(name)
        try {
            RecordingStore(context, name).use { store ->
                val source = snapshot(1730000010000L)
                val shuffled = source.copy(
                    ram = source.ram.reversed(),
                    battery = source.battery.reversed(),
                    thermal = listOf(
                        Reading("Battery temperature", "31.2 °C", "Test"),
                        Reading("Thermal status", "Light", "Test")
                    )
                )
                val id = store.startSession(shuffled.time)
                store.insert(id, shuffled)
                store.stopSession(id)
                val saved = store.readSession(id).single()
                assertEquals("51.0%", saved.chargeLevelText())
                assertEquals("31.2 °C", saved.batteryTemperatureText())
                assertEquals("2.5 GiB", saved.availableRamText())
                assertEquals("Light", saved.thermalStatusText())
                assertEquals("25.0%", saved.cpuLoadText())
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

}
