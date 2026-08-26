package de.westnordost.streetcomplete.overlays.buildings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
import de.westnordost.streetcomplete.osm.building.applyTo
import de.westnordost.streetcomplete.osm.building.applyToBuildingUse
import de.westnordost.streetcomplete.osm.building.createBuildingType
import de.westnordost.streetcomplete.osm.building.createBuildingUseType
import de.westnordost.streetcomplete.osm.building.description
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.osm.building.title
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.overlay_buildings_different_current_use
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.GroupedItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.overlay.SideBySideLayoutForm
import de.westnordost.streetcomplete.ui.common.overlay.SwitchAction
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.StringResource
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
    val currentBuildingUse = remember(element){ createBuildingUseType(element.tags) }

    // Prompt to facilitate switching to building:use editing
    val differentUsePrompt: StringResource = Res.string.overlay_buildings_different_current_use

    /** Excluded building types that shouldn't belong with building:use tagging
     * and also must not be allowed to be changed in [SideBySideLayoutForm],
     * should only be changed in [GroupedItemSelectOverlayForm] (subject to change in future)*/
    val excluded = listOf(UNSUPPORTED, RUINS, HISTORIC, ABANDONED, CONSTRUCTION)

    val isEligibleForBuildingUse = originalBuilding != null && originalBuilding !in excluded

    var switchToSideBySideLayout by remember { mutableStateOf(false) }

    /** show [SideBySideLayoutForm] when there is already a building:use tag OR
    * user clicks on differentUsePrompt, assuming building is eligible for building:use editing */
    val showSideBySideLayout = (currentBuildingUse != null || switchToSideBySideLayout) && isEligibleForBuildingUse

    AnimatedContent(
        targetState = showSideBySideLayout,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(400)
            ) togetherWith fadeOut(
                animationSpec = tween(400)
            )
        },
        label = "form_switch"
    ) { sideBySideLayout ->
        if(sideBySideLayout){
            SideBySideLayoutForm(
                on = on,
                groups = BuildingTypeCategory.entries,
                originalSelectedItem = originalBuilding,
                currentSelectedItem = currentBuildingUse,
                excludedBuildingItems = excluded,
                groupContent = { group ->
                    ImageWithDescription(
                        painter = painterResource(group.icon),
                        title = stringResource(group.title),
                        description = group.description?.let { stringResource(it) },
                        imageSize = DpSize(36.dp, 36.dp)
                    )
                },
                groupItemContent = { item ->
                    ImageWithDescription(
                        painter = painterResource(item.icon),
                        title = stringResource(item.title),
                        description = item.description?.let { stringResource(it) },
                        imageSize = DpSize(48.dp, 48.dp)
                    )
                },
                cardItemContent = { item ->
                    Text(
                        text = stringResource(item.title),
                        style = MaterialTheme.typography.subtitle2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                itemIcon = { it.icon },
                itemLabel = { it.osmValue.toString()} ,
                itemColor = { it.color },
                onClickOk = { selectedBuilding, selectedBuildingUse ->
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    selectedBuilding.applyTo(tagChanges)
                    selectedBuildingUse?.applyToBuildingUse(tagChanges)
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                label =
                    // always show house number, never show feature name (because type of building is
                    // already shown in the form itself)
                    nameAndLocationLabel(element, featureDictionary = null, showHouseNumber = true),
            )
        }
        else {
            GroupedItemSelectOverlayForm(
                on = on,
                groups = BuildingTypeCategory.entries,
                topSelectableItems = BuildingType.topSelectableValues,
                initialSelectedItem = originalBuilding,
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
                switchAction = if(isEligibleForBuildingUse) {
                    SwitchAction(differentUsePrompt) { switchToSideBySideLayout = true }
                } else null,
                onClickOk = { selectedItem ->
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    selectedItem.applyTo(tagChanges)
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                prefs = preferences,
                favoriteKey = "BuildingsOverlayForm",
                label =
                    // always show house number, never show feature name (because type of building is
                    // already shown in the form itself)
                    nameAndLocationLabel(element, featureDictionary = null, showHouseNumber = true),
            )
        }
    }
}
