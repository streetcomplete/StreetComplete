package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.ui.common.ExpandableSearchField
import de.westnordost.streetcomplete.ui.common.MoreIcon
import de.westnordost.streetcomplete.ui.common.SearchIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

/** Top bar and search field for the quest selection screen */
@Composable fun QuestSelectionTopAppBar(
    currentPresetName: String,
    onClickBack: () -> Unit,
    onUnselectAll: () -> Unit,
    onReset: () -> Unit,
    search: TextFieldValue,
    onSearchChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSearch by remember { mutableStateOf(false) }

    fun setShowSearch(value: Boolean) {
        showSearch = value
        if (!value) onSearchChange(TextFieldValue())
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.primarySurface,
        elevation = AppBarDefaults.TopAppBarElevation,
    ) {
        Column {
            TopAppBar(
                title = { QuestSelectionTitle(currentPresetName) },
                navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
                actions = {
                    QuestSelectionTopBarActions(
                        onUnselectAll = onUnselectAll,
                        onReset = onReset,
                        onClickSearch = { setShowSearch(!showSearch) }
                    )
                },
                elevation = 0.dp
            )
            ExpandableSearchField(
                expanded = showSearch,
                onDismiss = { setShowSearch(false) },
                search = search,
                onSearchChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.surface
                )
            )
        }
    }
}

@Composable
private fun QuestSelectionTitle(currentPresetName: String) {
    Column {
        Text(
            text = stringResource(R.string.pref_title_quests2),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.pref_subtitle_quests_preset_name, currentPresetName),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.body1,
        )
    }
}

@Composable
private fun QuestSelectionTopBarActions(
    onUnselectAll: () -> Unit,
    onReset: () -> Unit,
    onClickSearch: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeselectAllDialog by remember { mutableStateOf(false) }
    var showActionsDropdown by remember { mutableStateOf(false) }

    IconButton(onClick = onClickSearch) { SearchIcon() }
    Box {
        IconButton(onClick = { showActionsDropdown = true }) { MoreIcon() }
        DropdownMenu(
            expanded = showActionsDropdown,
            onDismissRequest = { showActionsDropdown = false },
        ) {
            DropdownMenuItem(onClick = {
                showResetDialog = true
                showActionsDropdown = false
            }) {
                Text(stringResource(R.string.action_reset))
            }
            DropdownMenuItem(onClick = {
                showDeselectAllDialog = true
                showActionsDropdown = false
            }) {
                Text(stringResource(R.string.action_deselect_all))
            }
        }
    }

    if (showDeselectAllDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeselectAllDialog = false },
            onConfirmed = onUnselectAll,
            text = { Text(stringResource(R.string.pref_quests_deselect_all)) },
        )
    }

    if (showResetDialog) {
        ConfirmationDialog(
            onDismissRequest = { showResetDialog = false },
            onConfirmed = onReset,
            text = { Text(stringResource(R.string.pref_quests_reset)) },
        )
    }
}

@Preview
@Composable
private fun PreviewQuestSelectionTopBar() {
    var searchText by remember { mutableStateOf(TextFieldValue()) }
    QuestSelectionTopAppBar(
        currentPresetName = "Test",
        onClickBack = {},
        onUnselectAll = {},
        onReset = {},
        search = searchText,
        onSearchChange = { searchText = it },
    )
}
