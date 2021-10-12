package de.westnordost.streetcomplete.data.user.achievements

interface UserAchievementsSource {
    /** Get the user's granted achievements and their level */
    fun getAchievements(): List<Pair<Achievement, Int>>
}
