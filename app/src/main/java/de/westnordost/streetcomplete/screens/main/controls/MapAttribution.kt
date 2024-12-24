package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.TextWithHalo
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog

/** Shows (hardcoded) map attribution and opens links on click */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapAttribution(modifier: Modifier = Modifier) {
    var shownLink by remember { mutableStateOf<String?>(null) }

    ProvideTextStyle(MaterialTheme.typography.caption) {
        FlowRow(modifier = modifier) {
            TextWithHalo(
                text = stringResource(R.string.map_attribution_osm),
                modifier = Modifier
                    .clickable { shownLink = "https://osm.org/copyright" }
                    .padding(4.dp),
                elevation = 4.dp
            )
            TextWithHalo(
                text = "© JawgMaps",
                modifier = Modifier
                    .clickable { shownLink = "https://jawg.io" }
                    .padding(4.dp),
                elevation = 4.dp
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
