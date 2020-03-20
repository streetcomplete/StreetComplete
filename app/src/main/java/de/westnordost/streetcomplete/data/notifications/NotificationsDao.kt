package de.westnordost.streetcomplete.data.notifications

import android.content.SharedPreferences
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.user.UserStore
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** This has nothing to do with Android notifications. Android reserves far too many keywords for
 *  itself, really.
 *  This class is to access user notifications, which are basically dialogs that pop up when
 *  clicking on the bell icon, such as "you have a new OSM message in your inbox" etc. */
@Singleton class NotificationsDao @Inject constructor(
    private val userStore: UserStore,
    private val newUserAchievementsDao: NewUserAchievementsDao,
    @Named("Achievements") achievements: List<Achievement>,
    private val prefs: SharedPreferences
) {
    interface UpdateListener {
        fun onNumberOfNotificationsUpdated(numberOfNotifications: Int)
    }
    private val listeners: MutableList<UpdateListener> = CopyOnWriteArrayList()

    private val achievementsById = achievements.associateBy { it.id }

    init {
        userStore.addListener(object : UserStore.UpdateListener {
            override fun onUserDataUpdated() {
                onNumberOfNotificationsUpdated()
            }
        })
        newUserAchievementsDao.addListener(object : NewUserAchievementsDao.UpdateListener {
            override fun onNewUserAchievementsUpdated() {
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
        val hasUnreadMessages = userStore.unreadMessagesCount > 0
        val hasNewVersion = BuildConfig.VERSION_NAME != prefs.getString(Prefs.LAST_VERSION, null)

        var notifications = 0
        if (hasUnreadMessages) notifications++
        if (hasNewVersion) notifications++
        notifications += newUserAchievementsDao.getCount()
        return notifications
    }

    fun popNextNotification(): Notification? {

        val lastVersion = prefs.getString(Prefs.LAST_VERSION, null)
        if (BuildConfig.VERSION_NAME != lastVersion) {
            prefs.edit().putString(Prefs.LAST_VERSION, BuildConfig.VERSION_NAME).apply()
            onNumberOfNotificationsUpdated()
            return NewVersionNotification("v$lastVersion")
        }

        val newAchievement = newUserAchievementsDao.pop()
        if (newAchievement != null) {
            onNumberOfNotificationsUpdated()
            val achievement = achievementsById[newAchievement.first]
            if (achievement != null) {
                return NewAchievementNotification(achievement, newAchievement.second)
            }
        }

        val unreadOsmMessages = userStore.unreadMessagesCount
        if (unreadOsmMessages > 0) {
            userStore.unreadMessagesCount = 0
            onNumberOfNotificationsUpdated()
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

