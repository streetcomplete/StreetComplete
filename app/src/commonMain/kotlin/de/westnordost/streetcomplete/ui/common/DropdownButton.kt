package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonElevation
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.resources.quest_select_hint
import de.westnordost.streetcomplete.ui.ktx.minus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

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
fun <T> DropdownButton(
    items: List<T>,
    onSelectedItem: (T) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    style: ButtonStyle = ButtonStyle.Default,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    elevation: ButtonElevation? = style.elevation,
    shape: Shape = style.shape,
    border: BorderStroke? = style.border,
    colors: ButtonColors = style.buttonColors,
    contentPadding: PaddingValues = style.contentPadding,
    showDropDownArrow: Boolean = true,
    itemContent: @Composable (RowScope.(item: T) -> Unit),
    content: @Composable (RowScope.() -> Unit) = {
        if (selectedItem != null) itemContent(selectedItem)
        else Text(stringResource(Res.string.quest_select_hint))
    }
) {
    var expanded by remember { mutableStateOf(false) }

    val revisedContentPadding = if (showDropDownArrow) {
        contentPadding - PaddingValues(end = 8.dp)
    } else {
        contentPadding
    }

    Box(modifier = modifier) {
        Button2(
            onClick = { expanded = !expanded },
            enabled = enabled,
            interactionSource = interactionSource,
            elevation = elevation,
            shape = shape,
            border = border,
            colors = colors,
            contentPadding = revisedContentPadding,
        ) {
            content()
            if (showDropDownArrow) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
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
                ) {
                    itemContent(item)
                }
            }
        }
    }
}

@Preview
@Composable
private fun LengthInputSelectorPreview() {
    val words = remember { "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam".split(' ') }
    var selected by remember { mutableStateOf(words[0]) }
    DropdownButton(
        items = words,
        selectedItem = selected,
        onSelectedItem = { selected = it },
        itemContent = { Text(it) }
    )
}
