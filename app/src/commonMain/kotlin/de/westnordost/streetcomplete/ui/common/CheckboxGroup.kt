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
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

/** A group composed of a list of [options]. Multiple can be selected. */
@Composable
fun <T> CheckboxGroup(
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
                    onValueChange = { onSelectionChange(option, it) },
                    role = Role.Checkbox,
                )
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
    val items = (1..10).toList()
    val selection = remember { mutableStateSetOf<Int>() }

    CheckboxGroup(
        options = items,
        onSelectionChange = { option: Int, selected: Boolean ->
            if (selected) selection.add(option)
            else selection.remove(option)
        },
        selectedOptions = selection,
        itemContent = { Text(it.toString()) }
    )
}
