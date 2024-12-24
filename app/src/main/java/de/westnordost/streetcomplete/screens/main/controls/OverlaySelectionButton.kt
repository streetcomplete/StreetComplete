package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.ui.common.OverlaysIcon

/** Overlay selection button that shows the icon of the currently selected overlay */
@Composable
fun OverlaySelectionButton(
    onClick: () -> Unit,
    overlay: Overlay?,
    modifier: Modifier = Modifier
) {
    val icon = overlay?.icon
    MapButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = if (icon != null) 6.dp else 12.dp
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = overlay.name,
                modifier = Modifier.size(36.dp)
            )
        } else {
            OverlaysIcon()
        }
    }
}
