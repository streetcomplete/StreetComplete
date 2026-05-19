package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.ui.common.RadioGroup

/** Quest form in which the [items] are displayed as a list of radio buttons */
@Composable
fun <I> RadioGroupQuestForm(
    items: List<I>,
    itemContent: @Composable (item: I) -> Unit,
    onClickOk: (selectedItem: I) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: List<Answer> = emptyList(),
) {
    var checkedItemIndex by rememberSaveable(items) { mutableStateOf<Int>(-1) }
    val checkedItem by remember {
        derivedStateOf { checkedItemIndex.takeIf { it != -1 }?.let { items[it] } }
    }
    QuestForm(
        isComplete = checkedItem != null,
        onClickOk = { onClickOk(checkedItem!!) },
        modifier = modifier,
        otherAnswers = otherAnswers
    ) {
        RadioGroup(
            options = items,
            onSelectionChange = { checkedItemIndex = items.indexOf(it) },
            selectedOption = checkedItem,
            itemContent = { itemContent(it) }
        )
    }
}
