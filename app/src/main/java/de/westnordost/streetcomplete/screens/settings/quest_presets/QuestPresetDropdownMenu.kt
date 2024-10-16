package de.westnordost.streetcomplete.screens.settings.quest_presets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem

/** The dropdown menu that shows when tapping on the more button */
@Composable
fun QuestPresetDropdownMenu(
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
                    text = stringResource(R.string.quest_presets_rename),
                    painter = painterResource(R.drawable.ic_edit_24dp)
                )
            }
        }
        DropdownMenuItem(onClick = { onDismissRequest(); onDuplicate() }) {
            TextWithIcon(
                text = stringResource(R.string.quest_presets_duplicate),
                painter = painterResource(R.drawable.ic_content_copy_24dp)
            )
        }
        DropdownMenuItem(onClick = { onDismissRequest(); onShare() }) {
            TextWithIcon(
                text = stringResource(R.string.quest_presets_share),
                painter = painterResource(R.drawable.ic_share_24dp)
            )
        }
        if (!isDefaultPreset) {
            DropdownMenuItem(onClick = { onDismissRequest(); onDelete() }) {
                TextWithIcon(
                    text = stringResource(R.string.quest_presets_delete),
                    painter = painterResource(R.drawable.ic_delete_24dp)
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
