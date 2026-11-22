// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Present the original screen design without replacing actual feature destinations. */
internal data class ReferenceAction(val title: String, val destination: String)

@Composable
internal fun ReferenceLanding(
    route: String,
    title: String,
    subtitle: String,
    caption: String,
    actions: List<ReferenceAction>,
    open: (String) -> Unit
) {
    require(actions.size in 1..4) { "Reference layout supports one to four real actions" }

    Column(
        modifier = Modifier.fillMaxSize().testTag("reference-landing-$route").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading(title, subtitle, caption)
        ReferenceHeroCard { open(actions.first().destination) }
        Spacer(Modifier.height(0.dp))
        actions.forEachIndexed { index, action ->
            ReferenceMetricRow(
                action.title, listOf("◇", "▤", "◉", "⌁")[index], index
            ) { open(action.destination) }
        }
    }
}
