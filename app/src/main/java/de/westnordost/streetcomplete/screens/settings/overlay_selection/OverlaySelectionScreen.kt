package de.westnordost.streetcomplete.screens.settings.overlay_selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.DropdownMenu
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.ui.common.MoreIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

/** Shows a screen in which the user can enable and disable overlays */
@Composable
fun OverlaySelectionScreen(
    viewModel: OverlaySelectionViewModel,
    onClickBack: () -> Unit,
) {
    val currentPresetName by viewModel.selectedEditTypePresetName.collectAsState()

    val overlays by viewModel.overlays.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { OverlaySelectionTitle(currentPresetName ?: stringResource(R.string.quest_presets_default_name)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
            actions = {
                OverlaySelectionTopBarActions(
                    onReset = { viewModel.resetAll() },
                )
            }
        )
        val insets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ).asPaddingValues()

        OverlaySelectionList(
            items = overlays,
            onSelect = { overlay, selected ->
                viewModel.select(overlay, selected)
            },
            modifier = Modifier.consumeWindowInsets(insets),
            contentPadding = insets,
        )
    }
}

@Composable
private fun OverlaySelectionTitle(currentPresetName: String) {
    Column {
        Text(
            text = stringResource(R.string.pref_title_overlays),
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
private fun OverlaySelectionTopBarActions(
    onReset: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showActionsDropdown by remember { mutableStateOf(false) }

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
        }
    }

    if (showResetDialog) {
        ConfirmationDialog(
            onDismissRequest = { showResetDialog = false },
            onConfirmed = onReset,
            text = { Text(stringResource(R.string.pref_overlays_reset)) },
        )
    }
}
