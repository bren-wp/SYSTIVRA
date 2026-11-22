// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
internal fun ScreenCatalog(
    routes: List<Pair<String, String>>,
    enabled: Set<String>,
    open: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var requestedPage by rememberSaveable { mutableIntStateOf(0) }
    val matches = filterCatalogRoutes(routes, enabled, category, query)
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().testTag("screen-catalog-page")
    ) {
        val perPage = if (maxHeight < 580.dp) 1 else if (maxHeight < 750.dp) 2 else 3
        val page = requestedPage.coerceIn(0, pageCount(matches.size, perPage) - 1)
        Column(
            modifier = Modifier.fillMaxSize().testTag("catalog-scroll-content")
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Find a function", fontWeight = FontWeight.Bold, fontSize = 21.sp)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; requestedPage = 0 },
                modifier = Modifier.fillMaxWidth().testTag("catalog-search"),
                label = { Text("Search tools") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = TextPrimary,
                    unfocusedLabelColor = Muted,
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Muted,
                    cursorColor = Cyan,
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel
                )
            )
            Column(
                modifier = Modifier.fillMaxWidth().testTag("catalog-categories"),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                catalogCategories.chunked(3).forEach { group ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        group.forEach { label ->
                            FilterChip(
                                selected = category == label,
                                onClick = { category = label; requestedPage = 0 },
                                modifier = Modifier.weight(1f)
                                    .testTag("catalog-category-${label.lowercase(Locale.ROOT)}"),
                                label = {
                                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Panel,
                                    labelColor = TextPrimary,
                                    selectedContainerColor = Color(0xFF153B68),
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
            if (matches.isEmpty()) {
                Text("No tools match. Change category or search.", color = Muted)
                TextButton(
                    onClick = { query = ""; category = "All"; requestedPage = 0 },
                    modifier = Modifier.testTag("catalog-clear-filters")
                ) { Text("Show all tools") }
            } else {
                pageItems(matches, page, perPage).forEach { (id, label) ->
                    LinkCard(label, "Open $label") { open(id) }
                }
                PageControls(
                    total = matches.size, pageSize = perPage, page = page,
                    onPageChange = { requestedPage = it }, tag = "catalog"
                )
            }
        }
    }
}
