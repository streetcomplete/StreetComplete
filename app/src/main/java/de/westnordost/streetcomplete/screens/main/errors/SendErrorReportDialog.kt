package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

@Composable
fun SendErrorReportDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    title: String
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(title) },
        text = { Text(stringResource(R.string.crash_message)) },
        confirmButtonText = stringResource(R.string.crash_compose_email),
        // should be more of a modal dialog
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}
