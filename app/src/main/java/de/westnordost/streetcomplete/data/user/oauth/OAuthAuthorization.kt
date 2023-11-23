package de.westnordost.streetcomplete.data.user.oauth

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.user.AuthorizationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
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
 * 2. let user accept or deny the permission request in the browser. Authorization endpoint will
 *    call the redirect URI (aka callback URI) with parameters in either case.
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
 */
@Serializable class OAuthAuthorization(
    private val authorizationUrl: String,
    private val accessTokenUrl: String,
    private val clientId: String,
    private val scopes: List<String>,
    private val redirectUri: String
) {
    /** For the code challenge as specified in RFC 7636
     *  https://www.rfc-editor.org/rfc/rfc7636
     *
     *  and required in the OAuth 2.1 draft
     *  https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09
     */
    private val codeVerifier: String = createRandomAlphanumericString(128)

    /** identifies this oauth authorization flow, in case there are several at once */
    private val state: String = createRandomAlphanumericString(8)

    @Transient
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Creates the URL to be opened in the browser or a web view in which the user agrees to
     * authorize the requested permissions.
     */
    fun createAuthorizationUrl(): String =
        authorizationUrl + "?" + listOf(
            "response_type" to "code",
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_uri" to redirectUri,
            "code_challenge_method" to "S256",
            "code_challenge" to createPKCE_S256CodeChallenge(codeVerifier),
            "state" to state,
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
     * @throws AuthorizationException if the URI does not contain the authorization code, e.g.
     *                                   the user did not accept the requested permissions
     * @throws ConnectionException if there has been an error that is the server's fault (try again
     *                             later or open a bug report at openstreetmap-website if it
     *                             persists)
     */
    fun extractAuthorizationCode(uri: URI): String {
        val parameters = uri.getQueryParameters()
        val authorizationCode = parameters["code"]
        if (authorizationCode != null) return authorizationCode

        val error = parameters["error"]
            ?: throw ConnectionException("OAuth 2 authorization endpoint did not return a valid error response: $uri")

        val errorResponse = ErrorResponse(
            error,
            parameters["error_description"],
            parameters["error_uri"]
        )
        throw AuthorizationException(errorResponse.toErrorMessage())
    }

    /**
     * Retrieves the access token, using the previously retrieved [authorizationCode]
     *
     * @throws IOException if an I/O exception occurs.
     * @throws AuthorizationException if there has been an OAuth authorization error
     * @throws ConnectionException if there has been an error that is the server's fault (try again
     *                             later or open a bug report at openstreetmap-website if it
     *                             persists)
     */
    fun retrieveAccessToken(authorizationCode: String): String {
        val url = accessTokenUrl + "?" + listOfNotNull(
            "grant_type" to "authorization_code",
            "client_id" to clientId,
            "code" to authorizationCode,
            "redirect_uri" to redirectUri,
            "code_verifier" to codeVerifier,
        ).toUrlParameters()

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT)
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.requestMethod = "POST"

        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val body = connection.errorStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<ErrorResponse>(body)
                throw AuthorizationException(response.toErrorMessage())
            } else {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val response = json.decodeFromString<AccessTokenResponse>(body)
                if (response.token_type.lowercase() != "bearer") {
                    // hey! that's not what we asked for! (according to the RFC, the client MUST check this)
                    throw ConnectionException("OAuth 2 token endpoint returned an unknown token type (${response.token_type})")
                }
                return response.access_token
            }
        // if OSM server does not return valid JSON, it is the server's fault, hence
        } catch (e: SerializationException) {
            throw ConnectionException("OAuth 2 token endpoint did not return a valid response", e)
        } catch (e: IllegalArgumentException ) {
            throw ConnectionException("OAuth 2 token endpoint did not return a valid response", e)
        }
    }

    @Serializable
    private data class AccessTokenResponse(
        val access_token: String,
        val token_type: String,
        // OSM does currently not issue refresh tokens and the access token has no expiry date, so
        // we can ignore the below
        val expires_in: Long? = null,
        val refresh_token: String? = null,
    )

    @Serializable
    private data class ErrorResponse(
        val error: String,
        val error_description: String? = null,
        val error_uri: String? = null,
    ) {
        fun toErrorMessage(): String = listOfNotNull(
            error,
            error_description?.let { ": $error_description" },
            error_uri?.let { " (see $error_uri)" }
        ).joinToString("")
    }
}

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

private fun createRandomAlphanumericString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (0 ..< length).map { allowedChars.random() }.joinToString("")
}

private fun URI.getQueryParameters(): Map<String, String> =
    query?.split('&')?.associate {
        val parts = it.split('=')
        parts[0] to URLDecoder.decode(parts[1], "US-ASCII")
    }.orEmpty()
