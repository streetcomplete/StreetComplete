package de.westnordost.streetcomplete.screens.settings.presets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_content_copy_24
import de.westnordost.streetcomplete.resources.ic_delete_24
import de.westnordost.streetcomplete.resources.ic_edit_24
import de.westnordost.streetcomplete.resources.ic_share_24
import de.westnordost.streetcomplete.resources.quest_presets_delete
import de.westnordost.streetcomplete.resources.quest_presets_duplicate
import de.westnordost.streetcomplete.resources.quest_presets_rename
import de.westnordost.streetcomplete.resources.quest_presets_share
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The dropdown menu that shows when tapping on the more button */
@Composable
fun EditTypePresetDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    isDefaultPreset: Boolean,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        if (!isDefaultPreset) {
            DropdownMenuItem(onClick = { onDismissRequest(); onRename() }) {
                TextWithIcon(
                    text = stringResource(Res.string.quest_presets_rename),
                    painter = painterResource(Res.drawable.ic_edit_24)
                )
            }
        }
        DropdownMenuItem(onClick = { onDismissRequest(); onDuplicate() }) {
            TextWithIcon(
                text = stringResource(Res.string.quest_presets_duplicate),
                painter = painterResource(Res.drawable.ic_content_copy_24)
            )
        }
        DropdownMenuItem(onClick = { onDismissRequest(); onShare() }) {
            TextWithIcon(
                text = stringResource(Res.string.quest_presets_share),
                painter = painterResource(Res.drawable.ic_share_24)
            )
        }
        if (!isDefaultPreset) {
            DropdownMenuItem(onClick = { onDismissRequest(); onDelete() }) {
                TextWithIcon(
                    text = stringResource(Res.string.quest_presets_delete),
                    painter = painterResource(Res.drawable.ic_delete_24)
                )
            }
        }
    }
}

@Composable
private fun TextWithIcon(text: String, painter: Painter, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter, text)
        Text(text)
    }
}
