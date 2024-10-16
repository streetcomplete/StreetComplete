package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.Listeners

/** Controller that handles user login, logout, auth and updated data */
class UserDataController(private val prefs: Preferences) : UserDataSource {

    private val listeners = Listeners<UserDataSource.Listener>()

    override val userId: Long get() = prefs.userId
    override val userName: String? get() = prefs.userName

    override var unreadMessagesCount: Int
        get() = prefs.userUnreadMessages
        set(value) {
            prefs.userUnreadMessages = value
            listeners.forEach { it.onUpdated() }
        }

    fun setDetails(userDetails: UserInfo) {
        prefs.userId = userDetails.id
        prefs.userName = userDetails.displayName
        userDetails.unreadMessagesCount?.let { prefs.userUnreadMessages }
        listeners.forEach { it.onUpdated() }
    }

    fun clear() {
        prefs.clearUserData()
        listeners.forEach { it.onUpdated() }
    }

    override fun addListener(listener: UserDataSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserDataSource.Listener) {
        listeners.remove(listener)
    }
}
