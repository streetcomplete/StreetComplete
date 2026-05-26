package de.westnordost.streetcomplete.overlays.cycleway

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.applyTo
import de.westnordost.streetcomplete.osm.cycleway_separate.getIcon
import de.westnordost.streetcomplete.osm.cycleway_separate.parseSeparateCycleway
import de.westnordost.streetcomplete.osm.cycleway_separate.title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun SeparateCyclewayForm(
    onEdit: (UpdateElementTagsAction) -> Unit,
    element: Element,
    countryInfo: CountryInfo,
    preferences: Preferences = koinInject(),
) {
    val originalSeparateCycleway = remember(element) { parseSeparateCycleway(element.tags) }

    ItemSelectOverlayForm(
        itemsPerRow = 1,
        items = SeparateCycleway.entries,
        initialSelectedItem = originalSeparateCycleway,
        itemContent = { item ->
            ImageWithDescription(
                painter = painterResource(item.getIcon(countryInfo.isLeftHandTraffic)),
                title = null,
                description = stringResource(item.title)
            )
        },
        lastPickedItemContent = { item ->
            val icon = item.getIcon(countryInfo.isLeftHandTraffic)
            Image(painterResource(icon), stringResource(item.title), Modifier.height(32.dp))
        },
        onClickOk = { selectedItem ->
            val tagChanges = StringMapChangesBuilder(element.tags)
            selectedItem.applyTo(tagChanges)
            onEdit(UpdateElementTagsAction(element, tagChanges.create()))
        },
        prefs = preferences,
        favoriteKey = "SeparateCyclewayForm",
    )
}
