package de.westnordost.streetcomplete.overlays.places

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AskReplacePlaceDialog(
    onDismissRequest: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
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
