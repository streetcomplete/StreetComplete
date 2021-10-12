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
        listeners.forEach { it.onUserDataUpdated() }
    }

    fun clear() {
        prefs.edit(true) {
            remove(Prefs.OSM_USER_ID)
            remove(Prefs.OSM_USER_NAME)
            remove(Prefs.OSM_UNREAD_MESSAGES)
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
