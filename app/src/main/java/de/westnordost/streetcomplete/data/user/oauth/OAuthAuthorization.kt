package de.westnordost.streetcomplete.data.user.oauth

import de.westnordost.streetcomplete.ApplicationConstants
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * One authorization with OAuth. For each authorization request, a new instance must be created
 *
 * Authorization flow:
 *
 * 1. createAuthorizationUrl() and open it in a browser (or web view)
 * 2. let user accept or deny the permission request in the browser (or web view). Authorization
 *    endpoint will call the redirect URI (aka callback URI) with parameters in either case.
 * 3. check if the URI received is for this instance with itsForMe(uri) and then
 *    extractAuthorizationCode(uri) from that URI
 * 4. retrieveAccessToken(authorizationCode) with the retrieved authorizationCode
 *
 *
 * @param authorizationUrl the OAuth2 server authorization endpoint
 * @param accessTokenUrl the OAuth2 server token endpoint
 * @param clientId the OAuth2 client id
 * @param scopes the scopes (aka permissions) to request
 * @param redirectUri the redirect URI (aka callback URI) that the authorization endpoint should
 *                    call when the user allowed the authorization.
 * @param state optional string to identify this oauth authorization flow, in case there are
 *              several at once (using the same redirect URI)
 */
@Serializable class OAuthAuthorization(
    private val authorizationUrl: String,
    private val accessTokenUrl: String,
    private val clientId: String,
    private val scopes: List<String>,
    private val redirectUri: String,
    private val state: String? = null
) {
    /**
     * For the code challenge as specified in RFC 7636
     * https://www.rfc-editor.org/rfc/rfc7636
     *
     * and required in the OAuth 2.1 draft
     * https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09
     */
    private val codeVerifier: String = createRandomAlphanumericString(128)

    @Transient
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Creates the URL to be opened in the browser or a web view in which the user agrees to
     * authorize the requested permissions.
     */
    fun createAuthorizationUrl(): String =
        authorizationUrl + "?" + listOfNotNull(
            "response_type" to "code",
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_uri" to redirectUri,
            "code_challenge_method" to "S256",
            "code_challenge" to createPKCE_S256CodeChallenge(codeVerifier),
            state?.let { "state" to it },
        ).toUrlParameters()

    /**
     * Checks whether the given callback uri is meant for this instance
     */
    fun itsForMe(uri: URI): Boolean {
        val uri2 = URI(redirectUri)
        return uri.scheme == uri2.scheme && uri.authority == uri2.authority && uri.path == uri2.path
            && uri.getQueryParameters()["state"] == state
    }

    /**
     * Extracts the authorization code from a parameter of the callback uri
     *
     * @throws OAuthException if the URI does not contain the authorization code, e.g.
     *                        the user did not accept the requested permissions
     * @throws OAuthConnectionException if the server reply is malformed
     */
    fun extractAuthorizationCode(uri: URI): String {
        val parameters = uri.getQueryParameters()
        val authorizationCode = parameters["code"]
        if (authorizationCode != null) return authorizationCode

        val error = parameters["error"]
            ?: throw OAuthConnectionException("OAuth 2 authorization endpoint did not return a valid error response: $uri")

        throw OAuthException(error, parameters["error_description"], parameters["error_uri"])
    }

    /**
     * Retrieves the access token, using the previously retrieved [authorizationCode]
     *
     * @throws OAuthException if there has been an OAuth authorization error
     * @throws OAuthConnectionException if the server reply is malformed or there is an issue with
     *                                   the connection
     */
    fun retrieveAccessToken(authorizationCode: String): AccessTokenResponse {
        val url = accessTokenUrl + "?" + listOfNotNull(
            "grant_type" to "authorization_code",
            "client_id" to clientId,
            "code" to authorizationCode,
            "redirect_uri" to redirectUri,
            "code_verifier" to codeVerifier,
        ).toUrlParameters()

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT)
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.requestMethod = "POST"

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val body = connection.errorStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<ErrorResponseJson>(body)
                throw OAuthException(response.error, response.error_description, response.error_uri)
            } else {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<AccessTokenResponseJson>(body)
                if (response.token_type.lowercase() != "bearer") {
                    // hey! that's not what we asked for! (according to the RFC, the client MUST check this)
                    throw OAuthConnectionException("OAuth 2 token endpoint returned an unknown token type (${response.token_type})")
                }
                return AccessTokenResponse(response.access_token, response.scope?.split(" "))
            }
        // if OSM server does not return valid JSON, it is the server's fault, hence
        } catch (e: SerializationException) {
            throw OAuthConnectionException("OAuth 2 token endpoint did not return a valid response", e)
        } catch (e: IllegalArgumentException) {
            throw OAuthConnectionException("OAuth 2 token endpoint did not return a valid response", e)
        } catch (e: IOException) {
            throw OAuthConnectionException(cause = e)
        }
    }
}

data class AccessTokenResponse(
    val accessToken: String,
    /** Granted scopes may be null if all requested scopes were granted */
    val grantedScopes: List<String>? = null
)

@Serializable
private data class AccessTokenResponseJson(
    val access_token: String,
    val token_type: String,
    val scope: String? = null,
    // OSM does currently not issue refresh tokens and the access token has no expiry date, so
    // we can ignore the below
    val expires_in: Long? = null,
    val refresh_token: String? = null,
)

@Serializable
private data class ErrorResponseJson(
    val error: String,
    val error_description: String? = null,
    val error_uri: String? = null,
)

/**
 * Create the RFC 7636 Proof Key for Code Exchange from a random string.
 *
 * See https://www.rfc-editor.org/rfc/rfc7636
 */
@OptIn(ExperimentalEncodingApi::class)
private fun createPKCE_S256CodeChallenge(codeVerifier: String): String {
    // S256: code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
    val encodedBytes = codeVerifier.toByteArray(StandardCharsets.US_ASCII)
    val sha256 = MessageDigest.getInstance("SHA-256").digest(encodedBytes)
    return Base64.UrlSafe.encode(sha256).split("=")[0]
}

private fun Iterable<Pair<String, String>>.toUrlParameters(): String =
    joinToString("&") { (k, v) -> k + "=" + URLEncoder.encode(v, "US-ASCII") }

private fun createRandomAlphanumericString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (0 until length).map { allowedChars.random() }.joinToString("")
}

private fun URI.getQueryParameters(): Map<String, String> =
    query?.split('&')?.associate {
        val parts = it.split('=')
        parts[0] to URLDecoder.decode(parts[1], "US-ASCII")
    }.orEmpty()
