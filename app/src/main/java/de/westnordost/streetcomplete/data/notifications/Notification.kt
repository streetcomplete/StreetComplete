package de.westnordost.streetcomplete.data.notifications

import de.westnordost.streetcomplete.data.user.achievements.Achievement

sealed class Notification

data class OsmUnreadMessagesNotification(val unreadMessages: Int) : Notification()
data class NewAchievementNotification(val achievement: Achievement, val level: Int): Notification()
data class NewVersionNotification(val sinceVersion: String): Notification()