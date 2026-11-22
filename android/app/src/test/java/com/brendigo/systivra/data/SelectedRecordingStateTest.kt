// SYSTIVRA by Brendigo
package com.brendigo.systivra.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectedRecordingStateTest {
    private fun reading(time: Long): DeviceSnapshot = DeviceSnapshot.empty().copy(time = time)

    @Test fun selectingAnotherRecordingImmediatelyHidesPreviousSamples() {
        val old = SelectedRecordingState(id = 10L, samples = listOf(reading(10L)), generation = 2)
        val next = old.select(20L)
        assertTrue(next.loading)
        assertEquals(20L, next.id)
        assertTrue(next.samples.isEmpty())
        val loaded = next.resolve(20L, next.generation, listOf(reading(20L)))
        assertFalse(loaded.loading)
        assertEquals(listOf(20L), loaded.samples.map { it.time })
    }

    @Test fun slowResultCannotOverrideMoreRecentSelection() {
        val old = SelectedRecordingState().select(10L)
        val current = old.select(20L)
        val ignored = current.resolve(10L, old.generation, listOf(reading(10L)))
        assertEquals(current, ignored)
        assertTrue(ignored.loading)
        val correct = ignored.resolve(20L, current.generation, listOf(reading(20L)))
        assertEquals(listOf(20L), correct.samples.map { it.time })
    }

    @Test fun failureAndDeleteKeepPreviousSessionInvisible() {
        val requested = SelectedRecordingState().select(99L)
        val failed = requested.fail(99L, requested.generation)
        assertFalse(failed.loading)
        assertTrue(failed.samples.isEmpty())
        val reset = failed.clear()
        assertEquals(null, reset.id)
        assertEquals(reset, reset.resolve(99L, requested.generation, listOf(reading(99L))))
    }

    @Test fun selectingAllNeverRetainsPreviousSessionReadings() {
        val selected = SelectedRecordingState().select(50L)
            .resolve(50L, 1L, listOf(reading(50L)))
        val all = selected.select(null).resolve(null, 2L, listOf(reading(1L)))
        assertFalse(all.loading)
        assertEquals(null, all.id)
        assertTrue(all.samples.isEmpty())
    }
}
