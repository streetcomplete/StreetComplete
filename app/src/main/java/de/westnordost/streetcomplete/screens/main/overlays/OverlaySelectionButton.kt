package de.westnordost.streetcomplete.screens.main.overlays

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.ui.common.OverlaysIcon

/** Overlay selection button that shows the icon of the currently selected overlay */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverlaySelectionButton(
    onClick: () -> Unit,
    overlay: Overlay?,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(6.dp),
        shape = CircleShape,
        elevation = 4.dp
    ) {
        val icon = overlay?.icon
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = overlay.name,
                modifier = Modifier.padding(6.dp).size(36.dp)
            )
        } else {
            Box(Modifier.padding(12.dp)) { OverlaysIcon() }
        }
    }
}
