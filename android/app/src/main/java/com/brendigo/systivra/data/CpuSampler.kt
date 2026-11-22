// SYSTIVRA by Brendigo
package com.brendigo.systivra.data

import java.io.File
import java.util.Locale

/**
 * Optional SYSTEM-WIDE CPU activity from /proc/stat, if readable.
 * Modern Android versions often restrict /proc; returns null.
 * Requires two real samples and does not guess if the data is blocked.
 */
class CpuSampler {
    private var previous: CpuTicks? = null

    @Synchronized
    fun samplePercent(): String? {
        val row = runCatching { File("/proc/stat").bufferedReader().use { it.readLine() } }.getOrNull()
        val current = parseCpuTicks(row) ?: return null
        val load = cpuLoadPercent(previous, current)
        previous = current
        return load?.let { String.format(Locale.US, "%.1f%%", it) }
    }
}

internal data class CpuTicks(val total: Long, val idle: Long)

/** Linux /proc/stat aggregate CPU jiffy parser. */
internal fun parseCpuTicks(row: String?): CpuTicks? {
    val tokens = row?.trim()?.split(Regex("\\s+")) ?: return null
    if (tokens.firstOrNull() != "cpu" || tokens.size < 8) return null
    val numbers = tokens.drop(1).take(10).map { it.toLongOrNull() }
    if (numbers.size < 7 || numbers.any { it == null || it < 0 }) return null
    val ticks = numbers.filterNotNull()
    return CpuTicks(
        total = ticks.sum(),
        idle = ticks[3] + ticks.getOrElse(4) { 0L }
    )
}

internal fun cpuLoadPercent(previous: CpuTicks?, current: CpuTicks): Double? {
    if (previous == null) return null
    val totalDelta = current.total - previous.total
    val idleDelta = current.idle - previous.idle
    if (totalDelta <= 0 || idleDelta < 0 || idleDelta > totalDelta) return null
    return (totalDelta - idleDelta) * 100.0 / totalDelta
}
