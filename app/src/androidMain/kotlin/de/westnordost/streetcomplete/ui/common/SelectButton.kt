package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonElevation
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import org.jetbrains.compose.resources.painterResource
import kotlin.collections.forEach

/** A simple selector button to select from a couple of values.
 *
 *  In Material design, it is kind of similar to an exposed dropdown menu, only that the latter is
 *  styled like a text field.
 *
 *  In Apple human interface design guidelines, this would be called a
 *  [Pop-up button](https://developer.apple.com/design/human-interface-guidelines/pop-up-buttons).
 *  */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> SelectButton(
    items: List<T>,
    selectedItem: T,
    onSelectedItem: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    elevation: ButtonElevation? = null,
    itemContent: @Composable (RowScope.(item: T) -> Unit),
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            enabled = enabled,
            interactionSource = interactionSource,
            elevation = elevation,
        ) {
            itemContent(selectedItem)
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        if (item != selectedItem) {
                            onSelectedItem(item)
                        }
                        expanded = false
                    },
                    textStyle = LocalTextStyle.current
                ) {
                    itemContent(item)
                }
            }
        }
    }
}

@Preview @Composable
private fun LengthInputSelectorPreview() {
    val words = remember { LoremIpsum(10).values.joinToString(" ").split(' ') }
    var selected by remember { mutableStateOf(words[0]) }
    SelectButton(
        items = words,
        selectedItem = selected,
        onSelectedItem = { selected = it },
    ) { Text(it) }
}
