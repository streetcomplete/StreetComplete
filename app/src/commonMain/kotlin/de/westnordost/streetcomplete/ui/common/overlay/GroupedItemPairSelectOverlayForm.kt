package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
@Composable
inline fun <C, reified I1, reified I2> GroupedItemPairSelectOverlayForm (
    noinline on: (Action) -> Unit,

    label1: String,
    groups1: List<Group<C, I1>>,
    initialSelectedItem1: I1?,
    noinline groupContent1: @Composable (group: Group<C, I1>) -> Unit,
    noinline itemContent1: @Composable (item: I1) -> Unit,

    label2: String,
    groups2: List<Group<C, I2>>,
    initialSelectedItem2: I2?,
    noinline groupContent2: @Composable (group: Group<C, I2>) -> Unit,
    noinline itemContent2: @Composable (item: I2) -> Unit,
    crossinline onClickOk: (Pair<I1, I2>) -> Unit,

    modifier: Modifier = Modifier,
    isComplete: Boolean = true,
    featureDictionary: FeatureDictionary = koinInject(),
    label: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    noinline otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
) {
    var selectedItem1 by rememberSerializable(initialSelectedItem1) { mutableStateOf(initialSelectedItem1) }
    var selectedItem2 by rememberSerializable(initialSelectedItem2) { mutableStateOf(initialSelectedItem2) }

    OverlayForm(
        on = on,
        isComplete = isComplete && selectedItem1 != null && selectedItem2 != null,
        hasChanges = selectedItem1 != initialSelectedItem1 || selectedItem2 != initialSelectedItem2,
        onClickOk = { onClickOk(Pair(selectedItem1!!, selectedItem2!!)) },
        modifier = modifier,
        label = label,
        otherAnswers = otherAnswers,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GroupedItemPairSelectItem(
                label = label1,
                item = selectedItem1,
                groups = groups1,
                itemContent = itemContent1,
                groupContent = groupContent1,
                onSelected = { selectedItem1 = it },
            )
            GroupedItemPairSelectItem(
                label = label2,
                item = selectedItem2,
                groups = groups2,
                itemContent = itemContent2,
                groupContent = groupContent2,
                onSelected = { selectedItem2 = it },
            )
        }
    }
}

@Composable
fun <C, I> GroupedItemPairSelectItem(
    label: String,
    item: I?,
    groups: List<Group<C, I>>,
    groupContent: @Composable (group: Group<C, I>) -> Unit,
    itemContent: @Composable (item: I) -> Unit,
    onSelected: (I) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
            style = MaterialTheme.typography.caption,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            ItemCard(
                item = item,
                expanded = expanded,
                onExpandChange = { expanded = it },
                content = itemContent,
            )
        }
    }

    if (expanded) {
        GroupedItemSelectDialog(
            onDismissRequest = { expanded = false },
            groups = groups,
            onSelected = onSelected,
            groupContent = groupContent,
            itemContent = itemContent
        )
    }
}
