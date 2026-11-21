// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.RecordedSession
import java.time.Instant
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFilterTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val sunday = Instant.parse("2026-11-15T11:00:00Z").toEpochMilli()
    private val today = Instant.parse("2026-11-15T09:00:00Z").toEpochMilli()
    private val monday = Instant.parse("2026-11-09T08:00:00Z").toEpochMilli()
    private val lastSunday = Instant.parse("2026-11-08T20:00:00Z").toEpochMilli()
    private val future = Instant.parse("2026-11-16T09:00:00Z").toEpochMilli()

    private val sessions = listOf(
        RecordedSession(id = 1, startedAt = lastSunday, lastSampleAt = lastSunday, sampleCount = 1),
        RecordedSession(id = 2, startedAt = today, lastSampleAt = today, sampleCount = 2),
        RecordedSession(id = 3, startedAt = monday, lastSampleAt = monday, sampleCount = 3),
        RecordedSession(id = 4, startedAt = future, lastSampleAt = future, sampleCount = 4)
    )

    @Test fun todayFilterExcludesOlderAndFutureSessions() {
        assertEquals(listOf(2L), visibleSavedSessions(sessions, HistoryWindow.TODAY, sunday, timeZone = utc).map { it.id })
    }

    @Test fun weekFilterIncludesMondayThroughSundayButNotPriorWeek() {
        assertEquals(listOf(2L, 3L), visibleSavedSessions(sessions, HistoryWindow.WEEK, sunday, timeZone = utc).map { it.id })
    }

    @Test fun allFilterHonorsSortDirectionAndExcludesFutureData() {
        assertEquals(listOf(2L, 3L, 1L), visibleSavedSessions(sessions, HistoryWindow.ALL, sunday, timeZone = utc).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), visibleSavedSessions(sessions, HistoryWindow.ALL, sunday, newestFirst = false, timeZone = utc).map { it.id })
    }
}
