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
import de.westnordost.streetcomplete.osm.building.BuildingType
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.building.BuildingTypeCategory
import de.westnordost.streetcomplete.osm.building.applyBuildingUseTo
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.createBuildingUseType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.minus
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.resources.*
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
    val originalBuilding = remember(element) { createBuildingType(element.tags) }
    val originalBuildingUse = remember(element) { createBuildingUseType(element.tags) }

    val groupsPair = remember {
        Pair(
            // building: no building is constructed as historic, ruins, abandoned or construction
            BuildingTypeCategory.entries - setOf(UNSUPPORTED, HISTORIC, RUINS, ABANDONED, CONSTRUCTION),
            // building use
            BuildingTypeCategory.entries - setOf(UNSUPPORTED, CONSTRUCTION)
        )
    }

    var switchToPairLayout by remember { mutableStateOf(false) }

    // always show house number, never show feature name (because type of building
    // is already shown in the form itself)
    val label = nameAndLocationLabel(element, featureDictionary = null, showHouseNumber = true)

    AnimatedContent(
        targetState = originalBuildingUse != null || switchToPairLayout,
        transitionSpec = ReplaceBottomSheetTransitionSpec,
    ) { pairLayout ->
        if (pairLayout) {
            GroupedItemPairSelectOverlayForm(
                on = on,
                groupsPair = groupsPair,
                initialSelectedItemPair = Pair(originalBuilding, originalBuildingUse),
                groupContent = {  BuildingTypeCategoryItem(it.category) },
                itemContent = { BuildingTypeItem(it) },
                onClickOk = { (selectedBuilding, selectedBuildingUse) ->
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    selectedBuilding.applyTo(tagChanges)
                    selectedBuildingUse.applyBuildingUseTo(tagChanges)
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                labels = Pair(
                    stringResource(Res.string.overlay_buildings_original_use),
                    stringResource(Res.string.overlay_buildings_current_use),
                ),
                label = label,
            )
        }
        else {
            GroupedItemSelectOverlayForm(
                on = on,
                groups = BuildingTypeCategory.entries,
                topSelectableItems = BuildingType.topSelectableValues,
                initialSelectedItem = originalBuilding,
                groupContent = { BuildingTypeCategoryItem(it) },
                itemContent = { BuildingTypeItem(it) },
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
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                prefs = preferences,
                favoriteKey = "BuildingsOverlayForm",
                label = label,
                otherAnswers = { listOf(
                    AnswerItem(
                        text = stringResource(Res.string.overlay_buildings_different_current_use),
                        action = { switchToPairLayout = true }
                    )
                ) }
            )
        }
    }
}

@Composable
private fun BuildingTypeItem(item: BuildingType, modifier: Modifier = Modifier) {
    ImageWithDescription(
        painter = painterResource(item.icon),
        title = stringResource(item.title),
        description = item.description?.let { stringResource(it) },
        imageSize = DpSize(48.dp, 48.dp),
        modifier = modifier,
    )
}

@Composable
private fun BuildingTypeCategoryItem(item: BuildingTypeCategory, modifier: Modifier = Modifier) {
    ImageWithDescription(
        painter = painterResource(item.icon),
        title = stringResource(item.title),
        description = item.description?.let { stringResource(it) },
        imageSize = DpSize(48.dp, 48.dp),
        modifier = modifier,
    )
}
