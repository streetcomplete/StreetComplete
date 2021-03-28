package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.osmapi.user.UserDetails
import de.westnordost.streetcomplete.Prefs
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Stores OSM user data.
 *
 *  Must be the only access to these values (=singleton) to ensure that
 *  other classes listening to updates are properly notified. */
@Singleton class UserStore @Inject constructor(private val prefs: SharedPreferences) {

    interface UpdateListener {
        fun onUserDataUpdated()
    }
    private val listeners: MutableList<UpdateListener> = CopyOnWriteArrayList()

    val userId: Long get() = prefs.getLong(Prefs.OSM_USER_ID, -1)
    val userName: String? get() = prefs.getString(Prefs.OSM_USER_NAME, null)

    var rank: Int
        get() = prefs.getInt(Prefs.USER_GLOBAL_RANK, -1)
        set(value) {
            prefs.edit(true) { putInt(Prefs.USER_GLOBAL_RANK, value) }
        }

    var daysActive: Int
        get() = prefs.getInt(Prefs.USER_DAYS_ACTIVE, 0)
        set(value) {
            prefs.edit(true) { putInt(Prefs.USER_DAYS_ACTIVE, value) }
        }

    var lastStatisticsUpdate: Long
    get() = prefs.getLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, 0)
    set(value) {
        prefs.edit(true) { putLong(Prefs.USER_LAST_TIMESTAMP_ACTIVE, value) }
    }

    var isSynchronizingStatistics: Boolean
        // default true because if it is not set yet, the first thing that is done is to synchronize it
        get() = prefs.getBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, true)
        set(value) {
            prefs.edit(true) { putBoolean(Prefs.IS_SYNCHRONIZING_STATISTICS, value) }
        }

    var unreadMessagesCount: Int
    get() = prefs.getInt(Prefs.OSM_UNREAD_MESSAGES, 0)
    set(value) {
        prefs.edit(true) { putInt(Prefs.OSM_UNREAD_MESSAGES, value) }
        onUserDetailsUpdated()
    }

    fun setDetails(userDetails: UserDetails) {
        prefs.edit(true) {
            putLong(Prefs.OSM_USER_ID, userDetails.id)
            putString(Prefs.OSM_USER_NAME, userDetails.displayName)
            putInt(Prefs.OSM_UNREAD_MESSAGES, userDetails.unreadMessagesCount)
        }
        onUserDetailsUpdated()
    }

    fun clear() {
        prefs.edit(true) {
            remove(Prefs.OSM_USER_ID)
            remove(Prefs.OSM_USER_NAME)
            remove(Prefs.OSM_UNREAD_MESSAGES)
            remove(Prefs.USER_DAYS_ACTIVE)
            remove(Prefs.IS_SYNCHRONIZING_STATISTICS)
        }
    }

    fun addListener(listener: UpdateListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UpdateListener) {
        listeners.remove(listener)
    }

    private fun onUserDetailsUpdated() {
        for (listener in listeners) {
            listener.onUserDataUpdated()
        }
    }
}
