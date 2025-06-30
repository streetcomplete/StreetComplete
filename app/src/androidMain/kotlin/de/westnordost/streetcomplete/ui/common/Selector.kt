package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import kotlin.collections.forEach

/** A simple selector to select from a couple of values */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> Selector(
    items: List<T>,
    selectedItem: T,
    onSelectedItem: (T) -> Unit,
    modifier: Modifier = Modifier,
    getItemText: (T) -> String = { it.toString() },
    style: TextFieldStyle = TextFieldStyle.Filled,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = style.shape,
    contentPadding: PaddingValues = style.getContentPadding(label != null),
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField2(
            value = getItemText(selectedItem),
            onValueChange = { },
            readOnly = true,
            label = label,
            style = style,
            leadingIcon = leadingIcon,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = shape,
            colors = when (style) {
                TextFieldStyle.Filled -> ExposedDropdownMenuDefaults.textFieldColors()
                TextFieldStyle.Outlined -> ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            },
            contentPadding = contentPadding,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { unit ->
                DropdownMenuItem(
                    onClick = {
                        onSelectedItem(unit)
                        expanded = false
                    }
                ) {
                    Text(
                        text = getItemText(unit),
                        modifier = Modifier.align(Alignment.CenterVertically),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview @Composable
private fun LengthInputSelectorPreview() {
    val words = remember { LoremIpsum(10).values.joinToString(" ").split(' ') }
    var selected by remember { mutableStateOf(words[0]) }
    Selector(
        items = words,
        selectedItem = selected,
        getItemText = { it },
        onSelectedItem = { selected = it },
    )
}
