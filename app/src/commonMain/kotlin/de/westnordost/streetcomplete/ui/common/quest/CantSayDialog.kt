package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user is asked how to proceed when a quest can't be answered:
 * - wants to leave a note to explain why it can't be answered
 * - rather just hide the quest
 * - just disable the whole quest type */
@Composable
fun CantSayDialog(
    onDismissRequest: () -> Unit,
    onLeaveNote: () -> Unit,
    onHideQuest: () -> Unit,
    onDisableQuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onDismissRequest(); onLeaveNote() }) {
                Text(stringResource(Res.string.quest_leave_new_note_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest(); onDisableQuest() }) {
                Text(stringResource(Res.string.quest_leave_new_note_disable_quest))
            }
            TextButton(onClick = { onDismissRequest(); onHideQuest() }) {
                Text(stringResource(Res.string.quest_leave_new_note_no))
            }
        },
        title = { Text(stringResource(Res.string.quest_leave_new_note_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.quest_leave_new_note_description))
                Text(stringResource(Res.string.quest_leave_new_note_description_disable))
            }
        },
        modifier = modifier,
    )
}
