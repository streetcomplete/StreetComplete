package de.westnordost.streetcomplete.notifications

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.HandlesOnBackPressed
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.about.WhatsNewDialog
import de.westnordost.streetcomplete.data.notifications.*
import de.westnordost.streetcomplete.settings.SettingsActivity
import de.westnordost.streetcomplete.user.AchievementInfoFragment

/** A fragment that contains any fragments that would show notifications.
 *  Usually, notifications are shown as dialogs, however there is currently one exception which
 *  makes this necessary as a fragment */
class NotificationsContainerFragment : Fragment(R.layout.fragment_notifications_container),
    HandlesOnBackPressed {

    fun showNotification(notification: Notification) {
        val ctx = context ?: return
        when (notification) {
            is OsmUnreadMessagesNotification -> {
                OsmUnreadMessagesFragment
                    .create(notification.unreadMessages)
                    .show(childFragmentManager, null)
            }
            is NewVersionNotification -> {
                WhatsNewDialog(ctx, notification.sinceVersion)
                    .show()
            }
            is NewAchievementNotification -> {
                val f: Fragment = childFragmentManager.findFragmentById(R.id.achievement_info_fragment)!!
                (f as AchievementInfoFragment).showNew(notification.achievement, notification.level)
            }
            is QuestSelectionHintNotification -> {
                AlertDialog.Builder(ctx)
                    .setTitle(R.string.quest_selection_hint_title)
                    .setMessage(R.string.quest_selection_hint_message)
                    .setPositiveButton(R.string.quest_streetName_cantType_open_settings) { _, _ ->
                        startActivity(SettingsActivity.createLaunchQuestSettingsIntent(ctx))
                    }
                    .setNegativeButton(android.R.string.ok, null)
                    .show()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        for(f in childFragmentManager.fragments) {
            if (f is HandlesOnBackPressed) {
                if (f.onBackPressed()) return true
            }
        }
        return false
    }
}
