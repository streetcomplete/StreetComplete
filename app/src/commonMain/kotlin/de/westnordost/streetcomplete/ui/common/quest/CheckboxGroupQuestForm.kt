package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

/** Quest form in which the [items] are displayed as a list of checkboxes */
@Composable
fun <I> CheckboxGroupQuestForm(
    items: List<I>,
    itemContent: @Composable (item: I) -> Unit,
    onClickOk: (selectedItems: Set<I>) -> Unit,
    modifier: Modifier = Modifier,
    otherAnswers: List<Answer> = emptyList(),
) {
    var selectedItemIndices by rememberSerializable { mutableStateOf(emptySet<Int>()) }
    val selectedItems by remember {
        derivedStateOf { selectedItemIndices.mapTo(HashSet()) { items[it] } }
    }

    QuestForm(
        answers = Form(
            isComplete = selectedItemIndices.isNotEmpty(),
            onClickOk =  { onClickOk(selectedItems) }
        ),
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_multiselect_hint))
            }
            CheckboxGroup(
                options = items,
                onSelectionChange = { option, selected ->
                    val index = items.indexOf(option)
                    if (index != -1) {
                        selectedItemIndices =
                            if (selected) { selectedItemIndices + index }
                            else { selectedItemIndices - index }
                    }
                },
                selectedOptions = selectedItems,
                itemContent = { itemContent(it) }
            )
        }
    }
}
