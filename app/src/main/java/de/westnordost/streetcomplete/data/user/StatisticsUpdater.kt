package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.user.achievements.AchievementGiver
import java.util.*
import javax.inject.Inject

class StatisticsUpdater @Inject constructor(
        private val questStatisticsDao: QuestStatisticsDao,
        private val achievementGiver: AchievementGiver,
        private val userStore: UserStore
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
}

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR) &&
    get(Calendar.YEAR) == other.get(Calendar.YEAR)