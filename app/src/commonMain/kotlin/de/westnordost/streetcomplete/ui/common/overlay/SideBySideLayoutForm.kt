package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.osm.building.icon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.floating_question
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.resources.none
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
inline fun <reified G: Group<I>, reified I> SideBySideLayoutForm (
    noinline on: (Action) -> Unit,
    groups: List<G>,
    initialOriginalItem: I?,
    initialCurrentItem: I?,
    excludedFromOriginalSelection: List<I> = emptyList(),
    excludedFromCurrentSelection: List<I> = emptyList(),
    noinline groupContent: @Composable (group: G) -> Unit,
    noinline groupItemContent: @Composable (item: I) -> Unit,
    noinline itemContent: @Composable (item: I) -> Unit,
    noinline itemIcon: (I) -> DrawableResource?,
    noinline itemLabel: (I) -> String,
    crossinline onClickOk: (selectedOriginalItem: I, selectedCurrentItem: I?) -> Unit,
    modifier: Modifier = Modifier,
    isComplete: Boolean = true,
    featureDictionary: FeatureDictionary = koinInject(),
    label: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    primaryPrompt: String,
    secondaryPrompt: String,
    emptySecondaryPrompt: String,
    noinline otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
) {
    var selectedOriginalItem by rememberSerializable(initialOriginalItem) {
        mutableStateOf(
            initialOriginalItem
        )
    }
    var selectedCurrentItem by rememberSerializable(initialCurrentItem) {
        mutableStateOf(
            initialCurrentItem
        )
    }
    OverlayForm(
        on = on,
        isComplete = isComplete && selectedOriginalItem != null,
        hasChanges = selectedOriginalItem != initialOriginalItem || selectedCurrentItem != initialCurrentItem,
        onClickOk = {
            selectedOriginalItem?.let {
                onClickOk(it, selectedCurrentItem)
            }
        },
        modifier = modifier,
        label = label,
        otherAnswers = otherAnswers,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Original State
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedOriginalItem?.let { "$primaryPrompt ${itemLabel(it)}" }
                        ?: "$primaryPrompt ?",
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                selectedOriginalItem?.let { item ->
                    itemIcon(item)?.let { icon ->
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                } ?: Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("?", style = MaterialTheme.typography.h2.copy(lineHeight = 60.sp), color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ItemCard(
                        item = selectedOriginalItem,
                        expanded = expanded,
                        onExpandChange = { expanded = it },
                        content = itemContent,
                    )
                    if (expanded) {
                        GroupedItemSelectDialog(
                            onDismissRequest = { expanded = false },
                            groups = groups,
                            onSelected = { selectedOriginalItem = it },
                            groupContent = groupContent,
                            itemContent = groupItemContent,
                            excludedItems = excludedFromOriginalSelection,
                        )
                    }
                }
            }

            VerticalDivider(thickness = 12.dp)

            // Current State
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedCurrentItem?.let { "$secondaryPrompt ${itemLabel(it)}" }
                        ?: emptySecondaryPrompt,
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                selectedCurrentItem?.let { item ->
                    itemIcon(item)?.let { icon ->
                        Image(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                } ?: Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("?", style = MaterialTheme.typography.h2.copy(lineHeight = 60.sp), color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                }
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ItemCard(
                        item = selectedCurrentItem,
                        expanded = expanded,
                        onExpandChange = { expanded = it },
                        content = itemContent,
                    )
                    if (expanded) {
                        GroupedItemSelectDialog(
                            onDismissRequest = { expanded = false },
                            groups = groups,
                            onSelected = { selectedCurrentItem = it },
                            groupContent = groupContent,
                            itemContent = groupItemContent,
                            excludedItems = excludedFromCurrentSelection,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(48.dp))
    }
}
