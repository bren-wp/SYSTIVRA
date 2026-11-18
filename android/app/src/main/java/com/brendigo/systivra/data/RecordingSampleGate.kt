package com.brendigo.systivra.data

import java.util.concurrent.atomic.AtomicInteger

/**
 * Hard cap on large hardware snapshots waiting for disk. Control actions are
 * never discarded; only new samples may be rejected if storage falls behind.
 *
 * Atomic CAS is used instead of increment/decrement-on-reject so a stalled
 * consumer cannot temporarily exceed the cap under concurrent producers.
 */
internal class RecordingSampleGate(private val maxPending: Int = 32) {
    init { require(maxPending > 0) }

    private val pending = AtomicInteger(0)

    fun tryAcquire(): Boolean {
        while (true) {
            val current = pending.get()
            if (current >= maxPending) return false
            if (pending.compareAndSet(current, current + 1)) return true
        }
    }

    fun release() {
        while (true) {
            val current = pending.get()
            check(current > 0) { "Storage sample release without successful enqueue" }
            if (pending.compareAndSet(current, current - 1)) return
        }
    }

    fun pendingCount(): Int = pending.get()
}
