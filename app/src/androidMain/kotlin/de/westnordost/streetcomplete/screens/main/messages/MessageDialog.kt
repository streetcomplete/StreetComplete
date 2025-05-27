package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.achievements.AchievementDialog

/** Dialog that shows a Message */
@Composable
fun MessageDialog(
    message: Message,
    allQuestIconIds: List<Int>,
    onDismissRequest: () -> Unit,
) {
    when (message) {
        is Message.NewAchievement -> {
            AchievementDialog(
                achievement = message.achievement,
                level = message.level,
                unlockedLinks = message.unlockedLinks,
                onDismissRequest = onDismissRequest
            )
        }
        is Message.NewVersion -> {
            WhatsNewDialog(
                changelog = message.changelog,
                onDismissRequest = onDismissRequest,
            )
        }
        is Message.QuestSelectionHint -> {
            val context = LocalContext.current
            QuestSelectionHintDialog(
                onDismissRequest = onDismissRequest,
                onClickOpenSettings = {
                    context.startActivity(SettingsActivity.createLaunchQuestSettingsIntent(context))
                },
                allQuestIconIds = allQuestIconIds
            )
        }
        is Message.OsmUnreadMessages -> {
            val uriHandler = LocalUriHandler.current
            UnreadMessagesDialog(
                unreadMessageCount = message.unreadMessages,
                onDismissRequest = onDismissRequest,
                onClickOpenMessages = {
                    uriHandler.openUri("https://www.openstreetmap.org/messages/inbox")
                }
            )
        }
    }
}
