package de.westnordost.streetcomplete.screens.user.links

import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow

class LinksViewModelImpl(
    private val achievementsSource: AchievementsSource,
    private val statisticsSource: StatisticsSource,
) : LinksViewModel() {
    override val isSynchronizingStatistics = MutableStateFlow(statisticsSource.isSynchronizing)
    override val links = MutableStateFlow<List<Link>?>(null)

    init {
        launch(IO) {
            links.value = achievementsSource.getLinks()
        }
    }
}
