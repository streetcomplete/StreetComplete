package de.westnordost.streetcomplete.screens.user.profile

import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


class ProfileViewModelImpl(
    private val userDataSource: UserDataSource,
    private val userLoginStatusController: UserLoginStatusController,
    private val userUpdater: UserUpdater,
    private val statisticsSource: StatisticsSource,
    private val achievementsSource: AchievementsSource,
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val avatarsCacheDirectory: File,
    private val prefs: Preferences
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

    override var lastShownGlobalUserRank: Int?
        set(value) =
            if (value != null) prefs.putInt(Prefs.LAST_SHOWN_USER_GLOBAL_RANK, value)
            else prefs.remove(Prefs.LAST_SHOWN_USER_GLOBAL_RANK)
        get() = prefs.getIntOrNull(Prefs.LAST_SHOWN_USER_GLOBAL_RANK)

    override var lastShownGlobalUserRankCurrentWeek: Int?
        set(value) =
            if (value != null) prefs.putInt(Prefs.LAST_SHOWN_USER_GLOBAL_RANK_CURRENT_WEEK, value)
            else prefs.remove(Prefs.LAST_SHOWN_USER_GLOBAL_RANK_CURRENT_WEEK)
        get() = prefs.getIntOrNull(Prefs.LAST_SHOWN_USER_GLOBAL_RANK_CURRENT_WEEK)

    override var lastShownLocalUserRank: CountryStatistics?
        set(value) = prefs.putString(Prefs.LAST_SHOWN_USER_LOCAL_RANK, Json.encodeToString(value))
        get() = prefs.getStringOrNull(Prefs.LAST_SHOWN_USER_LOCAL_RANK)?.let { Json.decodeFromString(it) }

    override var lastShownLocalUserRankCurrentWeek: CountryStatistics?
        set(value) = prefs.putString(Prefs.LAST_SHOWN_USER_LOCAL_RANK_CURRENT_WEEK, Json.encodeToString(value))
        get() = prefs.getStringOrNull(Prefs.LAST_SHOWN_USER_LOCAL_RANK_CURRENT_WEEK)?.let { Json.decodeFromString(it) }

    override fun logOutUser() {
        viewModelScope.launch { userLoginStatusController.logOut() }
    }

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { unsyncedChangesCount.update { it + 1 } }
        override fun onDecreased() { unsyncedChangesCount.update { it -1 } }
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
        viewModelScope.launch {
            rank.value = statisticsSource.rank
            rankCurrentWeek.value = statisticsSource.currentWeekRank
            biggestSolvedCountCountryStatistics.value =
                withContext(Dispatchers.IO) { statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount() }
            biggestSolvedCountCurrentWeekCountryStatistics.value =
                withContext(Dispatchers.IO) { statisticsSource.getCurrentWeekCountryStatisticsOfCountryWithBiggestSolvedCount() }
        }
    }

    private fun updateEditCounts() {
        viewModelScope.launch {
            editCount.update { withContext(Dispatchers.IO) { statisticsSource.getEditCount() } }
            editCountCurrentWeek.update { withContext(Dispatchers.IO) { statisticsSource.getCurrentWeekEditCount() } }
        }
    }

    private fun updateAchievementLevels() {
        viewModelScope.launch {
            val achievements = withContext(Dispatchers.IO) { achievementsSource.getAchievements() }
            achievementLevels.value = achievements.sumOf { it.second }
        }
    }

    private fun updateDatesActive() {
        daysActive.value = statisticsSource.daysActive
        viewModelScope.launch {
            datesActive.value = DatesActiveInRange(
                withContext(Dispatchers.IO) { statisticsSource.getActiveDates() },
                statisticsSource.activeDatesRange
            )
        }
    }

    private fun updateUnsyncedChangesCount() {
        viewModelScope.launch {
            unsyncedChangesCount.update {
                withContext(Dispatchers.IO) {
                    unsyncedChangesCountSource.getCount()
                }
            }
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
