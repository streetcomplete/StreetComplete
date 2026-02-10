package de.westnordost.streetcomplete.data.messages

import de.westnordost.streetcomplete.data.changelog.Changelog
import de.westnordost.streetcomplete.data.messages.Message.NewAchievement
import de.westnordost.streetcomplete.data.messages.Message.NewVersion
import de.westnordost.streetcomplete.data.messages.Message.NewWeeklyOsm
import de.westnordost.streetcomplete.data.messages.Message.OsmUnreadMessages
import de.westnordost.streetcomplete.data.messages.Message.QuestSelectionHint
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.Link
import kotlinx.datetime.LocalDate
import kotlin.reflect.KClass

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

    /** New edition of weekly OSM */
    data class NewWeeklyOsm(val date: LocalDate) : Message

    companion object {
        fun classFromSimpleName(name: String): KClass<out Message>? = when (name) {
            OsmUnreadMessages::class.simpleName -> OsmUnreadMessages::class
            NewAchievement::class.simpleName -> NewAchievement::class
            NewVersion::class.simpleName -> NewVersion::class
            QuestSelectionHint::class.simpleName -> QuestSelectionHint::class
            NewWeeklyOsm::class.simpleName -> NewWeeklyOsm::class
            else -> null
        }
    }
}
