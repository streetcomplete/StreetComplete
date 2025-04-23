package de.westnordost.streetcomplete.data.user.achievements

interface AchievementsSource {
    interface Listener {
        /** Called when a new achievement level has been reached. When the user unlocked several
         *  levels at once, this is only called once with the highest achieved level, the
         *  [unlockedLinks] contain all links not unlocked yet */
        fun onAchievementUnlocked(achievement: Achievement, level: Int, unlockedLinks: List<Link>)
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
