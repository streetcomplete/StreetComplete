package de.westnordost.streetcomplete.screens.main

import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.stringResource

/** Dropdown menu shown when long-pressing on the map. */
@Composable
fun MapContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClickCreateNote: () -> Unit,
    onClickCreateTrack: () -> Unit,
    onClickOpenLocation: () -> Unit,
    isOpenLocationAvailable: Boolean,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
    ) {
        DropdownMenuItem(onClick = { onDismissRequest(); onClickCreateNote() }) {
            Text(stringResource(Res.string.map_btn_create_note))
        }
        DropdownMenuItem(onClick = { onDismissRequest(); onClickCreateTrack() }) {
            Text(stringResource(Res.string.map_btn_create_track))
        }
        if (isOpenLocationAvailable) {
            DropdownMenuItem(onClick = { onDismissRequest(); onClickOpenLocation() }) {
                Text(stringResource(Res.string.action_open_location))
            }
        }
    }
}
