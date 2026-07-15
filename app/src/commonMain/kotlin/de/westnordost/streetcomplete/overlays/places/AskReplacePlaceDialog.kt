package de.westnordost.streetcomplete.overlays.places

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialog
import org.jetbrains.compose.resources.stringResource

/** Asks whether this place is a new place and thus should be replaced, or if it is still the same
 *  place (i.e. name and/or place type has been adjusted) */
@Composable
fun AskReplacePlaceDialog(
    onDismissRequest: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttonRow = {
            TextButton(onClick = { onDismissRequest(); onAnswer(false) }) {
                Text(stringResource(Res.string.confirmation_replace_shop_no))
            }
            TextButton(onClick = { onDismissRequest(); onAnswer(true) }) {
                Text(stringResource(Res.string.confirmation_replace_shop_yes))
            }
        },
        title = { Text(stringResource(Res.string.confirmation_replace_shop_title)) },
        text = { Text(stringResource(Res.string.confirmation_replace_shop_message)) },
        modifier = modifier
    )
}
