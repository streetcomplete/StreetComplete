package de.westnordost.streetcomplete.data.notifications

import android.content.SharedPreferences
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.user.UserDataController
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import java.util.concurrent.CopyOnWriteArrayList

/** This has nothing to do with Android notifications. Android reserves far too many keywords for
 *  itself, really.
 *  This class is to access user notifications, which are basically dialogs that pop up when
 *  clicking on the bell icon, such as "you have a new OSM message in your inbox" etc. */
class NotificationsSource(
    private val userDataController: UserDataController,
    private val achievementsSource: AchievementsSource,
    private val questSelectionHintController: QuestSelectionHintController,
    private val prefs: SharedPreferences
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
    *  database table*/

    interface UpdateListener {
        fun onNumberOfNotificationsUpdated(numberOfNotifications: Int)
    }
    private val listeners: MutableList<UpdateListener> = CopyOnWriteArrayList()

    /** Achievement levels unlocked since application start. I.e. when restarting the app, the
     *  notifications about new achievements unlocked are lost, this is deliberate */
    private val newAchievements = ArrayList<Pair<Achievement, Int>>()

    init {
        userDataController.addListener(object : UserDataSource.Listener {
            override fun onUpdated() {
                onNumberOfNotificationsUpdated()
            }
        })
        achievementsSource.addListener(object : AchievementsSource.Listener {
            override fun onAchievementUnlocked(achievement: Achievement, level: Int) {
                newAchievements.add(achievement to level)
                onNumberOfNotificationsUpdated()
            }

            override fun onAllAchievementsUpdated() {
                // when all achievements have been updated, this doesn't spawn any notifications
            }
        })
        questSelectionHintController.addListener(object : QuestSelectionHintController.Listener {
            override fun onQuestSelectionHintStateChanged() {
                onNumberOfNotificationsUpdated()
            }
        })
    }

    fun addListener(listener: UpdateListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UpdateListener) {
        listeners.remove(listener)
    }

    fun getNumberOfNotifications(): Int {
        val shouldShowQuestSelectionHint = questSelectionHintController.state == QuestSelectionHintState.SHOULD_SHOW
        val hasUnreadMessages = userDataController.unreadMessagesCount > 0
        val lastVersion = prefs.getString(Prefs.LAST_VERSION, null)
        val hasNewVersion = lastVersion != null && BuildConfig.VERSION_NAME != lastVersion
        if (lastVersion == null) {
            prefs.edit().putString(Prefs.LAST_VERSION, BuildConfig.VERSION_NAME).apply()
        }

        var notifications = 0
        if (shouldShowQuestSelectionHint) notifications++
        if (hasUnreadMessages) notifications++
        if (hasNewVersion) notifications++
        notifications += newAchievements.size
        return notifications
    }

    fun popNextNotification(): Notification? {

        val lastVersion = prefs.getString(Prefs.LAST_VERSION, null)
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.edit().putString(Prefs.LAST_VERSION, BuildConfig.VERSION_NAME).apply()
            if (lastVersion != null) {
                onNumberOfNotificationsUpdated()
                return NewVersionNotification("v$lastVersion")
            }
        }

        val shouldShowQuestSelectionHint = questSelectionHintController.state == QuestSelectionHintState.SHOULD_SHOW
        if (shouldShowQuestSelectionHint) {
            questSelectionHintController.state = QuestSelectionHintState.SHOWN
            return QuestSelectionHintNotification
        }

        val newAchievement = newAchievements.removeFirstOrNull()
        if (newAchievement != null) {
            onNumberOfNotificationsUpdated()
            return NewAchievementNotification(newAchievement.first, newAchievement.second)
        }

        val unreadOsmMessages = userDataController.unreadMessagesCount
        if (unreadOsmMessages > 0) {
            userDataController.unreadMessagesCount = 0
            return OsmUnreadMessagesNotification(unreadOsmMessages)
        }

        return null
    }

    private fun onNumberOfNotificationsUpdated() {
        for (listener in listeners) {
            listener.onNumberOfNotificationsUpdated(getNumberOfNotifications())
        }
    }
}
