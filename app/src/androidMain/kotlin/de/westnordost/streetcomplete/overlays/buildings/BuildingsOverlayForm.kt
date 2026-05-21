package de.westnordost.streetcomplete.overlays.buildings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.GroupedItemSelectOverlayForm
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun BuildingsOverlayForm(
    onEdit: (ElementEditAction) -> Unit,
    element: Element,
    preferences: Preferences = koinInject()
) {
    val originalBuilding = remember(element) { createBuildingType(element.tags) }

    GroupedItemSelectOverlayForm(
        groups = BuildingTypeCategory.entries,
        initialSelectedItem = originalBuilding,
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
            val tagChanges = StringMapChangesBuilder(element.tags)
            selectedItem.applyTo(tagChanges)
            onEdit(UpdateElementTagsAction(element, tagChanges.create()))
        },
        prefs = preferences,
        favoriteKey = "BuildingsOverlayForm",
        label =
            // always show house number, never show feature name (because type of building is
            // already shown in the form itself)
            nameAndLocationLabel(element, featureDictionary = null, showHouseNumber = true),
    )
}
