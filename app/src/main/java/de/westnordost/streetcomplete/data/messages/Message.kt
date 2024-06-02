package de.westnordost.streetcomplete.data.messages

import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.util.html.HtmlNode

sealed interface Message

data class OsmUnreadMessagesMessage(val unreadMessages: Int) : Message
data class NewAchievementMessage(val achievement: Achievement, val level: Int) : Message
data class NewVersionMessage(val changelog: Map<String, List<HtmlNode>>) : Message
data object QuestSelectionHintMessage : Message
