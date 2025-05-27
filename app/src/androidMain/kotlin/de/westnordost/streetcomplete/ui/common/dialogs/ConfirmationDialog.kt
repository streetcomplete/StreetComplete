package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties

/** Slight specialization of an alert dialog: AlertDialog with OK and Cancel button. Both buttons
 *  call [onDismissRequest] and the OK button additionally calls [onConfirmed]. */
@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButtonText: String = stringResource(android.R.string.ok),
    cancelButtonText: String = stringResource(android.R.string.cancel),
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirmed(); onDismissRequest() }) { Text(confirmButtonText) }
        },
        modifier = modifier,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(cancelButtonText) } },
        title = title,
        text = text,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        properties = properties,
    )
}
