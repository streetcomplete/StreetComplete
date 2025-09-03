package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.user.achievements.AchievementCondition.DaysActive
import de.westnordost.streetcomplete.data.user.achievements.AchievementCondition.EditsOfTypeCount
import de.westnordost.streetcomplete.data.user.achievements.AchievementCondition.TotalEditCount
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.util.Listeners

/** Manages the data associated with achievements: Unlocked achievements, unlocked links and info
 *  about newly unlocked achievements (the user shall be notified about) */
class AchievementsController(
    private val statisticsSource: StatisticsSource,
    private val userAchievementsDao: UserAchievementsDao,
    private val userLinksDao: UserLinksDao,
    private val allEditTypes: AllEditTypes,
    private val allAchievements: List<Achievement>,
    allLinks: List<Link>
) : AchievementsSource {

    private val listeners = Listeners<AchievementsSource.Listener>()

    private val achievementsById = allAchievements.associateBy { it.id }
    private val linksById = allLinks.associateBy { it.id }

    private val statisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(type: String) {
            updateEditTypeAchievements(type)
        }

        override fun onSubtractedOne(type: String) {
            // anything once granted is not removed, so nothing to do here
        }

        override fun onUpdatedAll(isFirstUpdate: Boolean) {
            // When syncing statistics from server first time after login, any granted achievements
            // should be granted silently (without message) because no user action was involved.
            // This ensures that achievement links added later will also be earned by old users
            // (i.e. unlocked on next achievement update) rather than be unlocked silently right
            // away.
            if (isFirstUpdate) {
                updateAllAchievementsSilently()
            }
        }

        override fun onCleared() {
            clear()
        }

        override fun onUpdatedDaysActive() {
            updateDaysActiveAchievements()
        }
    }

    init {
        statisticsSource.addListener(statisticsListener)
    }

    /** Get the user's granted achievements and their level */
    override fun getAchievements(): List<Pair<Achievement, Int>> =
        userAchievementsDao.getAll().mapNotNull { (name, level) ->
            val achievement = achievementsById[name]
            if (achievement != null && level > 0) achievement to level else null
        }

    /** Get the user's unlocked links */
    override fun getLinks(): List<Link> =
        userLinksDao.getAll().mapNotNull { linksById[it] }

    override fun addListener(listener: AchievementsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: AchievementsSource.Listener) {
        listeners.remove(listener)
    }

    private fun clear() {
        userLinksDao.clear()
        userAchievementsDao.clear()
        listeners.forEach { it.onAllAchievementsUpdated() }
    }

    /** Look at and grant all achievements and their links */
    private fun updateAllAchievementsSilently() {
        val unlockedAchievements = allAchievements
            .map { it to getAchievedLevel(it) }
            .filter { (_, level) -> level > 0 }
        val unlockedLinks = mutableListOf<Link>()
        for ((achievement, achievedLevel) in unlockedAchievements) {
            for (level in 1..achievedLevel) {
                unlockedLinks.addAll(achievement.unlockedLinks[level].orEmpty())
            }
        }
        userAchievementsDao.putAll(unlockedAchievements.map { it.first.id to it.second })
        userLinksDao.addAll(unlockedLinks.map { it.id })

        listeners.forEach { it.onAllAchievementsUpdated() }
    }

    /** Look at and grant only the achievements that have anything to do with the given edit type */
    private fun updateEditTypeAchievements(type: String) {
        updateAchievements(allAchievements.filter {
            when (it.condition) {
                is EditsOfTypeCount -> isContributingToAchievement(type, it.id)
                is TotalEditCount -> true
                else -> false
            }
        })
    }

    /** Look at and grant only the achievements that have anything to do with days active */
    private fun updateDaysActiveAchievements() {
        updateAchievements(allAchievements.filter { it.condition is DaysActive })
    }

    private fun updateAchievements(achievements: List<Achievement>) {
        val currentAchievementLevels = userAchievementsDao.getAll()
        val currentLinks = getLinks().toSet()
        // look at all defined achievements
        for (achievement in achievements) {
            val currentLevel = currentAchievementLevels[achievement.id] ?: 0
            if (achievement.maxLevel != -1 && currentLevel >= achievement.maxLevel) continue

            val achievedLevel = getAchievedLevel(achievement)
            if (achievedLevel > currentLevel) {
                userAchievementsDao.put(achievement.id, achievedLevel)

                val unlockedLinks = mutableListOf<Link>()
                // add all links from all levels (some might have been added later in-between)
                for (level in 1..achievedLevel) {
                    unlockedLinks.addAll(achievement.unlockedLinks[level].orEmpty())
                }
                unlockedLinks -= currentLinks

                // one message only even if multiple levels were achieved
                if (!statisticsSource.isSynchronizing) {
                    listeners.forEach { it.onAchievementUnlocked(achievement, achievedLevel, unlockedLinks) }
                }

                userLinksDao.addAll(unlockedLinks.map { it.id })
            }
        }
    }

    private fun getAchievedLevel(achievement: Achievement): Int {
        val func = achievement.pointsNeededToAdvanceFunction
        val achievedPoints = getAchievedPoints(achievement)
        var level = 0
        var threshold = 0
        do {
            threshold += func(level)
            level++
            if (achievement.maxLevel != -1 && level > achievement.maxLevel) break
        } while (threshold <= achievedPoints)
        return level - 1
    }

    private fun getAchievedPoints(achievement: Achievement): Int =
        when (achievement.condition) {
            is EditsOfTypeCount -> statisticsSource.getEditCount(getEditTypesContributingToAchievement(achievement.id))
            is TotalEditCount -> statisticsSource.getEditCount()
            is DaysActive -> statisticsSource.daysActive
        }

    private fun isContributingToAchievement(editType: String, achievementId: String): Boolean =
        allEditTypes.getByName(editType)?.achievements?.anyHasId(achievementId) == true

    private fun getEditTypesContributingToAchievement(achievementId: String): List<String> =
        allEditTypes.filter { it.achievements.anyHasId(achievementId) }.map { it.name }
}

private fun List<EditTypeAchievement>.anyHasId(achievementId: String) = any { it.id == achievementId }
