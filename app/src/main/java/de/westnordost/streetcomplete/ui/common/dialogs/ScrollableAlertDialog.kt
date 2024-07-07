package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import de.westnordost.streetcomplete.ui.theme.AppTheme

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
    buttons: (@Composable () -> Unit)? = null,
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
        Surface(
            modifier = modifier
                .conditional(width != null) { width(width!!) }
                .conditional(height != null) { height(height!!) },
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor
        ) {
            Column(Modifier.padding(top = 24.dp)) {
                if (title != null) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        LocalTextStyle provides MaterialTheme.typography.subtitle1
                    ) {
                        Column(Modifier.padding(start = 24.dp, bottom = 16.dp, end = 24.dp)) {
                            title()
                        }
                    }
                }
                if (content != null) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.medium,
                        LocalTextStyle provides MaterialTheme.typography.body2
                    ) {
                        Divider()
                        Column(Modifier.weight(1f)) { content() }
                        Divider()
                    }
                }
                if (buttons != null) {
                    FlowRow(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) { buttons() }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewScrollableAlertDialog() {
    val loremIpsum = remember { LoremIpsum(200).values.first() }
    val scrollState = rememberScrollState()
    AppTheme {
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
}
