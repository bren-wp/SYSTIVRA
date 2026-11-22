// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UiLanguageTest {
    @Test fun englishIsDefaultAndPreservesReferenceTextByteForByte() {
        assertEquals("en", normalizedUiLanguage(null))
        assertEquals("en", normalizedUiLanguage(""))
        assertEquals("en", normalizedUiLanguage("de"))
        for (text in listOf("Dashboard", "Settings", "Battery Details",
            "Reports & Export", "Monitor", "Tests", "More", "SYSTIVRA")) {
            assertEquals(text, translatedUiText(text, "en"))
        }
    }

    @Test fun croatianCoversMainNavigationAndCoreActions() {
        assertEquals("hr", normalizedUiLanguage("hr"))
        assertEquals("Početna", translatedUiText("Dashboard", "hr"))
        assertEquals("Postavke", translatedUiText("Settings", "hr"))
        assertEquals("Nadzor", translatedUiText("Monitor", "hr"))
        assertEquals("Testovi", translatedUiText("Tests", "hr"))
        assertEquals("Više", translatedUiText("More", "hr"))
        assertEquals("Zatvori", translatedUiText("Close", "hr"))
        assertEquals("Otvori Detalji baterije",
            translatedUiText("Open Battery Details", "hr"))
    }

    @Test fun croatianDeviceValuesAreNotInventedOrChanged() {
        assertEquals("82.4 °C", translatedUiText("82.4 °C", "hr"))
        assertEquals("Izvor: Android BatteryManager",
            translatedUiText("Source: Android BatteryManager", "hr"))
        assertEquals("432.1 MiB", translatedUiText("432.1 MiB", "hr"))
    }

    @Test fun catalogSearchMatchesEnglishAndTranslatedNames() {
        val routes = listOf(
            "reports-export" to "Reports & Export",
            "cpu-details" to "CPU Details",
            "settings" to "Settings"
        )
        val enabled = routes.map { it.first }.toSet()
        val english = filterCatalogRoutes(routes, enabled, "All", "Reports")
        assertEquals(listOf("reports-export"), english.map { it.first })
        val croatian = filterCatalogRoutes(routes, enabled, "All", "izvještaji", "hr")
        assertEquals(listOf("reports-export"), croatian.map { it.first })
        assertTrue(filterCatalogRoutes(routes, enabled, "All", "postavke", "hr")
            .any { it.first == "settings" })
        assertTrue(filterCatalogRoutes(routes, enabled, "All", "CPU", "hr")
            .any { it.first == "cpu-details" })
    }
}
