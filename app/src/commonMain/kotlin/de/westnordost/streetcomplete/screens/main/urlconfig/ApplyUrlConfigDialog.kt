package de.westnordost.streetcomplete.screens.main.urlconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_presets_default_name
import de.westnordost.streetcomplete.resources.urlconfig_apply_message
import de.westnordost.streetcomplete.resources.urlconfig_apply_message_overwrite
import de.westnordost.streetcomplete.resources.urlconfig_apply_title
import de.westnordost.streetcomplete.resources.urlconfig_switch_hint
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import org.jetbrains.compose.resources.stringResource

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
        title = { Text(stringResource(Res.string.urlconfig_apply_title)) },
        text = {
            val name = presetName ?: stringResource(Res.string.quest_presets_default_name)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.urlconfig_apply_message, "\"$name\""))
                Text(stringResource(Res.string.urlconfig_switch_hint))
                if (presetNameAlreadyExists) {
                    Text(
                        text = stringResource(Res.string.urlconfig_apply_message_overwrite),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
