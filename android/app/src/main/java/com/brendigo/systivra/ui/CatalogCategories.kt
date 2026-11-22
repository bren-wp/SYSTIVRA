// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

/**
 * A stable, local-only functional grouping for the Screen Catalog.
 * New routes remain discoverable under System (and always under All) until
 * explicitly categorized. No app permissions, remote calls or hidden results.
 */
internal val catalogCategories = listOf("All", "Hardware", "Monitor", "Tests", "Records", "System")

internal fun catalogCategoryFor(route: String): String = when {
    route == "live-monitor" ||
        route.startsWith("sensor-live") ||
        route.startsWith("alerts") ||
        route.startsWith("charging-analysis") ||
        route.startsWith("metric-fullscreen") ||
        route.startsWith("overlay-configuration") ||
        route.startsWith("app-usage") -> "Monitor"

    route == "history" ||
        route.startsWith("recording-") ||
        route.startsWith("session-") ||
        route.startsWith("reports-export") -> "Records"

    route.startsWith("benchmark-") ||
        route.startsWith("diagnostic") ||
        route.startsWith("component-test") ||
        route.startsWith("system-health") -> "Tests"

    route == "hardware-explorer" ||
        route == "system-overview" ||
        route.startsWith("cpu-") ||
        route.startsWith("gpu-") ||
        route.startsWith("memory-") ||
        route.startsWith("storage-") ||
        route.startsWith("battery-") ||
        route.startsWith("thermal-") ||
        route.startsWith("display-") ||
        route.startsWith("device-") ||
        route.startsWith("sensor-") ||
        route.startsWith("network-") -> "Hardware"

    else -> "System"
}

internal fun filterCatalogRoutes(
    routes: List<Pair<String, String>>,
    enabled: Set<String>,
    category: String,
    query: String,
    languageCode: String = UI_LANGUAGE_ENGLISH
): List<Pair<String, String>> {
    val term = query.trim()
    return routes.filter { (route, title) ->
        route in enabled &&
            (category == "All" || catalogCategoryFor(route) == category) &&
            (term.isEmpty() ||
                title.contains(term, ignoreCase = true) ||
                translatedUiText(title, languageCode).contains(term, ignoreCase = true))
    }
}
