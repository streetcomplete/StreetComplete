package de.westnordost.streetcomplete.screens.main.overlays

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem

/** Dropdown menu for selecting an overlay */
@Composable
fun OverlaySelectionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    overlays: List<Overlay>,
    onSelect: (Overlay?) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        DropdownMenuItem(onClick = { onDismissRequest(); onSelect(null) }) {
            Text(
                text = stringResource(R.string.overlay_none),
                modifier = Modifier.padding(start = 48.dp)
            )
        }
        for (overlay in overlays) {
            DropdownMenuItem(onClick = { onDismissRequest(); onSelect(overlay) }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(overlay.icon),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(stringResource(overlay.title))
                }
            }
        }
    }
}
