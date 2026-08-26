package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.overlay_buildings_current_use
import de.westnordost.streetcomplete.resources.overlay_buildings_no_current_use
import de.westnordost.streetcomplete.resources.overlay_buildings_original_use
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.VerticalDivider
import de.westnordost.streetcomplete.ui.common.dialogs.GroupedItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Overlay form that displays a SideBySideLayout, with similar working to [GroupedItemSelectOverlayForm]
 *  but allowing [UpdateElementTagsAction] for both currentBuilding (building:use)
 *  alongside originalBuilding (building). */
@Composable
inline fun <reified G: Group<I>, reified I> SideBySideLayoutForm (
    noinline on: (Action) -> Unit,
    groups: List<G>,
    originalSelectedItem: I?,
    currentSelectedItem: I?,
    excludedBuildingItems: List<I> = emptyList(),
    excludedBuildingUseItems: List<I> = emptyList(),
    noinline groupContent: @Composable (group: G) -> Unit,
    noinline groupItemContent: @Composable (item: I) -> Unit,
    noinline cardItemContent: @Composable (item:I) -> Unit,
    noinline itemIcon: (I) -> DrawableResource?,
    noinline itemLabel: (I) -> String,
    noinline itemColor: (I) -> Color?,
    crossinline onClickOk: (selectedBuilding: I, selectedBuildingUse: I?) -> Unit,
    modifier: Modifier = Modifier,
    isComplete: Boolean = true,
    featureDictionary: FeatureDictionary = koinInject(),
    label: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    noinline otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
){
    var selectedBuilding by rememberSerializable(originalSelectedItem) { mutableStateOf(originalSelectedItem)}
    var selectedBuildingUse by rememberSerializable(currentSelectedItem) { mutableStateOf(currentSelectedItem)}
    val currentUsePrompt = stringResource(Res.string.overlay_buildings_current_use)
    val noCurrentUsePrompt = stringResource(Res.string.overlay_buildings_no_current_use)
    val originalUsePrompt = stringResource(Res.string.overlay_buildings_original_use)
    OverlayForm(
        on = on,
        isComplete = isComplete && selectedBuilding != null,
        hasChanges = selectedBuilding != originalSelectedItem || selectedBuildingUse != currentSelectedItem,
        onClickOk = {
            val building = selectedBuilding!!
            val buildingUse = selectedBuildingUse
            onClickOk(building,buildingUse)
        },
        modifier = modifier,
        label = label,
        otherAnswers = otherAnswers,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Building
            BuildingUseColumn(
                modifier = Modifier.weight(1f),
                headerLabel = "$originalUsePrompt ${selectedBuilding?.let(itemLabel)}",
                selected = selectedBuilding,
                accentColor = selectedBuilding?.let(itemColor) ?: MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                icon = selectedBuilding?.let(itemIcon),
                itemContent = cardItemContent,
                itemSelectDialog = { expanded, onExpandedChange ->
                    if (expanded) {
                        GroupedItemSelectDialog(
                            onDismissRequest = { onExpandedChange(false) },
                            groups = groups,
                            onSelected = { selectedBuilding = it },
                            groupContent = groupContent,
                            itemContent = groupItemContent,
                            excludedItems = excludedBuildingItems,
                        )
                    }
                }
            )

            VerticalDivider(thickness = 12.dp)

            // Building:use
            BuildingUseColumn(
                modifier = Modifier.weight(1f),
                headerLabel = selectedBuildingUse?.let {"$currentUsePrompt ${itemLabel(it)}"} ?: noCurrentUsePrompt,
                selected = selectedBuildingUse,
                accentColor = selectedBuildingUse?.let(itemColor) ?: MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                icon = selectedBuildingUse?.let(itemIcon),
                itemContent = cardItemContent,
                itemSelectDialog = { expanded, onExpandedChange ->
                    if (expanded) {
                        GroupedItemSelectDialog(
                            onDismissRequest = { onExpandedChange(false) },
                            groups = groups,
                            onSelected = { selectedBuildingUse = it },
                            groupContent = groupContent,
                            itemContent = groupItemContent,
                            excludedItems = excludedBuildingUseItems,
                        )
                    }
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <I> BuildingUseColumn(
    headerLabel: String,
    selected: I?,
    accentColor: Color,
    icon: DrawableResource?,
    itemContent: @Composable (I) -> Unit,
    itemSelectDialog: @Composable (expanded: Boolean, onExpandedChange: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val circleModifier = Modifier
        .size(120.dp)
        .clip(CircleShape)
        .background(accentColor.copy(alpha = 0.32f))

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = headerLabel,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(20.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected != null && icon != null) {
                        Box(
                            modifier = circleModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(icon),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = circleModifier.border(
                                BorderStroke(8.dp, accentColor.copy(alpha = 0.4f)),
                                CircleShape,
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "?",
                                style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.Bold),
                                color = accentColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ItemCard(
                        item = selected,
                        expanded = expanded,
                        onExpandChange = { expanded = it },
                        content = itemContent,
                    )
                }
            }
        }
        itemSelectDialog(expanded) { expanded = it }
        Spacer(Modifier.size(4.dp))
    }
}
