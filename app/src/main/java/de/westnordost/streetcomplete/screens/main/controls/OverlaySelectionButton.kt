package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.custom.CustomOverlay
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.ui.common.OverlaysIcon
import org.koin.compose.koinInject

/** Overlay selection button that shows the icon of the currently selected overlay */
@Composable
fun OverlaySelectionButton(
    onClick: () -> Unit,
    overlay: Overlay?,
    modifier: Modifier = Modifier
) {
    val prefs: Preferences = koinInject()
    val icon = if (overlay is CustomOverlay){
        val index = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
        LocalContext.current.resources.getIdentifier(
            prefs.getString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_ICON, index), "ic_custom_overlay"),
            "drawable", LocalContext.current.packageName
        )
    } else overlay?.icon
    MapButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = if (icon != null) 6.dp else 12.dp
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = overlay!!.name,
                modifier = Modifier.size(36.dp)
            )
        } else {
            OverlaysIcon()
        }
    }
}
