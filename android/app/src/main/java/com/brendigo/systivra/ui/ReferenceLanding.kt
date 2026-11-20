package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
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

    LazyColumn(
        modifier = Modifier.testTag("reference-landing-$route"),
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading(title, subtitle, caption) }
        item {
            // The shared reference hero is a primary action for this route,
            // not an unrelated shortcut to Device & Android on every screen.
            ReferenceHeroCard { open(actions.first().destination) }
        }
        item { Spacer(Modifier.height(0.dp)) }
        itemsIndexed(actions) { index, action ->
            ReferenceMetricRow(
                action.title,
                listOf("◇", "▤", "◉", "⌁")[index],
                index
            ) {
                open(action.destination)
            }
        }
    }
}
