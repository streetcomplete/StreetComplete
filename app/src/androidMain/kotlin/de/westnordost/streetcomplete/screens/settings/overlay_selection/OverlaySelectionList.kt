package de.westnordost.streetcomplete.screens.settings.overlay_selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.overlays.mtb_scale.MtbScaleOverlay
import de.westnordost.streetcomplete.overlays.street_parking.StreetParkingOverlay
import de.westnordost.streetcomplete.overlays.surface.SurfaceOverlay
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.enable_overlay_confirmation_title
import de.westnordost.streetcomplete.resources.overlay
import de.westnordost.streetcomplete.resources.quest_enabled
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.theme.titleMedium
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** List of overlays to individually enable or disable */
@Composable
fun OverlaySelectionList(
    items: List<OverlaySelection>,
    onSelect: (overlay: Overlay, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var showEnableOverlayDialog by remember { mutableStateOf<Overlay?>(null) }

    Column(modifier) {
        val layoutDirection = LocalLayoutDirection.current
        OverlaySelectionHeader(Modifier.padding(
            start = contentPadding.calculateStartPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(layoutDirection)
        ))
        LazyColumn(
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding()
            ),
        ) {
            itemsIndexed(items) { index, item ->
                Column {
                    if (index > 0) Divider()
                    OverlaySelectionRow(
                        item = item,
                        onToggleSelection = { isSelected ->
                            // when enabling overlay that is disabled by default, require confirmation
                            if (isSelected && item.overlay.defaultDisabledMessage != null) {
                                showEnableOverlayDialog = item.overlay
                            } else {
                                onSelect(item.overlay, isSelected)
                            }
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    showEnableOverlayDialog?.let { overlay ->
        ConfirmationDialog(
            onDismissRequest = { showEnableOverlayDialog = null },
            onConfirmed = { onSelect(overlay, true) },
            title = { Text(stringResource(Res.string.enable_overlay_confirmation_title)) },
            text = { Text(overlay.defaultDisabledMessage?.let { stringResource(it) }.orEmpty()) }
        )
    }
}

@Composable
private fun OverlaySelectionHeader(modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.overlay),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(Res.string.quest_enabled),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Divider()
    }
}

@Preview
@Composable
private fun PreviewOverlaySelectionList() {
    OverlaySelectionList(
        items = listOf(
            OverlaySelection(StreetParkingOverlay(), true),
            OverlaySelection(SurfaceOverlay(), false),
            OverlaySelection(MtbScaleOverlay(), false),
        ),
        onSelect = { _, _ -> },
    )
}
