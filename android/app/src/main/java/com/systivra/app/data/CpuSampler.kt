package com.systivra.app.data

import java.io.File
import java.util.Locale

/**
 * Optional SYSTEM-WIDE CPU activity from /proc/stat, if readable.
 * Modern Android versions and OEM kernels often restrict /proc; returns null.
 * Requires two real samples and always uses the same source for comparison.
 */
class CpuSampler {
    private var previous: Pair<Long, Long>? = null

    @Synchronized
    fun samplePercent(): String? {
        val row = runCatching {
            File("/proc/stat").bufferedReader().use { it.readLine() }
        }.getOrNull()?.split(Regex("\\s+")) ?: return null
        if (row.firstOrNull() != "cpu" || row.size < 8) return null
        val ticks = row.drop(1).take(10).mapNotNull(String::toLongOrNull)
        if (ticks.size < 7) return null
        val idle = ticks[3] + ticks.getOrElse(4) { 0L }
        val total = ticks.sum()
        val old = previous
        previous = total to idle
        if (old == null) return null
        val totalDelta = total - old.first
        val idleDelta = idle - old.second
        if (totalDelta <= 0 || idleDelta < 0 || idleDelta > totalDelta) return null
        return String.format(Locale.US, "%.1f%%", (totalDelta - idleDelta) * 100.0 / totalDelta)
    }
}
