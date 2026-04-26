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
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

/** Slight specialization of an alert dialog, used in quests. Both buttons
 *  call [onDismissRequest] and the OK button additionally calls [onConfirmed]. */
@Composable
fun QuestConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    titleText: String? = stringResource(Res.string.quest_generic_confirmation_title),
    text: @Composable (() -> Unit)? = null,
    confirmButtonText: String = stringResource(Res.string.quest_generic_confirmation_yes),
    cancelButtonText: String = stringResource(Res.string.quest_generic_confirmation_no),
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
        title = titleText?.let { { Text(it) } },
        text = text,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        properties = properties,
    )
}
