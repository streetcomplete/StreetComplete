package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

/** Shows (hardcoded) map attribution and opens links on click */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapAttribution(modifier: Modifier = Modifier) {
    var shownLink by remember { mutableStateOf<String?>(null) }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProvideTextStyle(MaterialTheme.typography.caption.copy(
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            shadow = Shadow(
                color = MaterialTheme.colors.surface,
                blurRadius = 6f
            )
        )) {
            Text(
                text = stringResource(R.string.map_attribution_osm),
                modifier = Modifier.clickable { shownLink ="https://www.openstreetmap.org/copyright" }
            )
            Text(
                text = "© JawgMaps",
                modifier = Modifier.clickable { shownLink = "https://www.jawg.io" }
            )
        }
    }

    shownLink?.let { url ->
        val uriHandler = LocalUriHandler.current
        ConfirmationDialog(
            onDismissRequest = { shownLink = null },
            onConfirmed = { uriHandler.openUri(url) },
            title = { Text(stringResource(R.string.open_url)) },
            text = { Text(url) }
        )
    }
}
