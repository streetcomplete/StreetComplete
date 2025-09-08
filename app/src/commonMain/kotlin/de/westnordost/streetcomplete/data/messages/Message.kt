package de.westnordost.streetcomplete.data.messages

import de.westnordost.streetcomplete.data.changelog.Changelog
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.Link

sealed interface Message {
    /** User has unread messages in his OSM inbox */
    data class OsmUnreadMessages(val unreadMessages: Int) : Message

    /** User earned a new achievement */
    data class NewAchievement(
        val achievement: Achievement,
        val level: Int,
        val unlockedLinks: List<Link>
    ) : Message

    /** The app was updated to a new version */
    data class NewVersion(val changelog: Changelog) : Message

    /** The user is informed about being able to select which quests he wants to solve */
    data object QuestSelectionHint : Message
}
