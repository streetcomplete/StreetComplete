package de.westnordost.streetcomplete.data.messages

import de.westnordost.streetcomplete.data.user.achievements.Achievement

sealed class Message

data class OsmUnreadMessagesMessage(val unreadMessages: Int) : Message()
data class NewAchievementMessage(val achievement: Achievement, val level: Int) : Message()
data class NewVersionMessage(val sinceVersion: String) : Message()
object QuestSelectionHintMessage : Message()
