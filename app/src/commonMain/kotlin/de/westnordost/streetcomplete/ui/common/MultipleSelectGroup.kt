package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_accepts_cards_credit_only
import de.westnordost.streetcomplete.resources.quest_accepts_cards_debit_and_credit
import de.westnordost.streetcomplete.resources.quest_accepts_cards_dedit_only
import de.westnordost.streetcomplete.resources.quest_accepts_cards_unavailable
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** A group composed of a list of [options]. Multiple can be selected. */
@Composable
fun <T> MultipleSelectGroup(
    options: List<T>,
    onSelectionChange: (T) -> Unit,
    selectedOptions: Set<T>,
    itemContent: @Composable BoxScope.(T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        options.forEach { option ->
            Row(Modifier
                .clip(MaterialTheme.shapes.small)
                .selectable(
                    selected = selectedOptions.contains(option),
                    onClick = { onSelectionChange(option) },
                    role = Role.RadioButton
                )
                .selectableGroup()
                .padding(8.dp)
            ) {
                Checkbox(
                    checked = selectedOptions.contains(option),
                    // the whole row should be selectable, not only the selection button
                    onCheckedChange = null,
                )
                Box(Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp),
                ) {
                    itemContent(option)
                }
            }
        }
    }
}

@Composable
@Preview
private fun TextItemMultipleSelectGroupFormPreview() {
    lateinit var selectedOptions: MutableState<Set<Int>>

    MultipleSelectGroup(
        options = listOf(0,1,2,3),
        onSelectionChange = { option ->
            selectedOptions.value = if (selectedOptions.value.contains(option)) {
                selectedOptions.value + option
            } else {
                selectedOptions.value + option
            }
        },
        selectedOptions = selectedOptions.value,
        itemContent = {
            val text = when (it) {
                0 -> Res.string.quest_accepts_cards_debit_and_credit
                1 -> Res.string.quest_accepts_cards_credit_only
                2 -> Res.string.quest_accepts_cards_dedit_only
                3 -> Res.string.quest_accepts_cards_unavailable
                else -> null
            }
            text?.let { Text(stringResource(text)) }
        }
    )
}
