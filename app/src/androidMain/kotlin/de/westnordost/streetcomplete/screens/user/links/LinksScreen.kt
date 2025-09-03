package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.ktx.plus

/** Shows the user's unlocked links */
@Composable
fun LinksScreen(viewModel: LinksViewModel) {
    val links by viewModel.links.collectAsState()
    val hasLinks by remember { derivedStateOf { links?.isNotEmpty() == true } }

    Box {
        links?.let {
            val insets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ).asPaddingValues()
            LazyGroupedLinksColumn(
                allLinks = it,
                modifier = Modifier.fillMaxSize().consumeWindowInsets(insets),
                contentPadding = insets + PaddingValues(16.dp)
            )
        }
        if (!hasLinks) {
            val isSynchronizingStatistics by viewModel.isSynchronizingStatistics.collectAsState()
            CenteredLargeTitleHint(stringResource(
                if (isSynchronizingStatistics) R.string.stats_are_syncing
                else R.string.links_empty
            ))
        }
    }
}
