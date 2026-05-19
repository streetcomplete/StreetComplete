package de.westnordost.streetcomplete.screens.measure

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import org.jetbrains.compose.resources.stringResource

/** Dialog that asks whether all AR quests should be disabled. */
@Composable
fun DisableArQuestsDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(stringResource(Res.string.quest_disable_title)) },
        text = {
            Text(text + "\n\n" + stringResource(Res.string.quest_disable_message_tape_measure))
        },
        confirmButtonText = stringResource(Res.string.quest_disable_action),
        modifier = modifier,
    )
}
