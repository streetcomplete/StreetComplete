package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.overlays.Action
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.dialogs.GroupedItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import org.koin.compose.koinInject

/** Similar to [GroupedItemSelectOverlayForm], but there is actually a pair of Items. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
inline fun <reified G: Group<I>, reified I> GroupedItemPairSelectOverlayForm (
    noinline on: (Action) -> Unit,
    groupsPair: Pair<List<G>, List<G>>,
    initialSelectedItemPair: Pair<I?, I?>,
    noinline groupContent: @Composable (group: G) -> Unit,
    noinline groupItemContent: @Composable (item: I) -> Unit,
    noinline itemContent: @Composable (item: I) -> Unit,
    crossinline onClickOk: (Pair<I, I>) -> Unit,
    labels: Pair<String, String>,
    modifier: Modifier = Modifier,
    isComplete: Boolean = true,
    featureDictionary: FeatureDictionary = koinInject(),
    label: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    noinline otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
) {
    var selectedItemPair by rememberSerializable(initialSelectedItemPair) {
        mutableStateOf(initialSelectedItemPair)
    }
    var expandedIndex by remember { mutableIntStateOf(-1) }

    OverlayForm(
        on = on,
        isComplete = isComplete && selectedItemPair.first != null && selectedItemPair.second != null,
        hasChanges = selectedItemPair != initialSelectedItemPair,
        onClickOk = { onClickOk(Pair(selectedItemPair.first!!, selectedItemPair.second!!)) },
        modifier = modifier,
        label = label,
        otherAnswers = otherAnswers,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val labelList = labels.toList()
            selectedItemPair.toList().forEachIndexed { index, selectedItem ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = labelList[index],
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                        style = MaterialTheme.typography.caption,
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        ItemCard(
                            item = selectedItem,
                            expanded = expandedIndex == index,
                            onExpandChange = { expandedIndex = if (it) index else -1 },
                            content = itemContent,
                        )
                    }
                }
            }
        }
    }

    if (expandedIndex != -1) {
        GroupedItemSelectDialog(
            onDismissRequest = { expandedIndex = -1 },
            groups = if (expandedIndex == 0) groupsPair.first else groupsPair.second,
            onSelected = {
                selectedItemPair = Pair(
                    first = if (expandedIndex == 0) it else selectedItemPair.first,
                    second = if (expandedIndex == 1) it else selectedItemPair.second,
                )
            },
            groupContent = groupContent,
            itemContent = groupItemContent
        )
    }
}
