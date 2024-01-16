package de.westnordost.streetcomplete.data.user

import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.prefs.Preferences

class UserLoginStatusController(
    private val osmConnection: OsmConnection,
    private val prefs: Preferences,
) : UserLoginStatusSource {

    private val listeners = Listeners<UserLoginStatusSource.Listener>()

    override val isLoggedIn: Boolean get() =
        prefs.getStringOrNull(Prefs.OAUTH2_ACCESS_TOKEN) != null

    fun logIn(accessToken: String) {
        prefs.putString(Prefs.OAUTH2_ACCESS_TOKEN, accessToken)
        osmConnection.oAuthAccessToken = accessToken
        listeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        prefs.putString(Prefs.OAUTH2_ACCESS_TOKEN, null)
        prefs.remove(Prefs.OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP)
        prefs.remove(Prefs.OAUTH1_ACCESS_TOKEN)
        prefs.remove(Prefs.OAUTH1_ACCESS_TOKEN_SECRET)
        osmConnection.oAuthAccessToken = null
        listeners.forEach { it.onLoggedOut() }
    }

    override fun addListener(listener: UserLoginStatusSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginStatusSource.Listener) {
        listeners.remove(listener)
    }
}
