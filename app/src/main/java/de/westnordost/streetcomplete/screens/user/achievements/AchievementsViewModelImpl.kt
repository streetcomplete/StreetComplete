package de.westnordost.streetcomplete.screens.user.achievements

import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

class AchievementsViewModelImpl(
    private val achievementsSource: AchievementsSource,
    private val statisticsSource: StatisticsSource,
): AchievementsViewModel() {
    override val isSynchronizingStatistics = MutableStateFlow(statisticsSource.isSynchronizing)
    override val achievements = MutableStateFlow<List<Pair<Achievement, Int>>?>(null)


    init {
        launch(Dispatchers.IO) {
            achievements.value = achievementsSource.getAchievements()
        }
    }
}

