package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

/** Shows the icons for all achieved achievements and opens a dialog to show the details on click. */
@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel) {
    val isSynchronizingStatistics by viewModel.isSynchronizingStatistics.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    var showAchievement by remember { mutableStateOf<Pair<Achievement, Int>?>(null) }

    val allAchievements = achievements
    if (allAchievements != null) {
        if (allAchievements.isNotEmpty()) {
            LazyAchievementsGrid(
                achievements = allAchievements,
                onClickAchievement = { achievement, level ->
                    showAchievement = achievement to level
                },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            )
        } else {
            CenteredLargeTitleHint(stringResource(
                if (isSynchronizingStatistics) R.string.stats_are_syncing
                else R.string.achievements_empty
            ))
        }
    }

    // TODO Compose: revisit animate-from-icon when androidx.compose.animation 1.7 is stable
    // https://developer.android.com/develop/ui/compose/animation/shared-elements
    showAchievement?.let { (achievement, level) ->
        AchievementDialog(
            achievement, level,
            onDismissRequest = { showAchievement = null },
            isNew = false
        )
    }
}
