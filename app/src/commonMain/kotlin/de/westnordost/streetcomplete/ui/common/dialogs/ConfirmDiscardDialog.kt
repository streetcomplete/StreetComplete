package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

/** Dialog in which the user is asked whether he wants to discard the changes he made */
@Composable
fun ConfirmDiscardDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = {
            onDismissRequest()
            onConfirmed()
        },
        modifier = modifier,
        title = { Text(stringResource(Res.string.confirmation_discard_title)) },
        confirmButtonText = stringResource(Res.string.confirmation_discard_positive),
        cancelButtonText = stringResource(Res.string.short_no_answer_on_button)
    )
}
