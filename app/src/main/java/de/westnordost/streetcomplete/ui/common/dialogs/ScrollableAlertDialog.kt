package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.ui.ktx.conditional

// TODO Compose
//  AlertDialog does not support scrollable content (yet) https://issuetracker.google.com/issues/217151230
//  This widget and probably SimpleListPickerDialog can be removed/replaced by a normal AlertDialog
//  as soon as that ticket is solved (which really is a bug, considering that Material design
//  AlertDialogs SHOULD be able to have scrollable content because that is actually mentioned as an
//  example in the Material design guidelines).

/** AlertDialog that can have scrollable content without bugging out and separates the scrollable
 *  content with a divider at the top and bottom.
 *
 *  Caveat: It always covers the maximum possible space unless a size is specified */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrollableAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    buttons: (@Composable FlowRowScope.() -> Unit)? = null,
    width: Dp? = null,
    height: Dp? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties()
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        AlertDialogLayout(
            modifier = modifier
                .conditional(width != null) { width(width!!) }
                .conditional(height != null) { height(height!!) },
            title = title,
            content = content?.let { {
                Divider()
                Column(Modifier.weight(1f)) { content() }
                Divider()
            } },
            buttons = buttons,
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun PreviewScrollableAlertDialog() {
    val loremIpsum = remember { LoremIpsum(200).values.first() }
    val scrollState = rememberScrollState()
    ScrollableAlertDialog(
        onDismissRequest = {},
        title = { Text("Title") },
        content = {
            Text(
                text = loremIpsum,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState)
            )
        },
        buttons = {
            TextButton(onClick = {}) { Text("Cancel") }
            TextButton(onClick = {}) { Text("OK") }
        },
        width = 260.dp
    )
}
