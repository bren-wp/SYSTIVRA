// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationHistoryTest {
    @Test fun repeatedRoutesPopToExistingInsteadOfAccumulatingDuplicates() {
        val path = listOf("dashboard", "hardware-explorer", "cpu-details", "cpu-details-readings")
        val returned = pushNavigationRoute(path, "hardware-explorer", primaryTab = false)
        assertEquals(listOf("dashboard", "hardware-explorer"), returned)
        assertEquals(listOf("dashboard"), popNavigationRoute(returned))
    }

    @Test fun rapidTabSwitchesReplaceRootAndBackDoesNotReopenOldTabs() {
        var path = arrayListOf("dashboard")
        path = pushNavigationRoute(path, "cpu-details", primaryTab = false)
        path = pushNavigationRoute(path, "settings", primaryTab = true)
        assertEquals(listOf("settings"), path)
        path = pushNavigationRoute(path, "privacy-center", primaryTab = false)
        assertEquals(listOf("settings", "privacy-center"), path)
        assertEquals(listOf("settings"), popNavigationRoute(path))
    }

    @Test fun longBrowsingIsBoundedAndPreservesInitialRoot() {
        var path = arrayListOf("dashboard")
        repeat(250) { step ->
            path = pushNavigationRoute(path, "tool-$step", primaryTab = false)
            assertTrue(path.size <= MAX_NAVIGATION_DEPTH)
        }
        assertEquals(MAX_NAVIGATION_DEPTH, path.size)
        assertEquals("dashboard", path.first())
        assertEquals("tool-249", path.last())
        assertEquals("tool-248", popNavigationRoute(path).last())
    }

    @Test fun blankOrDuplicateNavigationLeavesHistoryUnchanged() {
        val stack = listOf("dashboard", "battery-details")
        assertEquals(stack, pushNavigationRoute(stack, "", primaryTab = false))
        assertEquals(stack, pushNavigationRoute(stack, "battery-details", primaryTab = false))
        assertEquals(listOf("dashboard"), popNavigationRoute(emptyList()))
    }

    @Test fun sameDeepLinkReturnsToItsOriginalEntry() {
        val stack = listOf("gpu-details", "gpu-details-readings", "settings")
        assertEquals(
            listOf("gpu-details"),
            pushNavigationRoute(stack, "gpu-details", primaryTab = false)
        )
    }
}
