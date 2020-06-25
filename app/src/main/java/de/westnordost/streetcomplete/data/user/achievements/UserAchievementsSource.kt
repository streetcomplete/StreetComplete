package de.westnordost.streetcomplete.data.user.achievements

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Provides the user's granted achievements and their level */
@Singleton class UserAchievementsSource @Inject constructor(
    private val achievementsDao: UserAchievementsDao,
    @Named("Achievements") private val allAchievements: List<Achievement>
) {
    private val achievementsById = allAchievements.associateBy { it.id }

    fun getAchievements(): List<Pair<Achievement, Int>> {
        return achievementsDao.getAll().mapNotNull {
            val achievement = achievementsById[it.key]
            if (achievement != null) achievement to it.value else null
        }
    }
}