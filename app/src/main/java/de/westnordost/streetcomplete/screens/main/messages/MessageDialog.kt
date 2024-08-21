package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.messages.NewAchievementMessage
import de.westnordost.streetcomplete.data.messages.NewVersionMessage
import de.westnordost.streetcomplete.data.messages.OsmUnreadMessagesMessage
import de.westnordost.streetcomplete.data.messages.QuestSelectionHintMessage
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.achievements.AchievementDialog
import de.westnordost.streetcomplete.util.ktx.openUri

/** Dialog that shows a Message */
@Composable
fun MessageDialog(
    message: Message,
    allQuestIconIds: List<Int>,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    when (message) {
        is NewAchievementMessage -> {
            AchievementDialog(
                achievement = message.achievement,
                level = message.level,
                onDismissRequest = onDismissRequest
            )
        }
        is NewVersionMessage -> {
            WhatsNewDialog(
                changelog = message.changelog,
                onDismissRequest = onDismissRequest,
                onClickLink = { context.openUri(it) }
            )
        }
        is QuestSelectionHintMessage -> {
            QuestSelectionHintDialog(
                onDismissRequest = onDismissRequest,
                onClickOpenSettings = {
                    context.startActivity(SettingsActivity.createLaunchQuestSettingsIntent(context))
                },
                allQuestIconIds = allQuestIconIds
            )
        }
        is OsmUnreadMessagesMessage -> {
            UnreadMessagesDialog(
                unreadMessageCount = message.unreadMessages,
                onDismissRequest = onDismissRequest,
                onClickOpenMessages = { context.openUri("https://www.openstreetmap.org/messages/inbox") }
            )
        }
    }
}
