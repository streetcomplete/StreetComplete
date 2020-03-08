package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.achievements.AchievementGiver
import javax.inject.Inject

class StatisticsManager @Inject constructor(
        private val questStatisticsDao: QuestStatisticsDao,
        private val achievementGiver: AchievementGiver
){
    fun addOne(questType: String) {
        questStatisticsDao.addOne(questType)
        achievementGiver.updateAchievements(questType)
        // TODO and days active!?
    }

    fun addOneNote() {
        questStatisticsDao.addOneNote()
        // TODO need to decide what to do with the note quests anyway...
        achievementGiver.updateAchievements("NOTE")
    }
}

// TODO path back for granted achievements...
