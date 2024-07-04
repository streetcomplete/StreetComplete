package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.ui.theme.AppTheme

/** List picker dialog with OK and cancel button that expands to its maximum possible size in both
 *  directions, scrollable.
 *  (See explanation in ScrollableAlertDialog why it expands to the maximum possible size)*/
@Composable
fun <T> ListPickerDialog(
    onDismissRequest: () -> Unit,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    selectedItem: T? = null,
    getItemName: (@Composable (T) -> String) = { it.toString() },
    width: Dp? = null,
    height: Dp? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties()
) {
    var selected by remember { mutableStateOf(selectedItem) }
    val state = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        val index = items.indexOf(selectedItem)
        if (index != -1) state.scrollToItem(index, -state.layoutInfo.viewportSize.height / 3)
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        content = {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalTextStyle provides MaterialTheme.typography.body1
            ) {
                if (state.canScrollBackward) Divider()
                LazyColumn(state = state) {
                    items(items) { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selected = item }
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = getItemName(item),
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f),
                            )
                            RadioButton(
                                selected = selected == item,
                                onClick = { selected = item }
                            )
                        }
                    }
                }
                if (state.canScrollForward) Divider()
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
            TextButton(
                onClick = {
                    onDismissRequest()
                    selected?.let { onItemSelected(it) }
                },
                enabled = selected != null,
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        width = width,
        height = height,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        properties = properties
    )
}

@Preview
@Composable
private fun PreviewListPickerDialog() {
    val items = remember { (0..<5).toList() }
    AppTheme {
        ListPickerDialog(
            onDismissRequest = {},
            items = items,
            onItemSelected = {},
            title = { Text("Select something") },
            selectedItem = 2,
            getItemName = { "Item $it" },
            width = 260.dp
        )
    }
}
