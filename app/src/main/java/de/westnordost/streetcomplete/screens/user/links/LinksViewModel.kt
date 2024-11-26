package de.westnordost.streetcomplete.screens.user.links

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class LinksViewModel : ViewModel() {
    abstract val isSynchronizingStatistics: StateFlow<Boolean>
    abstract val links: StateFlow<List<Link>?>
}

@Stable
class LinksViewModelImpl(
    private val achievementsSource: AchievementsSource,
    private val statisticsSource: StatisticsSource,
) : LinksViewModel() {
    override val isSynchronizingStatistics = MutableStateFlow(statisticsSource.isSynchronizing)
    override val links = MutableStateFlow<List<Link>?>(null)

    init {
        launch(Dispatchers.IO) {
            links.value = achievementsSource.getLinks()
        }
    }
}
