package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.prefs.Preferences
import oauth.signpost.OAuthConsumer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** Manages saving and loading OAuthConsumer persistently  */
class OAuthStore(
    private val prefs: Preferences,
) : KoinComponent {
    var oAuthConsumer: OAuthConsumer?
        get() {
            val result = get<OAuthConsumer>()
            val accessToken = prefs.getStringOrNull(Prefs.OAUTH_ACCESS_TOKEN)
            val accessTokenSecret = prefs.getStringOrNull(Prefs.OAUTH_ACCESS_TOKEN_SECRET)
            if (accessToken == null || accessTokenSecret == null) return null
            result.setTokenWithSecret(accessToken, accessTokenSecret)
            return result
        }
        set(value) {
            if (value != null && value.token != null && value.tokenSecret != null) {
                prefs.putString(Prefs.OAUTH_ACCESS_TOKEN, value.token)
                prefs.putString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, value.tokenSecret)
            } else {
                prefs.remove(Prefs.OAUTH_ACCESS_TOKEN)
                prefs.remove(Prefs.OAUTH_ACCESS_TOKEN_SECRET)
            }
        }

    val isAuthorized: Boolean
        get() = prefs.getStringOrNull(Prefs.OAUTH_ACCESS_TOKEN_SECRET) != null
}
