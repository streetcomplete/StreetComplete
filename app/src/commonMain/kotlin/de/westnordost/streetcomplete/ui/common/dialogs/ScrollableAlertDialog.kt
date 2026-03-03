package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview

// TODO Compose
//  AlertDialog does not support scrollable content (yet) https://issuetracker.google.com/issues/217151230
//  This widget and probably SimpleListPickerDialog can be removed/replaced by a normal AlertDialog
//  as soon as that ticket is solved (which really is a bug, considering that Material design
//  AlertDialogs SHOULD be able to have scrollable content because that is actually mentioned as an
//  example in the Material design guidelines).

/** AlertDialog that can have scrollable content without bugging out and separates the scrollable
 *  content with a divider at the top and bottom. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrollableAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    buttons: (@Composable FlowRowScope.() -> Unit)? = null,
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
            modifier = modifier,
            title = title,
            content = content?.let { {
                Column(Modifier.weight(1f, fill = false)) { content() }
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
    val loremIpsum = remember {
"""
Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.

Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer
""" }
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
        modifier = Modifier.width(260.dp)
    )
}
