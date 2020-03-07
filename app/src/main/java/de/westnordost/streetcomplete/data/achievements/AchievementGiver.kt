package de.westnordost.streetcomplete.data.achievements

import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.data.user.UserStore
import javax.inject.Inject

/** Grants achievements  based on solved quests (or other things) and puts the links contained in
 * these in the link collection */
class AchievementGiver @Inject constructor(
    private val userAchievementsDao: UserAchievementsDao,
    private val userLinksDao: UserLinksDao,
    private val questStatisticsDao: QuestStatisticsDao,
    private val achievements: List<Achievement>,
    private val userStore: UserStore
) {

    fun updateAchievements(): List<Pair<Achievement, Int>> {
        val newAchievements = mutableListOf<Pair<Achievement, Int>>()
        val currentAchievementLevels = userAchievementsDao.getAll()
        // look at all defined achievements
        for (achievement in achievements) {
            val currentLevel = currentAchievementLevels[achievement.id] ?: 0
            val totalLevels = achievement.levels.size
            // and check for the levels that haven't been granted yet, if the condition now resolves
            for (levelIndex in currentLevel until totalLevels) {
                val achievementLevel = achievement.levels[levelIndex]
                if (isAchieved(achievementLevel, achievement.condition)) {
                    val level = levelIndex + 1
                    userAchievementsDao.put(achievement.id, level)
                    userLinksDao.addAll(achievementLevel.links.map { it.id })
                    newAchievements.add(achievement to level)
                }
            }
        }
        return newAchievements
    }

    /** Goes through all granted achievements and gives the included links to the user if he doesn't
     *  have them yet. This method only needs to be called when new links have been added to already
     *  existing achievement levels from one StreetComplete version to another. So, this only needs
     *  to be done once after each app update */
    fun updateAchievementLinks() {
        val allGrantedLinks = mutableListOf<Link>()
        val currentAchievementLevels = userAchievementsDao.getAll()
        for (achievement in achievements) {
            val currentLevel = currentAchievementLevels[achievement.id] ?: 0
            for (levelIndex in 0 until currentLevel) {
                val achievementLevel = achievement.levels[levelIndex]
                allGrantedLinks.addAll(achievementLevel.links)
            }
        }
        userLinksDao.addAll(allGrantedLinks.map { it.id })
    }

    private fun isAchieved(achievementLevel: AchievementLevel, condition: AchievementCondition): Boolean {
        return achievementLevel.threshold <= when (condition) {
            is SolvedQuestsOfTypes -> questStatisticsDao.getAmount(condition.questTypes)
            is TotalSolvedQuests -> questStatisticsDao.getTotalAmount()
            is DaysActive -> userStore.daysActive
        }
    }
}
