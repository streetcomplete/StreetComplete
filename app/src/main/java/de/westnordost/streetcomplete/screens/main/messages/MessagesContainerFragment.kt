package de.westnordost.streetcomplete.screens.main.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.messages.NewAchievementMessage
import de.westnordost.streetcomplete.data.messages.NewVersionMessage
import de.westnordost.streetcomplete.data.messages.OsmUnreadMessagesMessage
import de.westnordost.streetcomplete.data.messages.QuestSelectionHintMessage
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.achievements.AchievementDialog
import de.westnordost.streetcomplete.ui.util.composableContent
import kotlinx.coroutines.flow.MutableStateFlow

/** A fragment that contains any fragments that would show messages.
 *  Usually, messages are shown as dialogs, however there is currently one exception which
 *  makes this necessary as a fragment */
class MessagesContainerFragment : Fragment() {

    private val shownMessage = MutableStateFlow<Message?>(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = composableContent {
        val message by shownMessage.collectAsState()

        when (val msg = message) {
            is NewAchievementMessage -> {
                AchievementDialog(
                    msg.achievement,
                    msg.level,
                    onDismissRequest = { shownMessage.value = null }
                )
            }
            is NewVersionMessage -> {
                WhatsNewDialog(
                    changelog = msg.changelog,
                    onDismissRequest = { shownMessage.value = null },
                )
            }
            else -> {}
        }
    }

    fun showMessage(message: Message) {
        val ctx = context ?: return
        shownMessage.value = message
        when (message) {
            is OsmUnreadMessagesMessage -> {
                OsmUnreadMessagesFragment
                    .create(message.unreadMessages)
                    .show(childFragmentManager, null)
            }
            is QuestSelectionHintMessage -> {
                AlertDialog.Builder(ctx)
                    .setTitle(R.string.quest_selection_hint_title)
                    .setMessage(R.string.quest_selection_hint_message)
                    .setPositiveButton(R.string.quest_streetName_cantType_open_settings) { _, _ ->
                        startActivity(SettingsActivity.createLaunchQuestSettingsIntent(ctx))
                    }
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
            }
            else -> {}
        }
    }
}
