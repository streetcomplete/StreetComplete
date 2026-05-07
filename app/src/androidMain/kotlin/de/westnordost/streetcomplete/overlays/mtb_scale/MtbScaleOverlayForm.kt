package de.westnordost.streetcomplete.overlays.mtb_scale

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
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

class MtbScaleOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        ItemSelectOverlayForm(
            itemsPerRow = 1,
            items = MtbScale.Value.entries,
            initialSelectedItem = remember { parseMtbScale(element!!.tags)?.value },
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
                val tagChanges = StringMapChangesBuilder(element!!.tags)
                MtbScale(selectedItem).applyTo(tagChanges)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            },
            prefs = prefs,
            favoriteKey = "MtbScaleOverlayForm",
        )
    }
}
