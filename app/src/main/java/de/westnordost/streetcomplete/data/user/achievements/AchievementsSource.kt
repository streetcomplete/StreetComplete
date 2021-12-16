package de.westnordost.streetcomplete.data.user.achievements

interface AchievementsSource {
    interface Listener {
        /** Called when a single achievement level has been unlocked */
        fun onAchievementUnlocked(achievement: Achievement, level: Int)
        /** Called when all achievements have been updated (after sync with server) */
        fun onAllAchievementsUpdated()
    }

    /** Get the user's granted achievements and their level */
    fun getAchievements(): List<Pair<Achievement, Int>>

    /** Get the user's unlocked links */
    fun getLinks(): List<Link>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
