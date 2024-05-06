package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.user.CenteredLargeTitleHint
import de.westnordost.streetcomplete.util.ktx.openUri

/** Shows the user's unlocked links */
@Composable
fun LinksScreen(viewModel: LinksViewModel) {
    val isSynchronizingStatistics by viewModel.isSynchronizingStatistics.collectAsState()
    val links by viewModel.links.collectAsState()

    val allLinks = links
    if (allLinks != null) {
        if (allLinks.isNotEmpty()) {
            val context = LocalContext.current
            LazyGroupedLinksColumn(
                allLinks = allLinks,
                onClickLink = { context.openUri(it) },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            )
        } else {
            CenteredLargeTitleHint(stringResource(
                if (isSynchronizingStatistics) R.string.stats_are_syncing
                else R.string.links_empty
            ))
        }
    }
}
