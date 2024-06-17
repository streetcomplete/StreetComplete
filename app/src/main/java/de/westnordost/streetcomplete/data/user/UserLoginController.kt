package de.westnordost.streetcomplete.data.user

import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.Listeners

class UserLoginController(
    private val osmConnection: OsmConnection,
    private val prefs: ObservableSettings,
) : UserLoginSource {

    private val listeners = Listeners<UserLoginSource.Listener>()

    override val isLoggedIn: Boolean get() = accessToken != null

    override val accessToken: String? get() =
        prefs.getStringOrNull(Prefs.OAUTH2_ACCESS_TOKEN)

    fun logIn(accessToken: String) {
        prefs.putString(Prefs.OAUTH2_ACCESS_TOKEN, accessToken)
        osmConnection.oAuthAccessToken = accessToken
        listeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        prefs.remove(Prefs.OAUTH2_ACCESS_TOKEN)
        prefs.remove(Prefs.OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP)
        prefs.remove(Prefs.OAUTH1_ACCESS_TOKEN)
        prefs.remove(Prefs.OAUTH1_ACCESS_TOKEN_SECRET)
        osmConnection.oAuthAccessToken = null
        listeners.forEach { it.onLoggedOut() }
    }

    override fun addListener(listener: UserLoginSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginSource.Listener) {
        listeners.remove(listener)
    }
}
