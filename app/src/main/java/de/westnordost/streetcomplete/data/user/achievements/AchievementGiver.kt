package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsDao
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.data.user.UserStore
import javax.inject.Inject
import javax.inject.Named

/** Grants achievements based on solved quests (or other things) and puts the links contained in
 * these in the link collection */
class AchievementGiver @Inject constructor(
        private val userAchievementsDao: UserAchievementsDao,
        private val newUserAchievementsDao: NewUserAchievementsDao,
        private val userLinksDao: UserLinksDao,
        private val questStatisticsDao: QuestStatisticsDao,
        @Named("Achievements") private val allAchievements: List<Achievement>,
        private val userStore: UserStore
) {

    /** Look at and grant all achievements */
    fun updateAllAchievements(silent: Boolean = false) {
        return updateAchievements(allAchievements, silent)
    }

    /** Look at and grant only the achievements that have anything to do with the given quest type */
    fun updateQuestTypeAchievements(questType: String) {
        return updateAchievements(allAchievements.filter {
            when (it.condition) {
                is SolvedQuestsOfTypes -> it.condition.questTypes.contains(questType)
                is TotalSolvedQuests -> true
                else -> false
            }
        })
    }

    /** Look at and grant only the achievements that have anything to do with days active */
    fun updateDaysActiveAchievements() {
        return updateAchievements(allAchievements.filter { it.condition is DaysActive })
    }

    private fun updateAchievements(achievements: List<Achievement>, silent: Boolean = false) {
        val currentAchievementLevels = userAchievementsDao.getAll()
        // look at all defined achievements
        for (achievement in achievements) {
            val currentLevel = currentAchievementLevels[achievement.id] ?: 0
            if (achievement.maxLevel != -1 && currentLevel >= achievement.maxLevel) continue

            val achievedLevel = getAchievedLevel(achievement)
            if (achievedLevel > currentLevel) {
                userAchievementsDao.put(achievement.id, achievedLevel)

                val unlockedLinkIds = mutableListOf<String>()
                for (level in (currentLevel + 1)..achievedLevel) {
                    achievement.unlockedLinks[level]?.map { it.id }?.let { unlockedLinkIds.addAll(it) }
                    if (!silent) newUserAchievementsDao.push(achievement.id to level)
                }
                if (unlockedLinkIds.isNotEmpty()) userLinksDao.addAll(unlockedLinkIds)
            }
        }
    }

    /** Goes through all granted achievements and gives the included links to the user if he doesn't
     *  have them yet. This method only needs to be called when new links have been added to already
     *  existing achievement levels from one StreetComplete version to another. So, this only needs
     *  to be done once after each app update */
    fun updateAchievementLinks() {
        val unlockedLinkIds = mutableListOf<String>()
        val currentAchievementLevels = userAchievementsDao.getAll()
        for (achievement in allAchievements) {
            val currentLevel = currentAchievementLevels[achievement.id] ?: 0
            for (level in 1..currentLevel) {
                achievement.unlockedLinks[level]?.map { it.id }?.let { unlockedLinkIds.addAll(it) }
            }
        }
        if (unlockedLinkIds.isNotEmpty()) userLinksDao.addAll(unlockedLinkIds)
    }

    private fun getAchievedLevel(achievement: Achievement): Int {
        val func = achievement.pointsNeededToAdvanceFunction
        var level = 0
        var threshold = 0
        do {
            threshold += func(level)
            level++
            if (achievement.maxLevel != -1 && level > achievement.maxLevel) break
        } while (isAchieved(threshold, achievement.condition))
        return level - 1
    }

    private fun isAchieved(threshold: Int, condition: AchievementCondition): Boolean {
        return threshold <= when (condition) {
            is SolvedQuestsOfTypes -> questStatisticsDao.getAmount(condition.questTypes)
            is TotalSolvedQuests -> questStatisticsDao.getTotalAmount()
            is DaysActive -> userStore.daysActive
        }
    }
}
