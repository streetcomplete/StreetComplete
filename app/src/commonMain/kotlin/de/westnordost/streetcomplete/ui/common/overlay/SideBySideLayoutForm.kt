package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.dialogs.GroupedItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.koin.compose.koinInject

/** Overlay form that displays a SideBySideLayout, with similar working to [GroupedItemSelectOverlayForm]
 *  but allowing [UpdateElementTagsAction] currently for -
 *  currentBuilding (building:use) & originalBuilding (building). */
@OptIn(ExperimentalMaterialApi::class)
@Composable
inline fun <reified G: Group<I>, reified I> SideBySideLayoutForm (
    noinline on: (Action) -> Unit,
    groups: Pair<List<G>, List<G>>,
    initialSelectedItemPair: Pair<I, I?>,
    noinline groupContent: @Composable (group: G) -> Unit,
    noinline groupItemContent: @Composable (item: I) -> Unit,
    noinline itemContent: @Composable (item: I) -> Unit,
    crossinline onClickOk: (Pair<I, I?>) -> Unit,
    modifier: Modifier = Modifier,
    isComplete: Boolean = true,
    featureDictionary: FeatureDictionary = koinInject(),
    label: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    prompts: Pair<String, String>,
    noinline otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
) {
    var selectedItemPair by rememberSerializable(initialSelectedItemPair) {
        mutableStateOf(initialSelectedItemPair)
    }
    var expandedIndex by remember { mutableIntStateOf(-1) }

    OverlayForm(
        on = on,
        isComplete = isComplete,
        hasChanges = selectedItemPair.first != initialSelectedItemPair.first || selectedItemPair.second != initialSelectedItemPair.second,
        onClickOk = {
            onClickOk(selectedItemPair)
        },
        modifier = modifier,
        label = label,
        otherAnswers = otherAnswers,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            selectedItemPair.toList().forEachIndexed { index, selectedItem ->
                Text(
                    text = when {
                        // Original use
                        index == 0 -> prompts.first
                        // Current use (can be empty/nothing)
                        else -> prompts.second
                    },
                    style = MaterialTheme.typography.caption.copy(
                        hyphens = Hyphens.Auto,
                        textAlign = TextAlign.Center,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                    ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ItemCard(
                        modifier = Modifier
                            .conditional(selectedItem){
                                fillMaxWidth()
                            },
                        item = selectedItem,
                        expanded = expandedIndex == index,
                        onExpandChange = { expandedIndex = if (it) index else -1 },
                        content = itemContent,
                    )
                }
            }
        }
    }
    if (expandedIndex != -1) {
        GroupedItemSelectDialog(
            onDismissRequest = { expandedIndex = -1 },
            groups = if (expandedIndex == 0) groups.first else groups.second,
            onSelected = {
                selectedItemPair =
                    if (expandedIndex == 0 ) {
                        selectedItemPair.copy(first = it)
                    }
                    else {
                        selectedItemPair.copy(second = it)
                    }
            },
            groupContent = groupContent,
            itemContent = groupItemContent
        )
    }
}
