package de.westnordost.streetcomplete.screens.user.achievements

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import kotlinx.coroutines.flow.StateFlow

abstract class AchievementsViewModel : ViewModel() {
    abstract val isSynchronizingStatistics: StateFlow<Boolean>
    abstract val achievements: StateFlow<List<Pair<Achievement, Int>>?>
}
