// SYSTIVRA by Brendigo
package com.brendigo.systivra.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Local, private session-based history.
 *
 * Both schema versions preserve existing user samples. Only explicit
 * foreground recording writes a sample. The app retains at most 900 samples
 * globally to bound storage, and can delete one session or all history.
 */
class RecordingStore(
    context: Context,
    databaseName: String = "systivra_recordings.db"
) : SQLiteOpenHelper(context.applicationContext, databaseName, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE recording_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                started_at INTEGER NOT NULL,
                ended_at INTEGER
            )""".trimIndent()
        )
        db.execSQL(
            """CREATE TABLE metric_samples (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                recorded_at INTEGER NOT NULL,
                battery_charge TEXT,
                battery_temp TEXT,
                free_ram TEXT,
                thermal_status TEXT,
                cpu_load TEXT
            )""".trimIndent()
        )
        db.execSQL("CREATE INDEX idx_metric_samples_session_time ON metric_samples(session_id, recorded_at)")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            // Remove only completed empty sessions left by older releases.
            // In-progress sessions without a sample must NOT be touched.
            db.execSQL(
                "DELETE FROM recording_sessions WHERE ended_at IS NOT NULL " +
                    "AND NOT EXISTS (SELECT 1 FROM metric_samples " +
                    "WHERE session_id = recording_sessions.id)"
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL(
                """CREATE TABLE recording_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    started_at INTEGER NOT NULL,
                    ended_at INTEGER
                )""".trimIndent()
            )
            db.execSQL("ALTER TABLE metric_samples ADD COLUMN session_id INTEGER NOT NULL DEFAULT 1")
            db.rawQuery(
                "SELECT MIN(recorded_at), MAX(recorded_at), COUNT(*) FROM metric_samples", null
            ).use { row ->
                if (row.moveToFirst() && row.getLong(2) > 0L) {
                    val values = ContentValues().apply {
                        put("id", 1L)
                        put("started_at", row.getLong(0))
                        put("ended_at", row.getLong(1))
                    }
                    db.insertOrThrow("recording_sessions", null, values)
                }
            }
            db.execSQL("CREATE INDEX idx_metric_samples_session_time ON metric_samples(session_id, recorded_at)")
            return
        }
        error("No safe database migration from $oldVersion to $newVersion")
    }

    /**
     * Called once by the foreground Activity before accepting new recording
     * actions. After an OS process kill, earlier sessions may have no end time.
     * Preserve their actual recorded data, close them at the last sample and
     * remove only orphaned empty sessions. Never run during live recording.
     */
    fun recoverInterruptedSessions() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL(
                "UPDATE recording_sessions SET ended_at = MAX(started_at, " +
                    "COALESCE((SELECT MAX(recorded_at) FROM metric_samples " +
                    "WHERE session_id = recording_sessions.id), started_at)) " +
                    "WHERE ended_at IS NULL"
            )
            db.execSQL(
                "DELETE FROM recording_sessions WHERE NOT EXISTS " +
                    "(SELECT 1 FROM metric_samples WHERE session_id = recording_sessions.id)"
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun startSession(atMillis: Long = System.currentTimeMillis()): Long =
        writableDatabase.insertOrThrow(
            "recording_sessions", null,
            ContentValues().apply { put("started_at", atMillis) }
        )

    fun stopSession(id: Long, atMillis: Long = System.currentTimeMillis()) {
        // An immediate Start -> Stop before the first foreground sample must
        // not leave an unlimited trail of invisible, empty SQLite rows.
        // Update and cleanup atomically; never delete a recorded session.
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.update(
                "recording_sessions",
                ContentValues().apply { put("ended_at", atMillis) },
                "id = ? AND ended_at IS NULL",
                arrayOf(id.toString())
            )
            db.delete(
                "recording_sessions",
                "id = ? AND ended_at IS NOT NULL AND NOT EXISTS " +
                    "(SELECT 1 FROM metric_samples WHERE session_id = ?)",
                arrayOf(id.toString(), id.toString())
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun readRecent(): List<DeviceSnapshot> = readSamples(null)

    fun readSession(id: Long): List<DeviceSnapshot> = readSamples(id)

    private fun readSamples(sessionId: Long?): List<DeviceSnapshot> {
        val readings = mutableListOf<PersistedReading>()
        val statement = "SELECT recorded_at, battery_charge, battery_temp, free_ram, thermal_status, cpu_load " +
            "FROM metric_samples " +
            (if (sessionId != null) "WHERE session_id = ? " else "") +
            "ORDER BY id DESC LIMIT ?"
        val args = if (sessionId == null) arrayOf(MAX_SAMPLES.toString())
            else arrayOf(sessionId.toString(), MAX_SAMPLES.toString())
        readableDatabase.rawQuery(statement, args).use { cursor ->
            while (cursor.moveToNext()) {
                readings += PersistedReading(
                    recordedAt = cursor.getLong(0),
                    batteryCharge = cursor.stringOrNull(1),
                    batteryTemperature = cursor.stringOrNull(2),
                    availableMemory = cursor.stringOrNull(3),
                    thermalStatus = cursor.stringOrNull(4),
                    cpuLoad = cursor.stringOrNull(5)
                )
            }
        }
        return readings.asReversed().map(PersistedReading::toSnapshot)
    }

    fun listSessions(): List<RecordedSession> {
        val sessions = mutableListOf<RecordedSession>()
        readableDatabase.rawQuery(
            """SELECT s.id, s.started_at,
                COALESCE(s.ended_at, MAX(m.recorded_at), s.started_at), COUNT(m.id)
               FROM recording_sessions s
               LEFT JOIN metric_samples m ON m.session_id = s.id
               GROUP BY s.id
               HAVING COUNT(m.id) > 0
               ORDER BY s.id DESC LIMIT 100""".trimIndent(),
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                sessions += RecordedSession(
                    id = cursor.getLong(0),
                    startedAt = cursor.getLong(1),
                    lastSampleAt = cursor.getLong(2),
                    sampleCount = cursor.getInt(3)
                )
            }
        }
        return sessions
    }

    fun insert(sessionId: Long, snapshot: DeviceSnapshot) {
        val values = ContentValues().apply {
            put("session_id", sessionId)
            put("recorded_at", snapshot.time)
            put("battery_charge", snapshot.chargeLevelText())
            put("battery_temp", snapshot.batteryTemperatureText())
            put("free_ram", snapshot.availableRamText())
            put("thermal_status", snapshot.thermalStatusText())
            put("cpu_load", snapshot.cpuLoadText())
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Reject late writes after Stop/Delete/Clear. The check and insert
            // run inside the same transaction so stale queued samples cannot
            // create orphan rows (SQLite schema v2 has no foreign key).
            val active = db.rawQuery(
                "SELECT 1 FROM recording_sessions WHERE id = ? AND ended_at IS NULL LIMIT 1",
                arrayOf(sessionId.toString())
            ).use { it.moveToFirst() }
            check(active) { "Recording session is missing or has already ended" }
            db.insertOrThrow("metric_samples", null, values)
            db.execSQL(
                "DELETE FROM metric_samples WHERE id NOT IN " +
                    "(SELECT id FROM metric_samples ORDER BY id DESC LIMIT $MAX_SAMPLES)"
            )
            pruneEmptySessions(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteSession(id: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("metric_samples", "session_id = ?", arrayOf(id.toString()))
            db.delete("recording_sessions", "id = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun clear() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("metric_samples", null, null)
            db.delete("recording_sessions", null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun pruneEmptySessions(db: SQLiteDatabase) {
        db.execSQL(
            "DELETE FROM recording_sessions WHERE id NOT IN " +
                "(SELECT DISTINCT session_id FROM metric_samples)"
        )
    }

    private fun Cursor.stringOrNull(column: Int): String? =
        if (isNull(column)) null else getString(column)

    companion object {
        private const val DATABASE_VERSION = 2
        const val MAX_SAMPLES = 900
    }
}

data class RecordedSession(
    val id: Long,
    val startedAt: Long,
    val lastSampleAt: Long,
    val sampleCount: Int
)

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
