// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

/**
 * Bounded, cycle-aware navigation history for Compose's saved state.
 *
 * Real device usage can revisit dozens of routes through the Screen Catalog.
 * Never grow a navigation stack indefinitely or accumulate repeated loops in
 * SavedState/Bundle; keep the root and most recent 23 entries.
 *
 * Primary tabs always start a new root. Re-entering a previously visited
 * destination returns to it (and discards its descendants), just like a
 * conventional back-stack pop-to-existing rather than duplicating screens.
 */
internal const val MAX_NAVIGATION_DEPTH = 24

internal fun pushNavigationRoute(
    previous: List<String>,
    destination: String,
    primaryTab: Boolean
): ArrayList<String> {
    val current = previous.ifEmpty { listOf("dashboard") }
    if (destination.isBlank()) return ArrayList(current)
    if (primaryTab) return arrayListOf(destination)
    if (current.lastOrNull() == destination) return ArrayList(current)
    val existing = current.indexOfLast { it == destination }
    if (existing >= 0) return ArrayList(current.take(existing + 1))
    return if (current.size < MAX_NAVIGATION_DEPTH) {
        ArrayList(current + destination)
    } else {
        ArrayList(listOf(current.first()) + current.takeLast(MAX_NAVIGATION_DEPTH - 2) + destination)
    }
}

internal fun popNavigationRoute(previous: List<String>): ArrayList<String> =
    if (previous.size <= 1) arrayListOf("dashboard")
    else ArrayList(previous.dropLast(1))
