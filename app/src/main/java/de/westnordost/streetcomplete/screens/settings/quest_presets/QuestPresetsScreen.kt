package de.westnordost.streetcomplete.screens.settings.quest_presets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.theme.titleMedium

/** Shows a screen in which the user can select which preset of quest selections he wants to use. */
@Composable fun QuestPresetsScreen(
    viewModel: QuestPresetsViewModel,
    onClickBack: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.action_manage_presets)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Box(Modifier.fillMaxHeight()) {
            QuestPresetsList(viewModel)
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_24dp),
                    contentDescription = stringResource(R.string.quest_presets_preset_add)
                )
            }
        }
    }

    if (showAddDialog) {
        TextInputDialog(
            onDismissRequest = { showAddDialog = false },
            onConfirmed = { viewModel.add(it) },
            title = { Text(stringResource(R.string.quest_presets_preset_add)) },
            textInputLabel = { Text(stringResource(R.string.quest_presets_preset_name)) }
        )
    }
}

@Composable
private fun QuestPresetsList(viewModel: QuestPresetsViewModel) {
    val presets by viewModel.presets.collectAsState()

    Column {
        QuestPresetsHeader()
        LazyColumn {
            itemsIndexed(presets, key = { _, it -> it.id }) { index, item ->
                Column {
                    if (index > 0) Divider()
                    QuestPresetItem(
                        item = item,
                        onSelect = { viewModel.select(item.id) },
                        onRename = { viewModel.rename(item.id, it) },
                        onDuplicate = { viewModel.duplicate(item.id, it) },
                        onShare = { viewModel.queryUrlConfig(item.id) },
                        onDelete = { viewModel.delete(item.id) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestPresetsHeader() {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.quest_presets_preset_name),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.quest_presets_selected),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Divider()
    }
}
