package de.westnordost.streetcomplete.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import org.jetbrains.compose.resources.stringResource

/** Shows a dialog that asks the user to login */
@Composable
fun RequestLoginDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.confirmation_authorize_now))
                Text(stringResource(Res.string.confirmation_authorize_now_note2))
            }
        },
        cancelButtonText = stringResource(Res.string.later)
    )
}
