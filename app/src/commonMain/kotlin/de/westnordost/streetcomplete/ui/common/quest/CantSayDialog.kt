package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user is asked whether he wants to leave a note to explain why it can't be
 *  answered, or whether he'd rather just hide the quest instead */
@Composable
fun CantSayDialog(
    onDismissRequest: () -> Unit,
    onLeaveNote: () -> Unit,
    onHideQuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttonRow = {
            TextButton(onClick = { onDismissRequest(); onHideQuest() }) {
                Text(stringResource(Res.string.quest_leave_new_note_no))
            }
            TextButton(onClick = { onDismissRequest(); onLeaveNote() }) {
                Text(stringResource(Res.string.quest_leave_new_note_yes))
            }
        },
        title = { Text(stringResource(Res.string.quest_leave_new_note_title)) },
        text = { Text(stringResource(Res.string.quest_leave_new_note_description)) },
        modifier = modifier,
    )
}
