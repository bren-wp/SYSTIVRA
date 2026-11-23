// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.brendigo.systivra.ui.LocalizedText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

internal fun pageCount(total: Int, pageSize: Int): Int {
    require(pageSize > 0)
    return if (total <= 0) 1 else 1 + (total - 1) / pageSize
}

internal fun <T> pageItems(items: List<T>, page: Int, pageSize: Int): List<T> {
    val index = page.coerceIn(0, pageCount(items.size, pageSize) - 1)
    return items.drop(index * pageSize).take(pageSize)
}

@Composable
internal fun PageControls(
    total: Int,
    pageSize: Int,
    page: Int,
    onPageChange: (Int) -> Unit,
    tag: String
) {
    val pages = pageCount(total, pageSize)
    if (pages < 2) return
    val current = page.coerceIn(0, pages - 1)
    Row(
        modifier = Modifier.fillMaxWidth().testTag("$tag-pages"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = { onPageChange(current - 1) },
            enabled = current > 0,
            modifier = Modifier.testTag("$tag-previous")
        ) { Text("Previous") }
        Text("${current + 1} / $pages")
        TextButton(
            onClick = { onPageChange(current + 1) },
            enabled = current + 1 < pages,
            modifier = Modifier.testTag("$tag-next")
        ) { Text("Next") }
    }
}
