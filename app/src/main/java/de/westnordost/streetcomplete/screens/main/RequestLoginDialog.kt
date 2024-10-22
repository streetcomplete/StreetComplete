package de.westnordost.streetcomplete.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

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
                Text(stringResource(R.string.confirmation_authorize_now))
                Text(stringResource(R.string.confirmation_authorize_now_note2))
            }
        },
        cancelButtonText = stringResource(R.string.later)
    )
}
