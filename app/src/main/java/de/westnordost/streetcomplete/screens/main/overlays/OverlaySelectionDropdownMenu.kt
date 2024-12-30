package de.westnordost.streetcomplete.screens.main.overlays

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.custom.getCustomOverlayIndices
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import de.westnordost.streetcomplete.util.showOverlayCustomizer
import org.koin.compose.koinInject

/** Dropdown menu for selecting an overlay */
@Composable
fun OverlaySelectionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    getOverlays: (Context) -> List<Overlay>,
    onSelect: (Overlay?) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val questTypeRegistry: QuestTypeRegistry = koinInject()
    val prefs: Preferences = koinInject()

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
        for (overlay in getOverlays(LocalContext.current)) {
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
                    Text(
                        text = if (overlay.title != 0) stringResource(overlay.title) else overlay.changesetComment,
                        modifier = Modifier.weight(1f)
                    )
                    if (overlay.title == 0) {
                        Image(
                            painter = painterResource(R.drawable.ic_settings_48dp),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    onDismissRequest()
                                    showOverlayCustomizer(overlay.wikiLink!!.toInt(), ctx, prefs, questTypeRegistry,
                                        { onSelect(overlay) },
                                        { if (it) onSelect(null) }
                                    )
                                }
                        )
                    }
                }
            }
        }
        if (prefs.expertMode) {
            DropdownMenuItem(onClick = {
                onDismissRequest()
                showOverlayCustomizer((getCustomOverlayIndices(prefs).maxOrNull() ?: 0) + 1, ctx, prefs, questTypeRegistry,
                    { prefs.selectedOverlayName = CustomOverlay::class.simpleName }, // not great, as it relies on onSelected not changing
                    { onSelect(null) }
                )
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_add_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = stringResource(R.string.custom_overlay_add_button),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
