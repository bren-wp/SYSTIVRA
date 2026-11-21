// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Context
import com.brendigo.systivra.data.Reading
import java.util.Locale

/**
 * Non-invasive temperature display preferences. Never modifies captured,
 * persisted, copied or exported original Android readings.
 */
internal const val TEMPERATURE_PREFS = "systivra_settings"
internal const val TEMPERATURE_PREF_KEY = "temperature_unit"

internal fun preferredFahrenheit(context: Context): Boolean =
    context.getSharedPreferences(TEMPERATURE_PREFS, Context.MODE_PRIVATE)
        .getString(TEMPERATURE_PREF_KEY, "C") == "F"

private val celsiusValue = Regex(
    """^\s*([+-]?(?:\d+(?:[.,]\d+)?|\.\d+))\s*°\s*C\s*$""",
    RegexOption.IGNORE_CASE
)

/**
 * Only convert genuine Celsius reading strings, and only measurements explicitly
 * identified as a temperature. Missing data and unsupported unit formats remain
 * unchanged instead of being interpreted as zero or a fabricated temperature.
 */
internal fun displayReading(reading: Reading, fahrenheit: Boolean): String {
    val raw = reading.display
    if (!fahrenheit || !reading.name.contains("temperature", ignoreCase = true)) return raw
    val match = celsiusValue.matchEntire(raw) ?: return raw
    val celsius = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return raw
    if (!celsius.isFinite() || celsius !in -273.15..1000.0) return raw
    return String.format(Locale.US, "%.1f °F", celsius * 9.0 / 5.0 + 32.0)
}
