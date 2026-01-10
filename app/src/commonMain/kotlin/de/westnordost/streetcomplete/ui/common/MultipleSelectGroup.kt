package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import de.westnordost.streetcomplete.resources.quest_internet_access_terminal
import de.westnordost.streetcomplete.resources.quest_internet_access_wired
import de.westnordost.streetcomplete.resources.quest_internet_access_wlan
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** A group composed of a list of [options]. Multiple can be selected. */
@Composable
fun <T> MultipleSelectGroup(
    options: List<T>,
    onSelectionChange: (item: T, selected: Boolean) -> Unit,
    selectedOptions: Set<T>,
    itemContent: @Composable BoxScope.(T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.selectableGroup()) {
        options.forEach { option ->
            Row(Modifier
                .clip(MaterialTheme.shapes.small)
                .toggleable(
                    value = selectedOptions.contains(option),
                    onValueChange = { onSelectionChange(option, selectedOptions.contains(option)) },
                    role = Role.Checkbox,
                )
                .selectableGroup()
                .padding(8.dp)
            ) {
                Checkbox(
                    checked = selectedOptions.contains(option),
                    // the whole row should be selectable, not only the selection button
                    onCheckedChange = null,
                )
                Box(
                    Modifier
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
    var selectedOptions by remember { mutableStateOf(emptySet<Int>()) }

    MultipleSelectGroup(
        options = listOf(0, 1, 2),
        onSelectionChange = { option: Int, selected: Boolean ->
            selectedOptions = if (selected) {
                selectedOptions + option
            } else {
                selectedOptions - option
            }
        },
        selectedOptions = selectedOptions,
        itemContent = {
            val text = when (it) {
                0 -> Res.string.quest_internet_access_wlan
                1 -> Res.string.quest_internet_access_terminal
                2 -> Res.string.quest_internet_access_wired
                else -> null
            }
            text?.let { Text(stringResource(text)) }
        }
    )
}
