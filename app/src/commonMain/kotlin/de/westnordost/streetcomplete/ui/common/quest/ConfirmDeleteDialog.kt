package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user is asked whether he is sure that an element does not exist. He can
 *  choose between deleting the element, leaving a note, or going back ("I will check") */
@Composable
fun ConfirmDeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
    onLeaveNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttonRow = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.quest_generic_confirmation_no))
            }
            TextButton(onClick = { onDismissRequest(); onLeaveNote() }) {
                Text(stringResource(Res.string.leave_note))
            }
            TextButton(onClick = { onDismissRequest(); onConfirmDelete() }) {
                Text(stringResource(Res.string.osm_element_gone_confirmation))
            }
        },
        modifier = modifier,
        text = { Text(stringResource(Res.string.osm_element_gone_description)) },
    )
}
