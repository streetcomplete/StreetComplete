package de.westnordost.streetcomplete.overlays.buildings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import de.westnordost.streetcomplete.osm.building.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithDescription
import de.westnordost.streetcomplete.ui.common.overlay.GroupedItemSelectOverlayForm
import de.westnordost.streetcomplete.ui.common.overlay.SideBySideLayoutForm
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
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
    val switchToBuildingUsePrompt: StringResource = Res.string.overlay_buildings_different_current_use

    /** Building types excluded from the building selection in [SideBySideLayoutForm]. */
    val excludedBuildingItems = listOf(UNSUPPORTED, RUINS, ABANDONED, CONSTRUCTION)

    /** Building types excluded from the building:use selection in [SideBySideLayoutForm] */
    val excludedBuildingUseItems = listOf(UNSUPPORTED, RUINS, HISTORIC, ABANDONED, CONSTRUCTION)

    val isEligibleForBuildingUse = originalBuilding != null && originalBuilding !in excludedBuildingItems

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
                initialOriginalItem = originalBuilding,
                initialCurrentItem = currentBuildingUse,
                excludedFromOriginalSelection = excludedBuildingItems,
                excludedFromCurrentSelection = excludedBuildingUseItems,
                groupContent = { group ->
                    ImageWithDescription(
                        painter = painterResource(group.icon),
                        title = stringResource(group.title),
                        description = group.description?.let { stringResource(it) },
                        imageSize = DpSize(48.dp, 48.dp)
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
                itemContent = { item ->
                    CompactBuildingItem(item)
                },
                itemIcon = { it.icon },
                itemLabel = { it.osmValue.toString()} ,
                onClickOk = { selectedBuilding, selectedBuildingUse ->
                    val tagChanges = StringMapChangesBuilder(element.tags)
                    selectedBuilding.applyTo(tagChanges)
                    selectedBuildingUse?.applyToBuildingUse(tagChanges)
                    on(Edit(UpdateElementTagsAction(element, tagChanges.create())))
                },
                primaryPrompt = stringResource(Res.string.overlay_buildings_original_use),
                secondaryPrompt = stringResource(Res.string.overlay_buildings_current_use),
                emptySecondaryPrompt = stringResource(Res.string.overlay_buildings_no_current_use),
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
                otherAnswers = {
                    if (isEligibleForBuildingUse) {
                        listOf(
                            AnswerItem(
                                text = stringResource(switchToBuildingUsePrompt),
                                action = { switchToSideBySideLayout = true }
                            )
                        )
                    } else {
                        emptyList()
                    }
                }
            )
        }
    }
}

/** Helper function to create a compact [ItemCard] content for [SideBySideLayoutForm] */
@Composable
private fun CompactBuildingItem(
    item: BuildingType
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(item.title),
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        item.description?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.caption,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
