package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.material.Text
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
    measureResult: ArMeasureResult,
    modifier: Modifier = Modifier,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(stringResource(Res.string.quest_disable_title)) },
        text = {
            val text = when (measureResult) {
                ArMeasureResult.Error -> Res.string.quest_disable_message_not_working
                ArMeasureResult.NotInstalled -> Res.string.quest_disable_message_not_installed
                is ArMeasureResult.Success -> null
            }
            text?.let {
                Text(
                    stringResource(text) +
                    "\n\n" +
                    stringResource(Res.string.quest_disable_message_tape_measure)
                )
            }
        },
        confirmButtonText = stringResource(Res.string.quest_disable_action),
        modifier = modifier,
    )
}
