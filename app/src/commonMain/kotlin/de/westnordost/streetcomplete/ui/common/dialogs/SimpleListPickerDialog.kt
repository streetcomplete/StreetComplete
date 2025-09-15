package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Similar to ListPickerDialog, but tapping on one item immediately closes the dialog
 *  (no OK button, no cancel button)
 *
 *  This dialog doesn't have the caveat of the ListPickerDialog in that it takes as much width
 *  as possible */
@Composable
fun <T> SimpleListPickerDialog(
    onDismissRequest: () -> Unit,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    itemContent: (@Composable (T) -> Unit),
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    selectedItem: T? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties()
) {
    val selected by remember { mutableStateOf(selectedItem) }
    val state = rememberLazyListState()

    fun select(item: T) {
        onDismissRequest()
        onItemSelected(item)
    }

    LaunchedEffect(selectedItem) {
        val index = items.indexOf(selectedItem)
        if (index != -1) state.scrollToItem(index, -state.layoutInfo.viewportSize.height / 3)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        AlertDialogLayout(
            modifier = modifier,
            title = title,
            content = {
                Column {
                    if (state.canScrollBackward) Divider()
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        LocalTextStyle provides MaterialTheme.typography.body1
                    ) {
                        LazyColumn(state = state) {
                            items(items) { item ->
                                val isSelected = selected == item
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .selectable(isSelected) { select(item) }
                                        .padding(horizontal = 24.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        itemContent(item)
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { select(item) }
                                    )
                                }
                            }
                        }
                    }
                    if (state.canScrollForward) Divider()
                }
            },
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor
        )
    }
}

@Preview
@Composable
private fun PreviewSimpleListPickerDialog() {
    val items = remember { (0..<5).toList() }
    SimpleListPickerDialog(
        onDismissRequest = {},
        items = items,
        onItemSelected = {},
        itemContent = { Text("Item $it") },
        title = { Text("Select something") },
        selectedItem = 2,
    )
}
