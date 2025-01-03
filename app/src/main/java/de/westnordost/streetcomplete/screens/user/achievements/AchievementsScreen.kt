package de.westnordost.streetcomplete.screens.user.achievements

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.ktx.plus

/** Shows the icons for all achieved achievements and opens a dialog to show the details on click. */
@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel) {
    val achievements by viewModel.achievements.collectAsState()
    val hasNoAchievements by remember { derivedStateOf { achievements?.isNotEmpty() != true } }

    var showAchievement by remember { mutableStateOf<Pair<Achievement, Int>?>(null) }

    achievements?.let {
        val insets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ).asPaddingValues()
        LazyAchievementsGrid(
            achievements = it,
            onClickAchievement = { achievement, level ->
                showAchievement = achievement to level
            },
            modifier = Modifier.fillMaxSize().consumeWindowInsets(insets),
            contentPadding = insets + PaddingValues(16.dp)
        )
    }
    if (hasNoAchievements) {
        val isSynchronizingStatistics by viewModel.isSynchronizingStatistics.collectAsState()
        CenteredLargeTitleHint(stringResource(
            if (isSynchronizingStatistics) R.string.stats_are_syncing
            else R.string.achievements_empty
        ))
    }

    showAchievement?.let { (achievement, level) ->
        AchievementDialog(
            achievement, level,
            onDismissRequest = { showAchievement = null },
            isNew = false
        )
    }
}
