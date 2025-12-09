package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Analogous to the RadioGroup, but a list of checkboxes. */
@Composable
fun <T> CheckboxList(
    options: List<T>,
    onToggle: (option: T, checked: Boolean) -> Unit,
    selectedOptions: Set<T>,
    itemContent: @Composable BoxScope.(T) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        options.forEach { option ->
            val checked = option in selectedOptions
            Row(Modifier
                .clip(MaterialTheme.shapes.small)
                .toggleable(checked) { onToggle(option, it) }
                .padding(8.dp)
            ) {
                Checkbox(
                    checked = checked,
                    // the whole row should be selectable, not only the checkbox button
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
private fun CheckboxListPreview() {
    val items = (1..10).toList()
    val selection = remember { mutableStateSetOf<Int>() }
    CheckboxList(
        options = items,
        onToggle = { option, checked ->
            if (checked) selection.add(option)
            else selection.remove(option)
        },
        selectedOptions = selection,
        itemContent = {
            Text(it.toString())
        }
    )
}
