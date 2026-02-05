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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.achievements.LinkCategory
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.link_category_editors_description
import de.westnordost.streetcomplete.resources.link_category_editors_title
import de.westnordost.streetcomplete.resources.link_category_goodies_description
import de.westnordost.streetcomplete.resources.link_category_goodies_title
import de.westnordost.streetcomplete.resources.link_category_intro_description
import de.westnordost.streetcomplete.resources.link_category_intro_title
import de.westnordost.streetcomplete.resources.link_category_maps_description
import de.westnordost.streetcomplete.resources.link_category_maps_title
import de.westnordost.streetcomplete.resources.link_category_showcase_description
import de.westnordost.streetcomplete.resources.link_category_showcase_title
import de.westnordost.streetcomplete.resources.link_wiki
import de.westnordost.streetcomplete.resources.link_wiki_description
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.ui.theme.titleSmall
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** Display a single link category from the link collection */
@Composable
fun LinkCategoryRow(category: LinkCategory, modifier: Modifier = Modifier) {
    Column {
        Spacer(modifier = modifier.padding(top = 8.dp))
        Text(stringResource(category.title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(category.description), style = MaterialTheme.typography.body1)
    }
}

private val LinkCategory.title: StringResource get() = when (this) {
    LinkCategory.INTRO -> Res.string.link_category_intro_title
    LinkCategory.EDITORS -> Res.string.link_category_editors_title
    LinkCategory.MAPS -> Res.string.link_category_maps_title
    LinkCategory.SHOWCASE -> Res.string.link_category_showcase_title
    LinkCategory.GOODIES -> Res.string.link_category_goodies_title
}

private val LinkCategory.description: StringResource get() = when (this) {
    LinkCategory.INTRO -> Res.string.link_category_intro_description
    LinkCategory.EDITORS -> Res.string.link_category_editors_description
    LinkCategory.SHOWCASE -> Res.string.link_category_showcase_description
    LinkCategory.MAPS -> Res.string.link_category_maps_description
    LinkCategory.GOODIES -> Res.string.link_category_goodies_description
}

/** Display a single link from the link collection */
@Composable
fun LinkRow(link: Link, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier
            .clickable { uriHandler.openUri(link.url) }
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
private fun LinkCategoryRowPreview() {
    LinkCategoryRow(LinkCategory.GOODIES)
}

@Preview
@Composable
private fun LinkRowPreview() {
    LinkRow(Link(
        "wiki",
        "https://wiki.openstreetmap.org",
        "OpenStreetMap Wiki",
        LinkCategory.INTRO,
        Res.drawable.link_wiki,
        Res.string.link_wiki_description
    ))
}
