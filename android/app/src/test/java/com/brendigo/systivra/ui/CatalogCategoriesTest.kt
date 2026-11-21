package com.brendigo.systivra.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogCategoriesTest {
    private val routes = listOf(
        "cpu-details" to "CPU Details",
        "battery-details" to "Battery Details",
        "diagnostics-center" to "Diagnostics Center",
        "recording-session" to "Recording Session",
        "reports-export" to "Reports & Export",
        "live-monitor" to "Live Monitor",
        "settings" to "Settings",
        "disabled-route" to "Disabled"
    )
    private val enabled = routes.map { it.first }.toSet() - "disabled-route"

    @Test fun everyEnabledFeatureBelongsToOneStableCategory() {
        assertEquals("Hardware", catalogCategoryFor("cpu-details"))
        assertEquals("Hardware", catalogCategoryFor("sensor-explorer-readings"))
        assertEquals("Monitor", catalogCategoryFor("live-monitor"))
        assertEquals("Monitor", catalogCategoryFor("charging-analysis"))
        assertEquals("Tests", catalogCategoryFor("diagnostics-center"))
        assertEquals("Records", catalogCategoryFor("reports-export-controls"))
        assertEquals("Records", catalogCategoryFor("recording-session"))
        assertEquals("System", catalogCategoryFor("settings"))
        assertTrue(catalogCategories.containsAll(routes.map { catalogCategoryFor(it.first) }))
    }

    @Test fun categoryFiltersReduceLongListsWithoutHidingSearch() {
        assertEquals(
            listOf("cpu-details", "battery-details"),
            filterCatalogRoutes(routes, enabled, "Hardware", "").map { it.first }
        )
        assertEquals(
            listOf("reports-export"),
            filterCatalogRoutes(routes, enabled, "Records", "REPORT").map { it.first }
        )
        assertEquals(
            listOf("battery-details"),
            filterCatalogRoutes(routes, enabled, "All", "  battery ").map { it.first }
        )
    }

    @Test fun disabledRoutesNeverAppearEvenWhenSearchMatches() {
        assertTrue(filterCatalogRoutes(routes, enabled, "All", "Disabled").isEmpty())
    }
}
