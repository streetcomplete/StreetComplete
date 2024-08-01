package de.westnordost.streetcomplete.screens.settings.quest_presets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.MoreIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog

@Composable
fun QuestPresetItem(
    item: QuestPresetSelection,
    onSelect: () -> Unit,
    onRename: (name: String) -> Unit,
    onDuplicate: (name: String) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showActionsDropdown by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    val name = item.name.ifEmpty { stringResource(R.string.quest_presets_default_name) }

    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(0.1f)
        )
        Box {
            IconButton(
                onClick = { showActionsDropdown = true },
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight(),
            ) {
                MoreIcon()
            }
            QuestPresetDropdownMenu(
                expanded = showActionsDropdown,
                onDismissRequest = { showActionsDropdown = false },
                onRename = { showRenameDialog = true },
                onDuplicate = { showDuplicateDialog = true },
                onShare = { showShareDialog = true; onShare() },
                onDelete = { showDeleteDialog = true },
                item.id == 0L,
            )
        }
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            RadioButton(selected = item.selected, onClick = onSelect)
        }
    }

    if (showRenameDialog) {
        TextInputDialog(
            onDismissRequest = { showRenameDialog = false },
            onConfirmed = { onRename(it) },
            title = { Text(stringResource(R.string.quest_presets_rename)) },
            text = name,
            textInputLabel = { Text(stringResource(R.string.quest_presets_preset_name)) }
        )
    }
    if (showDuplicateDialog) {
        TextInputDialog(
            onDismissRequest = { showDuplicateDialog = false },
            onConfirmed = { onDuplicate(it) },
            title = { Text(stringResource(R.string.quest_presets_duplicate)) },
            text = name,
            textInputLabel = { Text(stringResource(R.string.quest_presets_preset_name)) }
        )
    }
    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirmed = onDelete,
            text = { Text(stringResource(R.string.quest_presets_delete_message, name)) },
            confirmButtonText = stringResource(R.string.delete_confirmation),
        )
    }
    if (showShareDialog && item.url != null) {
        UrlConfigQRCodeDialog(
            onDismissRequest = { showShareDialog = false },
            url = item.url
        )
    }
}

@Preview
@Composable
private fun PreviewQuestPresetItem() {
    QuestPresetItem(
        item = QuestPresetSelection(1L, "A quest preset name", false),
        onSelect = {},
        onRename = {},
        onDuplicate = {},
        onShare = {},
        onDelete = {},
    )
}
