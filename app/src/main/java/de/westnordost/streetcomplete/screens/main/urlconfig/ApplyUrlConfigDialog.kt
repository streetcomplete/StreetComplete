package de.westnordost.streetcomplete.screens.main.urlconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

/** Dialog that asks user for confirmation whether he wants to apply a preset with the given name */
@Composable
fun ApplyUrlConfigDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    presetName: String?,
    presetNameAlreadyExists: Boolean,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(stringResource(R.string.urlconfig_apply_title)) },
        text = {
            val name = presetName ?: stringResource(R.string.quest_presets_default_name)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.urlconfig_apply_message, "\"$name\""))
                Text(stringResource(R.string.urlconfig_switch_hint))
                if (presetNameAlreadyExists) {
                    Text(
                        text = stringResource(R.string.urlconfig_apply_message_overwrite),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
