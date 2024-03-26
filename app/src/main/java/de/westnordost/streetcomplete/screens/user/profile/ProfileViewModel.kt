package de.westnordost.streetcomplete.screens.user.profile

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

abstract class ProfileViewModel : ViewModel() {
    abstract val userName: StateFlow<String?>
    abstract val userAvatarFilePath: StateFlow<String?>

    abstract val achievementLevels: StateFlow<Int>

    abstract val unsyncedChangesCount: StateFlow<Int>

    abstract val datesActive: StateFlow<DatesActiveInRange>
    abstract val daysActive: StateFlow<Int>

    abstract val editCount: StateFlow<Int>
    abstract val editCountCurrentWeek: StateFlow<Int>

    abstract val rank: StateFlow<Int>
    abstract val rankCurrentWeek: StateFlow<Int>

    abstract val biggestSolvedCountCountryStatistics: StateFlow<CountryStatistics?>
    abstract val biggestSolvedCountCurrentWeekCountryStatistics: StateFlow<CountryStatistics?>

    abstract var lastShownGlobalUserRank: Int?
    abstract var lastShownGlobalUserRankCurrentWeek: Int?
    abstract var lastShownLocalUserRank: CountryStatistics?
    abstract var lastShownLocalUserRankCurrentWeek: CountryStatistics?

    abstract fun logOutUser()
}

data class DatesActiveInRange(val datesActive: List<LocalDate>, val range: Int)
