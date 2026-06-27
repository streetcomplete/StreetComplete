package de.westnordost.streetcomplete.data.user.oauth

import de.westnordost.streetcomplete.data.ConnectionException
import io.ktor.http.URLParserException
import io.ktor.http.Url
import kotlinx.serialization.Serializable

/**
 * Client to get the authorization request URL and then later the access token as the third and
 * final step of the OAuth authorization flow.
 *
 * Authorization flow:
 *
 * 1. Generate and store a [OAuthAuthorizationParams] instance and call [getAuthorizationRequestUrl]
 *    to get the URL to be opened in a browser (or web view).
 *
 * 2. Let user accept or deny the permission request in the browser (or web view). Authorization
 *    endpoint will call the redirect URI (aka callback URI) with parameters in either case.
 *
 * 3. Check if the URI received matches the [OAuthAuthorizationParams] used in the previous steps
 *    with [OAuthAuthorizationParams.itsForMe] and if yes, feed the received uri to
 *    [getAccessToken] to finally get the access token.
 */
interface OAuthApiClient {
    /**
     * Creates the URL to be opened in the browser or a web view in which the user agrees to
     * authorize the requested permissions.
     */
    fun getAuthorizationRequestUrl(request: OAuthAuthorizationParams): String

    /**
     * Retrieves the access token. Needs the [authorizationResponseUrl] returned from the previous
     * authorization in the browser or web view (see [getAuthorizationRequestUrl]).
     *
     * @throws OAuthException if there has been an OAuth authorization error
     * @throws ConnectionException if the server reply is malformed or there is an issue with
     *                             the connection
     */
    suspend fun getAccessToken(
        request: OAuthAuthorizationParams,
        authorizationResponseUrl: String
    ): AccessTokenResponse
}

data class AccessTokenResponse(
    val accessToken: String,
    /** Granted scopes may be null if all requested scopes were granted */
    val grantedScopes: List<String>? = null
)

/**
 * One authorization with OAuth. For each authorization request, a new instance must be created
 *
 * @param authorizationUrl the OAuth2 server authorization endpoint
 * @param accessTokenUrl the OAuth2 server token endpoint
 * @param clientId the OAuth2 client id
 * @param scopes the scopes (aka permissions) to request
 * @param redirectUri the redirect URI (aka callback URI) that the authorization endpoint should
 *                    call when the user allowed the authorization.
 * @param state optional string to identify this oauth authorization flow, in case there are
 *              several at once (using the same redirect URI)
 * @param codeVerifier For the code challenge as specified in RFC 7636
 *                     (https://www.rfc-editor.org/rfc/rfc7636) and required in the OAuth 2.1 draft
 *                     (https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09)
 */
@Serializable data class OAuthAuthorizationParams(
    val authorizationUrl: String,
    val accessTokenUrl: String,
    val clientId: String,
    val scopes: List<String>,
    val redirectUri: String,
    val state: String? = null,
    val codeVerifier: String = createRandomAlphanumericString(128)
) {
    /**
     * Checks whether the given callback uri is meant for this instance
     */
    fun itsForMe(callBackUri: String): Boolean = try {
        itsForMe(Url(callBackUri))
    } catch (e: URLParserException) {
        false
    }

    private fun itsForMe(callbackUri: Url): Boolean {
        val uri2 = Url(redirectUri)
        return callbackUri.protocol == uri2.protocol
            && callbackUri.host == uri2.host
            && callbackUri.segments == uri2.segments
            && callbackUri.parameters["state"] == state
    }
}

fun createRandomAlphanumericString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (0 until length).map { allowedChars.random() }.joinToString("")
}
