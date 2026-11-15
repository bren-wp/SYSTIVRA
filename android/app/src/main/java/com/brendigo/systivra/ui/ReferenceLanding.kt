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
        item {
            Column {
                Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text(subtitle, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(caption, fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(18.dp))
            }
        }
        item { ReferenceHeroCard { open("device-and-android") } }
        item { Spacer(Modifier.height(7.dp)) }
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
