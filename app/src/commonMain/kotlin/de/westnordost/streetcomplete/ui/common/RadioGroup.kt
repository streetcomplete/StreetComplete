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
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview

/** A radio button group composed of a list of [options]. */
@Composable
fun <T> RadioGroup(
    options: List<T>,
    onSelectionChange: (T) -> Unit,
    selectedOption: T?,
    itemContent: @Composable BoxScope.(T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        options.forEach { option ->
            Row(Modifier
                .clip(MaterialTheme.shapes.small)
                .selectable(
                    selected = (option == selectedOption),
                    onClick = { onSelectionChange(option) },
                    role = Role.RadioButton
                )
                .padding(8.dp)
            ) {
                RadioButton(
                    selected = option == selectedOption,
                    // the whole row should be selectable, not only the radio button
                    onClick = null,
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
private fun RadioGroupPreview() {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    RadioGroup(
        options = listOf(0,1,2,3),
        onSelectionChange = { selectedOption = it },
        selectedOption = selectedOption,
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
