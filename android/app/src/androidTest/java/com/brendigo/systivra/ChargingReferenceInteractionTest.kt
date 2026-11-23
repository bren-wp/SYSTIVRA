// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.AppRoot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Android screen #13: exact geometry, truthful trend and real action routes. */
class ChargingReferenceInteractionTest {
    @get:Rule val rule = createComposeRule()
    private fun render(samples: List<DeviceSnapshot> = emptyList()) {
        rule.setContent {
            AppRoot(
                snapshot = DeviceSnapshot.empty(),
                samples = samples,
                recorded = emptyList(),
                isRecording = false,
                onToggleRecording = {},
                onClearRecording = {},
                initialRoute = "charging-analysis"
            )
        }
    }

    @Test fun chartUsesOriginal170DpViewportAndFour69DpCards() {
        render()
        rule.onNodeWithTag("reference-charging-analysis").assertExists()
        rule.onNodeWithText("Power session").assertExists()
        val chart = rule.onNodeWithTag("reference-measured-chart", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val first = rule.onNodeWithTag("reference-row-charge-timeline")
            .getUnclippedBoundsInRoot()
        assertEquals(20f, chart.left.value, 1f)
        assertEquals(170f, (chart.bottom - chart.top).value, 1f)
        assertEquals(69f, (first.bottom - first.top).value, 1f)
        assertEquals(24f, (first.top - chart.bottom).value, 1.5f)
        rule.onNodeWithTag("reference-row-current-if-available").assertExists()
        rule.onNodeWithTag("reference-row-temperature").assertExists()
        // Verify genuine touch scroll on the Android viewport rather than
        // semantics auto-scroll, which deadlocked this test on API 33.
        // Two bounded swipes must reveal the final card. Fail immediately if
        // the card remains clipped; never wait indefinitely for scroll idleness.
        Log.i("SYSTIVRA_SCROLL_QA", "charging: geometry verified; swipe start")
        repeat(2) {
            rule.onNodeWithTag("reference-charging-analysis")
                .performTouchInput { swipeUp() }
        }
        Log.i("SYSTIVRA_SCROLL_QA", "charging: swipes finished; checking last row")
        rule.onNodeWithTag("reference-row-session-duration").assertIsDisplayed()
        Log.i("SYSTIVRA_SCROLL_QA", "charging: last row visible")
    }

    @Test fun blankChartDoesNotPretendAnIllustrationIsMeasured() {
        render()
        rule.onNodeWithText("Waiting for measured samples").assertExists()
        rule.onNodeWithText("No illustrative or fabricated readings").assertExists()
    }

    @Test fun tappingChartOpensActualLiveBatteryTimeline() {
        val readings = listOf(62, 63).mapIndexed { index, charge ->
            DeviceSnapshot.empty().copy(time = (index + 1).toLong(), battery = listOf(
                Reading("Charge level", "$charge%", "Android battery broadcast")
            ))
        }
        render(readings)
        rule.onNodeWithText("Recorded device samples · measured percentages").assertExists()
        rule.onNodeWithTag("reference-charging-chart-target").performClick()
        rule.onNodeWithText("Battery Charge · Live").assertExists()
    }

    @Test fun currentRowShowsRealAndroidDataRatherThanPlaceholder() {
        render()
        rule.onNodeWithText("Current if available").performClick()
        rule.onNodeWithTag("charging-readings-page").assertExists()
        rule.onNodeWithText("Real Android battery readings. No fabricated capacity estimate.").assertExists()
        rule.onNodeWithText("No battery metrics available from Android.").assertExists()
    }
}
