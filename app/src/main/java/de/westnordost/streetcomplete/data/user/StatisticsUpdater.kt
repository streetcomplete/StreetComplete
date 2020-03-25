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
        val now = Date()
        val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal1.time = userStore.lastDateActive
        val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal2.time = now
        if (!cal1.isSameDay(cal2)) {
            userStore.lastDateActive = now
            userStore.daysActive++
            achievementGiver.updateDaysActiveAchievements()
        }

        questStatisticsDao.addOne(questType)
        achievementGiver.updateQuestTypeAchievements(questType)
    }
}
// TODO need to decide what to do with the note quests anyway...
// TODO path back for granted achievements...

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR) &&
    get(Calendar.YEAR) == other.get(Calendar.YEAR)