package de.westnordost.streetcomplete.data.messages

import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.Link
import de.westnordost.streetcomplete.util.html.HtmlNode

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
    data class NewVersion(val changelog: Map<String, List<HtmlNode>>) : Message

    /** The user is informed about being able to select which quests he wants to solve */
    data object QuestSelectionHint : Message
}
