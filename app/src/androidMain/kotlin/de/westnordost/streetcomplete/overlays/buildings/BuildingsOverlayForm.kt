package de.westnordost.streetcomplete.overlays.buildings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.GroupedItemSelectOverlayForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class BuildingsOverlayForm : AbstractOverlayForm() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        GroupedItemSelectOverlayForm(
            groups = BuildingTypeCategory.entries,
            initialSelectedItem = remember { createBuildingType(element!!.tags) },
            topSelectableItems = BuildingType.topSelectableValues,
            groupContent = { group ->
                ImageWithDescription(
                    painter = painterResource(group.icon),
                    title = stringResource(group.title),
                    description = group.description?.let { stringResource(it) },
                    imageSize = DpSize(48.dp, 48.dp)
                )
            },
            itemContent = { item ->
                ImageWithDescription(
                    painter = painterResource(item.icon),
                    title = stringResource(item.title),
                    description = item.description?.let { stringResource(it) },
                    imageSize = DpSize(48.dp, 48.dp)
                )
            },
            lastPickedItemContent = { item ->
                Image(
                    painter = painterResource(item.icon),
                    contentDescription = stringResource(item.title),
                    modifier = Modifier.height(24.dp)
                )
            },
            onClickOk = { selectedItem ->
                val tagChanges = StringMapChangesBuilder(element!!.tags)
                selectedItem.applyTo(tagChanges)
                applyEdit(UpdateElementTagsAction(element!!, tagChanges.create()))
            },
            prefs = prefs,
            favoriteKey = "BuildingsOverlayForm",
            label = element?.let {
                nameAndLocationLabel(it, featureDictionary = null, showHouseNumber = true)
            },
        )
    }
}
