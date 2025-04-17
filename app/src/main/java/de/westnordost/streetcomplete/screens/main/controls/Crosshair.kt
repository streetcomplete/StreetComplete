package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.getOpenQuestFormMapPadding

/** A crosshair at the position at which a new POI should be created */
@Composable
fun Crosshair(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        Icon(
            painter = painterResource(R.drawable.crosshair),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(getOpenQuestFormMapPadding(maxWidth, maxHeight)),
            tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewCrosshair() {
    AppTheme {
        Surface {
            Crosshair()
        }
    }
}
