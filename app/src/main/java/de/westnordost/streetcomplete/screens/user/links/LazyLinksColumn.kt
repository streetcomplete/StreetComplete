package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.links

@Composable
fun LazyGroupedLinksColumn(
    allLinks: List<Link>,
    onClickLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // TODO Compose: revisit animate-in of list items when androidx.compose.animation 1.7 is stable
    // probably Modifier.animateItem or Modifier.animateEnterExit
    LazyLinksGrid(modifier, contentPadding = contentPadding) {
        val groupedLinks = allLinks.groupBy { it.category }.map { (k,v) -> k to v }
        for ((category, links) in groupedLinks) {
            item(
                span = { GridItemSpan(maxLineSpan) },
                contentType = category::class
            ) {
                LinkCategoryItem(category)
            }
            items(links, contentType = { it::class }) { link ->
                LinkItem(link, onClickLink)
            }
        }
    }
}

@Composable
fun LazyLinksColumn(
    links: List<Link>,
    onClickLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyLinksGrid(modifier, contentPadding = contentPadding) {
        items(links) { link ->
            LinkItem(link, onClickLink)
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
fun PreviewLazyGroupedLinksColumn() {
    LazyGroupedLinksColumn(links,
        onClickLink = {},
        contentPadding = PaddingValues(16.dp)
    )
}
