package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.links

@Composable
fun LazyGroupedLinksColumn(
    allLinks: List<Link>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val groupedLinks = remember(allLinks) {
        allLinks.groupBy { it.category }.map { (k, v) -> k to v }
    }
    LazyLinksGrid(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        for ((category, links) in groupedLinks) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                contentType = category::class
            ) {
                LinkCategoryRow(category)
            }
            items(links, contentType = { it::class }) { link ->
                LinkRow(link)
            }
        }
    }
}

@Composable
fun LazyLinksColumn(
    links: List<Link>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyLinksGrid(modifier, contentPadding = contentPadding) {
        items(links) { link ->
            LinkRow(link)
        }
    }
}

@Composable
private fun LazyLinksGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@PreviewScreenSizes
@Composable
private fun PreviewLazyGroupedLinksColumn() {
    LazyGroupedLinksColumn(
        allLinks = links,
        contentPadding = PaddingValues(16.dp)
    )
}
