package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.AppTheme

@Composable
fun <T> ListPickerDialog(
    onDismissRequest: () -> Unit,
    height: Dp,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    selectedItem: T? = null,
    getItemName: (@Composable (T) -> String) = { it.toString() },
) {
    var selected by remember { mutableStateOf(selectedItem) }

    // TODO scroll to currently selected value

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        height = height,
        modifier = modifier,
        title = title,
        content = {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalTextStyle provides MaterialTheme.typography.body1
            ) {
                LazyColumn {
                    items(items) { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .clickable { selected = item }
                                .padding(horizontal = 16.dp)
                        ) {
                            RadioButton(
                                selected = selected == item,
                                onClick = { selected = item }
                            )
                            Text(
                                text = getItemName(item),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
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
        }
    )
}

@Preview
@Composable
private fun PreviewListPickerDialog() {
    val items = remember { (0..<5).toList() }
    AppTheme {
        ListPickerDialog(
            onDismissRequest = {},
            height = 200.dp,
            items = items,
            onItemSelected = {},
            title = { Text("Select something") },
            selectedItem = 20,
            getItemName = { "Item $it" },
        )
    }
}
