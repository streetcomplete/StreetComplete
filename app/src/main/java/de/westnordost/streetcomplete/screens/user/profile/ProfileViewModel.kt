package de.westnordost.streetcomplete.screens.user.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import java.io.File

abstract class ProfileViewModel : ViewModel() {
    abstract val userName: StateFlow<String?>
    abstract val userAvatarFile: StateFlow<File>

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

    abstract fun logOutUser()
}

@Immutable
data class DatesActiveInRange(val datesActive: List<LocalDate>, val range: Int)

class ProfileViewModelImpl(
    private val userDataSource: UserDataSource,
    private val userLoginStatusController: UserLoginStatusController,
    private val userUpdater: UserUpdater,
    private val statisticsSource: StatisticsSource,
    private val achievementsSource: AchievementsSource,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val avatarsCacheDirectory: File
) : ProfileViewModel() {

    override val userName = MutableStateFlow<String?>(null)
    override val userAvatarFile = MutableStateFlow(getUserAvatarFile())
    override val achievementLevels = MutableStateFlow(0)
    override val unsyncedChangesCount = MutableStateFlow(0)
    override val datesActive = MutableStateFlow(DatesActiveInRange(emptyList(), 0))
    override val daysActive = MutableStateFlow(0)
    override val editCount = MutableStateFlow(0)
    override val editCountCurrentWeek = MutableStateFlow(0)
    override val rank = MutableStateFlow(-1)
    override val rankCurrentWeek = MutableStateFlow(-1)
    override val biggestSolvedCountCountryStatistics = MutableStateFlow<CountryStatistics?>(null)
    override val biggestSolvedCountCurrentWeekCountryStatistics = MutableStateFlow<CountryStatistics?>(null)

    override fun logOutUser() {
        launch { userLoginStatusController.logOut() }
    }

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { unsyncedChangesCount.update { it + 1 } }
        override fun onDecreased() { unsyncedChangesCount.update { it - 1 } }
    }
    private val statisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(type: String) {
            editCount.update { it + 1 }
            editCountCurrentWeek.update { it + 1 }
        }
        override fun onSubtractedOne(type: String) {
            editCount.update { it - 1 }
            editCountCurrentWeek.update { it - 1 }
        }
        override fun onUpdatedAll() { updateStatistics() }
        override fun onCleared() { updateStatistics() }
        override fun onUpdatedDaysActive() { updateDatesActive() }
    }
    private val achievementsListener = object : AchievementsSource.Listener {
        override fun onAchievementUnlocked(achievement: Achievement, level: Int) { updateAchievementLevels() }
        override fun onAllAchievementsUpdated() { updateAchievementLevels() }
    }
    private val userListener = object : UserDataSource.Listener {
        override fun onUpdated() {
            userName.value = userDataSource.userName
            userAvatarFile.value = getUserAvatarFile()
        }
    }
    private val userAvatarListener = object : UserUpdater.Listener {
        override fun onUserAvatarUpdated() {
            userAvatarFile.value = getUserAvatarFile()
        }
    }

    init {
        userName.value = userDataSource.userName
        updateAchievementLevels()
        updateUnsyncedChangesCount()
        updateStatistics()

        userDataSource.addListener(userListener)
        userUpdater.addUserAvatarListener(userAvatarListener)
        statisticsSource.addListener(statisticsListener)
        unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)
        achievementsSource.addListener(achievementsListener)
    }

    private fun updateStatistics() {
        updateEditCounts()
        updateRanks()
        updateDatesActive()
    }

    private fun updateRanks() {
        launch(Dispatchers.IO) {
            rank.value = statisticsSource.rank
            rankCurrentWeek.value = statisticsSource.currentWeekRank
            biggestSolvedCountCountryStatistics.value =
                statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount()
            biggestSolvedCountCurrentWeekCountryStatistics.value =
                statisticsSource.getCurrentWeekCountryStatisticsOfCountryWithBiggestSolvedCount()
        }
    }

    private fun updateEditCounts() {
        launch(Dispatchers.IO) {
            editCount.update { statisticsSource.getEditCount() }
            editCountCurrentWeek.update { statisticsSource.getCurrentWeekEditCount() }
        }
    }

    private fun updateAchievementLevels() {
        launch(Dispatchers.IO) {
            achievementLevels.value = achievementsSource.getAchievements().sumOf { it.second }
        }
    }

    private fun updateDatesActive() {
        launch(Dispatchers.IO) {
            daysActive.value = statisticsSource.daysActive
            datesActive.value = DatesActiveInRange(
                statisticsSource.getActiveDates(),
                statisticsSource.activeDatesRange
            )
        }
    }

    private fun updateUnsyncedChangesCount() {
        launch(Dispatchers.IO) {
            unsyncedChangesCount.update { unsyncedChangesCountSource.getCount() }
        }
    }

    private fun getUserAvatarFile(): File =
        File(avatarsCacheDirectory, userDataSource.userId.toString())

    override fun onCleared() {
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
        statisticsSource.removeListener(statisticsListener)
        userDataSource.removeListener(userListener)
        userUpdater.removeUserAvatarListener(userAvatarListener)
        achievementsSource.removeListener(achievementsListener)
    }
}
