package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
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
import org.jetbrains.compose.resources.stringResource

/** Asks user if he was really on-site */
@Composable
fun SurveyConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    onToggleDontShowAgain: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dontShowAgain by remember { mutableStateOf(false) }
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(stringResource(Res.string.quest_source_dialog_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(Res.string.quest_source_dialog_note))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = {
                            dontShowAgain = it
                            onToggleDontShowAgain(it)
                        },
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
