package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R

@Composable
fun QuestSelectionHintDialog(
    onDismissRequest: () -> Unit,
    onClickOpenSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onDismissRequest(); onClickOpenSettings() }) {
                Text(stringResource(R.string.quest_streetName_cantType_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.quest_selection_hint_title)) },
        text = { Text(stringResource(R.string.quest_selection_hint_message)) }
    )
}
