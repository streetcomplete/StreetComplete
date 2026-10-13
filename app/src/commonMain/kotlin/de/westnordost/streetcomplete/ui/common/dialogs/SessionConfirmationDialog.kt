package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Shows a confirmation dialog with an option to suppress future
 * confirmations for the remainder of the session.
 */
@Composable
fun SessionConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: (dontShowAgain: Boolean) -> Unit,
    title: StringResource,
    note: StringResource? = null,
    modifier: Modifier = Modifier,
) {
    var dontShowAgain by remember { mutableStateOf(false) }
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = { onConfirmed(dontShowAgain) },
        title = { Text(stringResource(title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                note?.let { Text(stringResource(it)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                    )
                    Text(
                        text = stringResource(Res.string.dialog_session_dont_show_again),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        },
        modifier = modifier,
        confirmButtonText = stringResource(Res.string.quest_generic_confirmation_yes)
    )
}
