package com.brendigo.systivra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.ui.AppRoot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** A text label must never visually land in a neighbouring tab's click target. */
class ReferenceBottomBarTest {
    @get:Rule val rule = createComposeRule()

    @Test fun labelsRemainWithinTheirOwnClickableRegionsOnNarrowPhones() {
        rule.setContent {
            Box(Modifier.width(320.dp).height(720.dp)) {
                AppRoot(
                    snapshot = DeviceSnapshot.empty(),
                    samples = emptyList(),
                    recorded = emptyList(),
                    isRecording = false,
                    onToggleRecording = {},
                    onClearRecording = {}
                )
            }
        }

        val ids = listOf("dashboard", "live-monitor", "diagnostics-center", "settings")
        var previousRight = 0f
        ids.forEachIndexed { index, id ->
            val hit = rule.onNodeWithTag("reference-bottom-tab-$id").getUnclippedBoundsInRoot()
            val label = rule.onNodeWithTag(
                "reference-bottom-label-$id", useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            assertTrue("$id label left edge escapes its tab", label.left.value >= hit.left.value - 0.5f)
            assertTrue("$id label right edge escapes its tab", label.right.value <= hit.right.value + 0.5f)
            if (index > 0) {
                assertEquals("$id tab must touch its neighbour", previousRight, hit.left.value, 0.7f)
            }
            previousRight = hit.right.value
        }
        assertEquals(320f, previousRight, 1f)

        // The same region tested geometrically must remain fully interactive.
        rule.onNodeWithTag("reference-bottom-tab-live-monitor").performClick()
        rule.onNodeWithTag("live-monitor-scroll").assertExists()
    }
}
