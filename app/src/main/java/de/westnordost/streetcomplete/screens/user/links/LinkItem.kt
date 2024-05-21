package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.LinkCategory
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.theme.titleSmall

@Composable
fun LinkCategoryItem(category: LinkCategory, modifier: Modifier = Modifier) {
    Column {
        Spacer(modifier = modifier.padding(top = 8.dp))
        Text(stringResource(category.title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(category.description), style = MaterialTheme.typography.body1)
    }
}

private val LinkCategory.title: Int get() = when (this) {
    LinkCategory.INTRO -> R.string.link_category_intro_title
    LinkCategory.EDITORS -> R.string.link_category_editors_title
    LinkCategory.MAPS -> R.string.link_category_maps_title
    LinkCategory.SHOWCASE -> R.string.link_category_showcase_title
    LinkCategory.GOODIES -> R.string.link_category_goodies_title
}

private val LinkCategory.description: Int get() = when (this) {
    LinkCategory.INTRO -> R.string.link_category_intro_description
    LinkCategory.EDITORS -> R.string.link_category_editors_description
    LinkCategory.SHOWCASE -> R.string.link_category_showcase_description
    LinkCategory.MAPS -> R.string.link_category_maps_description
    LinkCategory.GOODIES -> R.string.link_category_goodies_description
}

@Composable
fun LinkItem(link: Link, onClickLink: (url: String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable { onClickLink(link.url) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // if no icon, the space should still be filled
        Box(Modifier.size(44.dp)) {
            if (link.icon != null) {
                Image(painterResource(link.icon), null, Modifier.fillMaxSize())
            }
        }
        Column {
            Text(link.title, style = MaterialTheme.typography.titleSmall)
            if (link.description != null) {
                Text(stringResource(link.description), style = MaterialTheme.typography.body2)
            }
        }
    }
}

@Preview
@Composable
private fun LinkCategoryItemPreview() {
    LinkCategoryItem(LinkCategory.GOODIES)
}

@Preview
@Composable
private fun LinkItemPreview() {
    LinkItem(Link(
        "wiki",
        "https://wiki.openstreetmap.org",
        "OpenStreetMap Wiki",
        LinkCategory.INTRO,
        R.drawable.ic_link_wiki,
        R.string.link_wiki_description
    ), {})
}
