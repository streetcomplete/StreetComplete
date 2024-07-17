package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.achievements

@Composable
fun LazyAchievementsGrid(
    achievements: List<Pair<Achievement, Int>>,
    onClickAchievement: (achievement: Achievement, level: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // TODO Compose: revisit animate-in of list items when androidx.compose.animation 1.7 is stable
    // probably Modifier.animateItem or Modifier.animateEnterExit
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 144.dp),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(achievements) { (achievement, level) ->
            Box {
                AchievementIcon(icon = achievement.icon, level = level)
                // clickable area as separate box because the ripple should be on top of all of it
                // while the icon should not be clipped within the achievement frame
                Box(
                    Modifier
                    .matchParentSize()
                    .clip(AchievementFrameShape)
                    .clickable { onClickAchievement(achievement, level) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLazyAchievementsGrid() {
    LazyAchievementsGrid(
        achievements = achievements.map { it to (1..20).random() },
        onClickAchievement = { achievement, level -> }
    )
}
