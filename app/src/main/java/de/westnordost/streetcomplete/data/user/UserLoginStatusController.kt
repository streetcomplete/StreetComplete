package de.westnordost.streetcomplete.data.user

import de.westnordost.osmapi.OsmConnection
import oauth.signpost.OAuthConsumer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class UserLoginStatusController @Inject constructor(
    private val oAuthStore: OAuthStore,
    private val osmConnection: OsmConnection,
): UserLoginStatusSource {

    private val listeners: MutableList<UserLoginStatusSource.Listener> = CopyOnWriteArrayList()

    override val isLoggedIn: Boolean get() = oAuthStore.isAuthorized

    fun logIn(consumer: OAuthConsumer) {
        oAuthStore.oAuthConsumer = consumer
        osmConnection.oAuth = consumer
        listeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        oAuthStore.oAuthConsumer = null
        osmConnection.oAuth = null
        listeners.forEach { it.onLoggedOut() }
    }

    override fun addListener(listener: UserLoginStatusSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginStatusSource.Listener) {
        listeners.remove(listener)
    }
}
