package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClient
import de.westnordost.streetcomplete.data.user.oauth.OAuthException
import de.westnordost.streetcomplete.util.logs.Log


// Finishes the OAuth login flow when an authorization callback URL is received.
class OAuthLoginCompleter(
    private val oAuthApiClient: OAuthApiClient,
    private val userLoginController: UserLoginController,
    private val oAuthCallbackHandler: OAuthCallbackHandler
) {

    suspend fun processCallback(authorizationResponseUrl: String): Boolean {

        val oAuthParams = oAuthCallbackHandler.getStoredOAuthParams() ?: return false

        return try {
            val tokenResponse = oAuthApiClient.getAccessToken(oAuthParams, authorizationResponseUrl)
            if (tokenResponse.grantedScopes?.containsAll(OAUTH2_REQUIRED_SCOPES) == false) {
                return false
            }

            userLoginController.logIn(tokenResponse.accessToken)

            // Clear stored params after successful login
            oAuthCallbackHandler.clearStoredParams()
            true
        } catch (e: Exception) {
            if (e is OAuthException && e.error == "access_denied") {
                Log.w(TAG, "OAuth access denied by user")
            } else {
                Log.e(TAG, "Error finishing OAuth login: ${e.message}", e)
            }
            false
        }
    }

    companion object {
        private const val TAG = "OAuthLoginCompleter"
    }
}

