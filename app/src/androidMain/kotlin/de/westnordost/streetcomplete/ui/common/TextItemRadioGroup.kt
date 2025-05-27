package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

/** A radio button group composed of a list of [options]. */
@Composable
fun <T> TextItemRadioGroup(
    options: List<TextItem<T>>,
    onSelectionChange: (TextItem<T>) -> Unit,
    currentOption: TextItem<T>?,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        options.forEach { option ->
            Row(Modifier
                .selectable(
                    selected = (option == currentOption),
                    onClick = { onSelectionChange(option) },
                    role = Role.RadioButton
                )
                .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = option == currentOption,
                    // the whole row should be selectable, not only the radio button
                    onClick = null,
                )
                Text(
                    text = stringResource(option.titleId),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TextItemRadioGroupFormPreview() {
    var selectedOption by remember { mutableStateOf<TextItem<Int>?>(null) }
    TextItemRadioGroup(
        options = listOf(
            TextItem(0, R.string.quest_accepts_cards_debit_and_credit),
            TextItem(1, R.string.quest_accepts_cards_credit_only),
            TextItem(2, R.string.quest_accepts_cards_dedit_only),
            TextItem(3, R.string.quest_accepts_cards_unavailable),
        ),
        onSelectionChange = { selectedOption = it },
        currentOption = selectedOption
    )
}

data class TextItem<T>(val value: T, val titleId: Int)
