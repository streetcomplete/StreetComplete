package de.westnordost.streetcomplete.overlays.buildings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.overlays.Edit
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.building.BuildingStatus
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingTypeAndStatus
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.createBuildingTypeAndStatus
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.GroupedItemPairSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.overlay.GroupedItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.util.ReplaceBottomSheetTransitionSpec
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun BuildingsOverlayForm(
    on: (OverlayAction) -> Unit,
    element: Element,
    preferences: Preferences = koinInject()
) {
    val originalBuildingAndStatus = remember(element) { createBuildingTypeAndStatus(element.tags) }
    val originalShowPairLayout =
        originalBuildingAndStatus.status != null && originalBuildingAndStatus.type != null
    var switchToPairLayout by remember { mutableStateOf(false) }

    // always show house number, never show feature name (because type of building
    // is already shown in the form itself)
    val label = nameAndLocationLabel(element, featureDictionary = null, showHouseNumber = true)

    AnimatedContent(
        targetState = originalShowPairLayout || switchToPairLayout,
        transitionSpec = ReplaceBottomSheetTransitionSpec,
    ) { pairLayout ->
        if (pairLayout) {
            GroupedItemPairSelectOverlayForm(
                on = on,

                label1 = stringResource(Res.string.overlay_buildings_original_use),
                groups1 = BuildingTypeCategory.buildingGroups,
                initialSelectedItem1 = originalBuildingAndStatus.type,
                groupContent1 = { BuildingTypeCategoryItem(it) },
                itemContent1 = { BuildingStatusItem(it) },

                label2 = stringResource(Res.string.overlay_buildings_current_use),
                groups2 = BuildingTypeCategory.statusGroups,
                initialSelectedItem2 = originalBuildingAndStatus.status,
                groupContent2 = { BuildingTypeCategoryItem(it) },
                itemContent2 = { BuildingStatusItem(it) },

                onClickOk = { (type, status) ->
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    BuildingTypeAndStatus(type, status).applyTo(tagChanges)
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                label = label,
            )
        }
        else {
            GroupedItemSelectOverlayForm(
                on = on,
                groups = BuildingTypeCategory.statusGroups,
                topSelectableItems = BuildingType.topSelectableValues,
                initialSelectedItem = originalBuildingAndStatus.status ?: originalBuildingAndStatus.type,
                groupContent = { BuildingTypeCategoryItem(it) },
                itemContent = { BuildingStatusItem(it) },
                lastPickedItemContent = { item ->
                    Image(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.title),
                        modifier = Modifier.height(24.dp)
                    )
                },
                onClickOk = { selectedItem ->
                    // in single-item select, we tag the status if a status was selected and
                    // otherwise the building type
                    val buildingTypeAndStatus = if (selectedItem is BuildingType) {
                        BuildingTypeAndStatus(selectedItem, null)
                    } else {
                        BuildingTypeAndStatus(null, selectedItem)
                    }
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    buildingTypeAndStatus.applyTo(tagChanges)
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                prefs = preferences,
                favoriteKey = "BuildingsOverlayForm",
                label = label,
                otherAnswers = { listOf(
                    AnswerItem(
                        text = stringResource(Res.string.overlay_buildings_different_current_use2),
                        action = { switchToPairLayout = true }
                    )
                ) }
            )
        }
    }
}

@Composable
private fun BuildingStatusItem(item: BuildingStatus, modifier: Modifier = Modifier) {
    ImageWithDescription(
        painter = painterResource(item.icon),
        title = stringResource(item.title),
        description = item.description?.let { stringResource(it) },
        imageSize = DpSize(48.dp, 48.dp),
        modifier = modifier,
    )
}

@Composable
private fun <I> BuildingTypeCategoryItem(item: Group<BuildingTypeCategory, I>, modifier: Modifier = Modifier) {
    ImageWithDescription(
        painter = painterResource(item.category.icon),
        title = stringResource(item.category.title),
        description = item.category.description?.let { stringResource(it) },
        imageSize = DpSize(48.dp, 48.dp),
        modifier = modifier,
    )
}
