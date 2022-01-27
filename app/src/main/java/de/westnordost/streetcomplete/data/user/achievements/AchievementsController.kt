package de.westnordost.streetcomplete.data.user.achievements

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Manages the data associated with achievements: Unlocked achievements, unlocked links and info
 *  about newly unlocked achievements (the user shall be notified about) */
@Singleton class AchievementsController @Inject constructor(
    private val statisticsSource: StatisticsSource,
    private val userAchievementsDao: UserAchievementsDao,
    private val userLinksDao: UserLinksDao,
    private val questTypeRegistry: QuestTypeRegistry,
    @Named("Achievements") private val allAchievements: List<Achievement>,
    @Named("Links") allLinks: List<Link>
): AchievementsSource {

    private val listeners: MutableList<AchievementsSource.Listener> = CopyOnWriteArrayList()

    private val achievementsById = allAchievements.associateBy { it.id }
    private val linksById = allLinks.associateBy { it.id }

    private val statisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(questType: QuestType<*>) {
            updateQuestTypeAchievements(questType)
        }

        override fun onSubtractedOne(questType: QuestType<*>) {
            // anything once granted is not removed, so nothing to do here
        }

        override fun onUpdatedAll() {
            // when syncing statistics from server, any granted achievements should be
            // granted silently (without notification) because no user action was involved
            updateAllAchievementsSilently()
            updateAchievementLinks()
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
        userAchievementsDao.getAll().mapNotNull {
            val achievement = achievementsById[it.key]
            if (achievement != null) achievement to it.value else null
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

    /** Look at and grant all achievements */
    private fun updateAllAchievementsSilently() {
        updateAchievements(allAchievements, silent = true)
        listeners.forEach { it.onAllAchievementsUpdated() }
    }

    /** Look at and grant only the achievements that have anything to do with the given quest type */
    private fun updateQuestTypeAchievements(questType: QuestType<*>) {
        updateAchievements(allAchievements.filter {
            when (it.condition) {
                is SolvedQuestsOfTypes -> questType.questTypeAchievements.anyHasId(it.id)
                is TotalSolvedQuests -> true
                else -> false
            }
        })
    }

    /** Look at and grant only the achievements that have anything to do with days active */
    private fun updateDaysActiveAchievements() {
        updateAchievements(allAchievements.filter { it.condition is DaysActive })
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

                    // one notification for each achievement level
                    if (!silent && !statisticsSource.isSynchronizing) {
                        listeners.forEach { it.onAchievementUnlocked(achievement, level) }
                    }
                }
                userLinksDao.addAll(unlockedLinkIds)
            }
        }
    }

    /** Goes through all granted achievements and gives the included links to the user if he doesn't
     *  have them yet. This method only needs to be called when new links have been added to already
     *  existing achievement levels from one StreetComplete version to another. So, this only needs
     *  to be done once after each app update */
    private fun updateAchievementLinks() {
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

    private fun getAchievedPoints(achievement: Achievement): Int {
        return when (achievement.condition) {
            is SolvedQuestsOfTypes -> statisticsSource.getSolvedCount(getAchievementQuestTypes(achievement.id))
            is TotalSolvedQuests -> statisticsSource.getSolvedCount()
            is DaysActive -> statisticsSource.daysActive
        }
    }

    private fun getAchievementQuestTypes(achievementId: String): List<QuestType<*>> =
        questTypeRegistry.filter { it.questTypeAchievements.anyHasId(achievementId) }
}

private fun List<QuestTypeAchievement>.anyHasId(achievementId: String) = any { it.id == achievementId }
