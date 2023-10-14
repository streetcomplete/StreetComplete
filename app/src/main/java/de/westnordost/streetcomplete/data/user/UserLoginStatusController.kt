package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.Listeners
import oauth.signpost.OAuthConsumer

class UserLoginStatusController(
    private val oAuthStore: OAuthStore,
    private val osmConnection: OsmConnection,
    private val prefs: SharedPreferences,
) : UserLoginStatusSource {

    private val listeners = Listeners<UserLoginStatusSource.Listener>()

    override val isLoggedIn: Boolean get() = oAuthStore.isAuthorized

    fun logIn(consumer: OAuthConsumer) {
        oAuthStore.oAuthConsumer = consumer
        osmConnection.oAuth = consumer
        prefs.edit { putBoolean(Prefs.OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP, true) }
        listeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        oAuthStore.oAuthConsumer = null
        osmConnection.oAuth = null
        prefs.edit { putBoolean(Prefs.OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP, false) }
        listeners.forEach { it.onLoggedOut() }
    }

    override fun addListener(listener: UserLoginStatusSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginStatusSource.Listener) {
        listeners.remove(listener)
    }
}
