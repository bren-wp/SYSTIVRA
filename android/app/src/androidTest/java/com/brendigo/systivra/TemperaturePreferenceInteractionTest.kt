// SYSTIVRA by Brendigo
package com.brendigo.systivra

import android.content.Context
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.AppRoot
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Tests real settings, persisted state, converted UI and original-value safety. */
class TemperaturePreferenceInteractionTest {
    @get:Rule val rule = createComposeRule()

    private fun preferences() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("systivra_settings", Context.MODE_PRIVATE)

    @After fun restoreDefault() {
        preferences().edit().remove("temperature_unit").commit()
    }

    @Test fun selectingFahrenheitPersistsAndAppearsInBatteryDetails() {
        preferences().edit().putString("temperature_unit", "C").commit()
        val snapshot = DeviceSnapshot.empty().copy(
            battery = listOf(
                Reading("Charge level", "80%", "Android battery broadcast"),
                Reading("Battery temperature", "35.0 °C", "Android battery broadcast")
            )
        )
        rule.setContent {
            AppRoot(
                snapshot = snapshot,
                samples = emptyList(),
                recorded = emptyList(),
                isRecording = false,
                onToggleRecording = {},
                onClearRecording = {},
                initialRoute = "settings"
            )
        }
        rule.onNodeWithText("Units").performClick()
        rule.onNodeWithTag("units-fahrenheit").performClick()
        assertEquals("F", preferences().getString("temperature_unit", "C"))
        rule.onNodeWithText("Close").performClick()
        rule.onNodeWithTag("reference-bottom-tab-dashboard").performClick()
        rule.onNodeWithText("Battery").performClick()
        // A single tap now reveals the temperature without a long scroll
        // through duplicated raw readings below the reference overview.
        rule.onNodeWithText("Battery temperature").performClick()
        rule.onNodeWithTag("selected-metric-battery-details")
            .assert(hasAnyDescendant(hasText("95.0 °F")))
    }

    @Test fun settingsUnitsCanReturnToCelsiusWithoutResettingOtherPreferences() {
        preferences().edit().putString("temperature_unit", "F")
            .putInt("sampling_seconds", 3).commit()
        rule.setContent {
            AppRoot(
                snapshot = DeviceSnapshot.empty(),
                samples = emptyList(),
                recorded = emptyList(),
                isRecording = false,
                onToggleRecording = {},
                onClearRecording = {},
                initialRoute = "settings"
            )
        }
        rule.onNodeWithText("Units").performClick()
        rule.onNodeWithTag("units-celsius").performClick()
        assertEquals("C", preferences().getString("temperature_unit", "F"))
        assertEquals(3, preferences().getInt("sampling_seconds", -1))
    }
}
