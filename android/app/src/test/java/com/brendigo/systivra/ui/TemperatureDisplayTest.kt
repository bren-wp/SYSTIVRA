// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import com.brendigo.systivra.data.Reading
import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureDisplayTest {
    private fun metric(name: String, value: String?): Reading =
        Reading(name, value, "Android platform")

    @Test fun convertsOnlyNamedCelsiusTemperatureMeasurements() {
        assertEquals("95.0 °F", displayReading(metric("Battery temperature", "35.0 °C"), true))
        assertEquals("32.0 °F", displayReading(metric("Temperature", "0 °C"), true))
        assertEquals("-40.0 °F", displayReading(metric("Battery temperature", "-40 °C"), true))
        assertEquals("98.6 °F", displayReading(metric("CPU temperature", "37,0 °C"), true))
    }

    @Test fun preservesExactSourceForCelsiusAndUnsupportedValues() {
        val original = metric("Battery temperature", "35.0 °C")
        assertEquals("35.0 °C", displayReading(original, false))
        assertEquals("35.0 °C", original.display)
        assertEquals("Unavailable", displayReading(metric("Battery temperature", null), true))
        assertEquals("350 mA", displayReading(metric("Battery current", "350 mA"), true))
        assertEquals("85%", displayReading(metric("Charge level", "85%"), true))
        assertEquals("85 °F", displayReading(metric("Battery temperature", "85 °F"), true))
        assertEquals("invalid °C", displayReading(metric("Battery temperature", "invalid °C"), true))
    }

    @Test fun doesNotMistakeOtherNumbersForTemperature() {
        assertEquals("24.0 °C", displayReading(metric("Thermal headroom", "24.0 °C"), true))
        assertEquals("98.6 °F", displayReading(metric("Thermal sensor temperature", "37.0 °C"), true))
    }
}
