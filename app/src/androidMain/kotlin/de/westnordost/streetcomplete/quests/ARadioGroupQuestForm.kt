package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.ui.common.RadioGroup
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable

abstract class ARadioGroupQuestForm<I : T, T> : AbstractOsmQuestForm<T>() {

    protected abstract val items: List<I>

    @Composable
    override fun Content() {
        var checkedItem by rememberSerializable { mutableStateOf<I?>(null) }
        QuestForm(
            answers = Confirm(
                isComplete = checkedItem != null,
                onClick = { applyAnswer(checkedItem!!) }
            )
        ) {
            RadioGroup(
                options = items,
                onSelectionChange = { checkedItem = it },
                selectedOption = checkedItem,
                itemContent = { ItemContent(it) }
            )
        }
    }

    @Composable protected abstract fun BoxScope.ItemContent(item: I)
}
