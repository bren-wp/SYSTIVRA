package com.systivra.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Private, local SQLite storage for explicitly recorded device metrics.
 * Never records in the background and never stores device IDs or app usage.
 *
 * At most 900 samples are retained. Writes are called from a dedicated
 * coroutine reader/writer, never from the main thread.
 */
class RecordingStore(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    "systivra_recordings.db",
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE metric_samples (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recorded_at INTEGER NOT NULL,
                battery_charge TEXT,
                battery_temp TEXT,
                free_ram TEXT,
                thermal_status TEXT,
                cpu_load TEXT
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX idx_metric_samples_time ON metric_samples(recorded_at)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Schema version 1 only. Future schema revisions must preserve user data.
        require(oldVersion == newVersion) {
            "Recording database migration is required for version $oldVersion -> $newVersion"
        }
    }

    fun readRecent(): List<DeviceSnapshot> {
        val rows = mutableListOf<PersistedReading>()
        readableDatabase.rawQuery(
            "SELECT recorded_at, battery_charge, battery_temp, free_ram, thermal_status, cpu_load " +
                "FROM metric_samples ORDER BY id DESC LIMIT ?",
            arrayOf(MAX_SAMPLES.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                rows.add(
                    PersistedReading(
                        recordedAt = cursor.getLong(0),
                        batteryCharge = nullable(cursor, 1),
                        batteryTemperature = nullable(cursor, 2),
                        availableMemory = nullable(cursor, 3),
                        thermalStatus = nullable(cursor, 4),
                        cpuLoad = nullable(cursor, 5)
                    )
                )
            }
        }
        return rows.asReversed().map(PersistedReading::toSnapshot)
    }

    fun insert(snapshot: DeviceSnapshot) {
        val values = ContentValues().apply {
            put("recorded_at", snapshot.time)
            put("battery_charge", snapshot.battery.getOrNull(0)?.value)
            put("battery_temp", snapshot.battery.getOrNull(2)?.value)
            put("free_ram", snapshot.ram.getOrNull(1)?.value)
            put("thermal_status", snapshot.thermal.getOrNull(0)?.value)
            put("cpu_load", snapshot.cpu.firstOrNull { it.name == "System-wide CPU load" }?.value)
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            check(db.insertOrThrow("metric_samples", null, values) != -1L)
            db.execSQL(
                "DELETE FROM metric_samples WHERE id NOT IN " +
                    "(SELECT id FROM metric_samples ORDER BY id DESC LIMIT $MAX_SAMPLES)"
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun clear() {
        writableDatabase.delete("metric_samples", null, null)
    }

    private fun nullable(cursor: Cursor, index: Int): String? =
        if (cursor.isNull(index)) null else cursor.getString(index)

    companion object {
        private const val DATABASE_VERSION = 1
        const val MAX_SAMPLES = 900
    }
}

/** Pure immutable value for SQL-to-domain reconstruction; no fabricated fields. */
internal data class PersistedReading(
    val recordedAt: Long,
    val batteryCharge: String?,
    val batteryTemperature: String?,
    val availableMemory: String?,
    val thermalStatus: String?,
    val cpuLoad: String?
) {
    fun toSnapshot(): DeviceSnapshot = DeviceSnapshot(
        time = recordedAt,
        name = "Saved local sample",
        cpu = listOf(Reading("System-wide CPU load", cpuLoad, "SQLite stored reading")),
        gpu = emptyList(),
        ram = listOf(
            Reading("Total RAM", null, "Not stored"),
            Reading("Available RAM", availableMemory, "SQLite stored reading")
        ),
        storage = emptyList(),
        battery = listOf(
            Reading("Charge level", batteryCharge, "SQLite stored reading"),
            Reading("Charging status", null, "Not stored"),
            Reading("Battery temperature", batteryTemperature, "SQLite stored reading")
        ),
        thermal = listOf(Reading("Thermal status", thermalStatus, "SQLite stored reading")),
        display = emptyList(), system = emptyList(), sensors = emptyList()
    )
}
