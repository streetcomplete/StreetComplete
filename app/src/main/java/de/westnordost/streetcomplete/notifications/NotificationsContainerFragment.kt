package de.westnordost.streetcomplete.notifications

import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.about.WhatsNewDialog
import de.westnordost.streetcomplete.data.notifications.NewAchievementNotification
import de.westnordost.streetcomplete.data.notifications.NewVersionNotification
import de.westnordost.streetcomplete.data.notifications.Notification
import de.westnordost.streetcomplete.data.notifications.OsmUnreadMessagesNotification
import de.westnordost.streetcomplete.user.AchievementInfoFragment

/** A fragment that contains any fragments that would show notifications.
 *  Usually, notifications are shown as dialogs, however there is currently one exception which
 *  makes this necessary as a fragment */
class NotificationsContainerFragment : Fragment(R.layout.fragment_notifications_container) {

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
        }
    }
}