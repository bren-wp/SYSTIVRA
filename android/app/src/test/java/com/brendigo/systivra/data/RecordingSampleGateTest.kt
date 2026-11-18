package com.brendigo.systivra.data

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingSampleGateTest {
    @Test fun boundedSamplesNeverGrowBeyondConfiguredBudget() {
        val gate = RecordingSampleGate(maxPending = 32)
        repeat(32) { assertTrue(gate.tryAcquire()) }
        repeat(5000) { assertFalse(gate.tryAcquire()) }
        assertEquals(32, gate.pendingCount())
        gate.release()
        assertTrue(gate.tryAcquire())
        assertEquals(32, gate.pendingCount())
        repeat(32) { gate.release() }
        assertEquals(0, gate.pendingCount())
    }

    @Test fun concurrentProducersCannotOversubscribeSampleBudget() {
        val gate = RecordingSampleGate(maxPending = 16)
        val accepted = AtomicInteger()
        val latch = CountDownLatch(8)
        repeat(8) {
            Thread {
                try {
                    repeat(250) { if (gate.tryAcquire()) accepted.incrementAndGet() }
                } finally {
                    latch.countDown()
                }
            }.start()
        }
        latch.await()
        assertEquals(16, accepted.get())
        assertEquals(16, gate.pendingCount())
        repeat(accepted.get()) { gate.release() }
        assertEquals(0, gate.pendingCount())
    }

    @Test(expected = IllegalStateException::class)
    fun releaseWithoutAcquireIsRejected() {
        RecordingSampleGate().release()
    }

    @Test(expected = IllegalArgumentException::class)
    fun aZeroBudgetIsRejected() {
        RecordingSampleGate(0)
    }
}
