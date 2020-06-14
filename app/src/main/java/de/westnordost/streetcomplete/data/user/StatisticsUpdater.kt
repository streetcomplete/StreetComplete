package de.westnordost.streetcomplete.data.user

import android.util.Log
import de.westnordost.streetcomplete.data.user.achievements.AchievementGiver
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/** Manages the updating of statistics, locally and pulling a complete update from backend  */
class StatisticsUpdater @Inject constructor(
    private val questStatisticsDao: QuestStatisticsDao,
    private val achievementGiver: AchievementGiver,
    private val userStore: UserStore,
    private val statisticsDownloader: StatisticsDownloader,
    @Named("QuestAliases") private val questAliases: List<Pair<String, String>>
){
    fun addOne(questType: String) {
        updateDaysActive()

        questStatisticsDao.addOne(questType)
        achievementGiver.updateQuestTypeAchievements(questType)
    }

    fun subtractOne(questType: String) {
        updateDaysActive()
        questStatisticsDao.subtractOne(questType)
    }

    private fun updateDaysActive() {
        val now = Date()
        val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal1.time = userStore.lastStatisticsUpdate
        val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal2.time = now
        userStore.lastStatisticsUpdate = now
        if (!cal1.isSameDay(cal2)) {
            userStore.daysActive++
            achievementGiver.updateDaysActiveAchievements()
        }
    }

    fun updateFromBackend(userId: Long) {
        try {
            val statistics = statisticsDownloader.download(userId)
            val backendIsStillAnalyzing = statistics.isAnalyzing
            userStore.isSynchronizingStatistics = backendIsStillAnalyzing
            if (backendIsStillAnalyzing) {
                Log.i(TAG, "Backend is still analyzing changeset history")
                return
            }

            val backendDataIsUpToDate = statistics.lastUpdate.time / 1000 >= userStore.lastStatisticsUpdate.time / 1000
            if (!backendDataIsUpToDate) {
                Log.i(TAG, "Backend data is not up-to-date")
                return
            }

            val newStatistics = statistics.questTypes.toMutableMap()
            mergeQuestAliases(newStatistics)
            questStatisticsDao.replaceAll(newStatistics)
            userStore.daysActive = statistics.daysActive
            userStore.lastStatisticsUpdate = statistics.lastUpdate
            // when syncing statistics from server, any granted achievements should be
            // granted silently (without notification) because no user action was involved
            achievementGiver.updateAllAchievements(silent = true)
            achievementGiver.updateAchievementLinks()
        }  catch (e: Exception) {
            Log.w(TAG, "Unable to download statistics", e)
        }
    }

    private fun mergeQuestAliases(map: MutableMap<String, Int>)  {
        for ((oldName, newName) in questAliases) {
            val count = map[oldName]
            if (count != null) {
                map.remove(oldName)
                map[newName] = (map[newName] ?: 0) + count
            }
        }
    }

    companion object {
        private const val TAG = "StatisticsUpdater"
    }
}

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR) &&
    get(Calendar.YEAR) == other.get(Calendar.YEAR)