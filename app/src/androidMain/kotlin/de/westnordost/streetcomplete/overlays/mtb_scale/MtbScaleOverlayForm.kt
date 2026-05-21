package de.westnordost.streetcomplete.overlays.mtb_scale

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.applyTo
import de.westnordost.streetcomplete.osm.mtb_scale.description
import de.westnordost.streetcomplete.osm.mtb_scale.icon
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.title
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.ItemSelectOverlayForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

@Composable
fun MtbScaleOverlayForm(
    onEdit: (ElementEditAction) -> Unit,
    element: Element,
    preferences: Preferences = koinInject(),
) {
    val originalMtbScaleValue = remember(element) { parseMtbScale(element.tags)?.value }

    ItemSelectOverlayForm(
        itemsPerRow = 1,
        items = MtbScale.Value.entries,
        initialSelectedItem = originalMtbScaleValue,
        itemContent = { item ->
            ImageWithDescription(
                painter = painterResource(item.icon),
                title = stringResource(item.title),
                description = stringResource(item.description)
            )
        },
        lastPickedItemContent = { item ->
            Text(stringResource(item.title))
        },
        onClickOk = { selectedItem ->
            val tagChanges = StringMapChangesBuilder(element.tags)
            MtbScale(selectedItem).applyTo(tagChanges)
            onEdit(UpdateElementTagsAction(element, tagChanges.create()))
        },
        prefs = preferences,
        favoriteKey = "MtbScaleOverlayForm",
    )
}
