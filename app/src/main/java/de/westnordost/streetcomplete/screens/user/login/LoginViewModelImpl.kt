package de.westnordost.streetcomplete.screens.user.login

import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.OAUTH2_AUTHORIZATION_URL
import de.westnordost.streetcomplete.data.user.OAUTH2_CLIENT_ID
import de.westnordost.streetcomplete.data.user.OAUTH2_REDIRECT_URI
import de.westnordost.streetcomplete.data.user.OAUTH2_REQUESTED_SCOPES
import de.westnordost.streetcomplete.data.user.OAUTH2_REQUIRED_SCOPES
import de.westnordost.streetcomplete.data.user.OAUTH2_TOKEN_URL
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.oauth.OAuthAuthorizationParams
import de.westnordost.streetcomplete.data.user.oauth.OAuthException
import de.westnordost.streetcomplete.data.user.oauth.OAuthService
import de.westnordost.streetcomplete.data.user.oauth.extractAuthorizationCode
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class LoginViewModelImpl(
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val userLoginStatusController: UserLoginStatusController,
    private val oAuthService: OAuthService,
    private val userUpdater: UserUpdater
) : LoginViewModel() {
    override val loginState = MutableStateFlow<LoginState>(LoggedOut)
    override val unsyncedChangesCount = MutableStateFlow(0)

    override val authorizationRequestUrl: String get() = oAuth.authorizationRequestUrl

    private val oAuth = OAuthAuthorizationParams(
        OAUTH2_AUTHORIZATION_URL,
        OAUTH2_TOKEN_URL,
        OAUTH2_CLIENT_ID,
        OAUTH2_REQUESTED_SCOPES,
        OAUTH2_REDIRECT_URI
    )

    init {
        launch(IO) {
            unsyncedChangesCount.update { unsyncedChangesCountSource.getCount() }
        }
    }

    override fun startLogin() {
        loginState.compareAndSet(LoggedOut, RequestingAuthorization)
    }

    override fun failAuthorization(url: String, errorCode: Int, description: String?) {
        Log.e(TAG, "Error for URL " + url + if (description != null) ": $description" else "")
        loginState.compareAndSet(RequestingAuthorization, LoginError.CommunicationError)
    }

    override fun isAuthorizationResponseUrl(url: String): Boolean =
        oAuth.itsForMe(url)

    override fun finishAuthorization(authorizationResponseUrl: String) {
        launch {
            val accessToken = retrieveAccessToken(authorizationResponseUrl)
            if (accessToken != null) {
                login(accessToken)
            }
        }
    }

    private suspend fun retrieveAccessToken(authorizationResponseUrl: String): String? {
        try {
            loginState.value = RetrievingAccessToken
            val authorizationCode = extractAuthorizationCode(authorizationResponseUrl)
            val accessTokenResponse = withContext(IO) {
                oAuthService.retrieveAccessToken(oAuth, authorizationCode)
            }
            if (accessTokenResponse.grantedScopes?.containsAll(OAUTH2_REQUIRED_SCOPES) == false) {
                loginState.value = LoginError.RequiredPermissionsNotGranted
                return null
            }
            return accessTokenResponse.accessToken
        } catch (e: Exception) {
            if (e is OAuthException && e.error == "access_denied") {
                loginState.value = LoginError.RequiredPermissionsNotGranted
            } else {
                Log.e(TAG, "Error during authorization", e)
                loginState.value = LoginError.CommunicationError
            }
            return null
        }
    }

    private suspend fun login(accessToken: String) {
        loginState.value = LoggedIn
        userLoginStatusController.logIn(accessToken)
        userUpdater.update()
    }

    override fun resetLogin() {
        if (loginState.value is LoginError) loginState.value = LoggedOut
    }

    companion object {
        private const val TAG = "Login"
    }
}
