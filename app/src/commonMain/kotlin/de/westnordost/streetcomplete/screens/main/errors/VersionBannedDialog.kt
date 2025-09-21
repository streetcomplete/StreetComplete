package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.version_banned_message
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import org.jetbrains.compose.resources.stringResource

/** Info dialog that informs user that his current app version has been banned from uploading
 *  data */
@Composable
fun VersionBannedDialog(
    onDismissRequest: () -> Unit,
    reason: String?
) {
    InfoDialog(
        onDismissRequest = onDismissRequest,
        text = {
            val message = StringBuilder(stringResource(Res.string.version_banned_message))
            if (reason != null) {
                message.append("\n\n\n")
                message.append(reason)
            }
            Text(message.toString())
        }
    )
}
