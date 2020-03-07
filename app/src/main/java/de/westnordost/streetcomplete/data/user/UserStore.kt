package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.osmapi.user.UserDetails
import de.westnordost.streetcomplete.Prefs
import javax.inject.Inject

class UserStore @Inject constructor(private val prefs: SharedPreferences) {

    val userId: Long get() = prefs.getLong(Prefs.OSM_USER_ID, -1)
    val userName: String? get() = prefs.getString(Prefs.OSM_USER_NAME, null)

    var daysActive: Int
        get() = prefs.getInt(Prefs.USER_DAYS_ACTIVE, 0)
        set(value) = prefs.edit { putInt(Prefs.USER_DAYS_ACTIVE, value) }

    var unreadMessagesCount: Int
    get() = prefs.getInt(Prefs.OSM_UNREAD_MESSAGES, 0)
    set(value) = prefs.edit { putInt(Prefs.OSM_UNREAD_MESSAGES, value) }

    fun setDetails(userDetails: UserDetails) {
        prefs.edit {
            putLong(Prefs.OSM_USER_ID, userDetails.id)
            putString(Prefs.OSM_USER_NAME, userDetails.displayName)
            putInt(Prefs.OSM_UNREAD_MESSAGES, userDetails.unreadMessagesCount)
        }
    }

    fun clear() {
        prefs.edit {
            remove(Prefs.OSM_USER_ID)
            remove(Prefs.OSM_USER_NAME)
            remove(Prefs.OSM_UNREAD_MESSAGES)
            remove(Prefs.USER_DAYS_ACTIVE)
        }
    }
}
