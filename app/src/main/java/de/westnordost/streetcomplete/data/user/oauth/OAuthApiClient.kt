package de.westnordost.streetcomplete.data.user.oauth

import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.http.takeFrom
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Client to get the access token as the third and final step of the OAuth authorization flow.
 *
 * Authorization flow:
 *
 * 1. Generate and store a OAuthAuthorizationParams instance and open the authorizationRequestUrl
 *    in a browser (or web view)
 * 2. Let user accept or deny the permission request in the browser (or web view). Authorization
 *    endpoint will call the redirect URI (aka callback URI) with parameters in either case.
 * 3. Check if the URI received is matches the OAuthAuthorizationParams instance with itsForMe(uri)
 *    and feed the received uri to OAuthService.retrieveAccessToken(uri)
 */
class OAuthApiClient(private val httpClient: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Retrieves the access token, given the [authorizationResponseUrl]
     *
     * @throws OAuthException if there has been an OAuth authorization error
     * @throws ConnectionException if the server reply is malformed or there is an issue with
     *                             the connection
     */
    suspend fun getAccessToken(
        request: OAuthAuthorizationParams,
        authorizationResponseUrl: String
    ): AccessTokenResponse = wrapApiClientExceptions {
        try {
            val response = httpClient.post(request.accessTokenUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                parameter("grant_type", "authorization_code")
                parameter("client_id", request.clientId)
                parameter("code", extractAuthorizationCode(authorizationResponseUrl))
                parameter("redirect_uri", request.redirectUri)
                parameter("code_verifier", request.codeVerifier)
                expectSuccess = true
            }
            val accessTokenResponse = json.decodeFromString<AccessTokenResponseJson>(response.body())
            if (accessTokenResponse.token_type.lowercase() != "bearer") {
                throw ConnectionException("OAuth 2 token endpoint returned an unknown token type (${accessTokenResponse.token_type})")
            }
            return AccessTokenResponse(accessTokenResponse.access_token, accessTokenResponse.scope?.split(" "))
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.BadRequest) {
                val errorResponse = json.decodeFromString<ErrorResponseJson>(e.response.body())
                throw OAuthException(errorResponse.error, errorResponse.error_description, errorResponse.error_uri)
            } else {
                throw e
            }
        }
    }
}

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
 */
@Serializable data class OAuthAuthorizationParams(
    val authorizationUrl: String,
    val accessTokenUrl: String,
    val clientId: String,
    val scopes: List<String>,
    val redirectUri: String,
    val state: String? = null
) {
    /**
     * For the code challenge as specified in RFC 7636
     * https://www.rfc-editor.org/rfc/rfc7636
     *
     * and required in the OAuth 2.1 draft
     * https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09
     */
    val codeVerifier: String = createRandomAlphanumericString(128)

    /**
     * Creates the URL to be opened in the browser or a web view in which the user agrees to
     * authorize the requested permissions.
     */
    val authorizationRequestUrl get() =
        URLBuilder().takeFrom(authorizationUrl).apply {
            parameters.append("response_type", "code")
            parameters.append("client_id", clientId)
            parameters.append("scope", scopes.joinToString(" "))
            parameters.append("redirect_uri", redirectUri)
            parameters.append("code_challenge_method", "S256")
            parameters.append("code_challenge", createPKCE_S256CodeChallenge(codeVerifier))
            state?.let { parameters.append("state", it) }
        }.build().toString()

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
            && callbackUri.pathSegments == uri2.pathSegments
            && callbackUri.parameters["state"] == state
    }
}

/**
 * Extracts the authorization code from a parameter of the callback uri
 *
 * @throws OAuthException if the URI does not contain the authorization code, e.g.
 *                        the user did not accept the requested permissions
 * @throws ConnectionException if the server reply is malformed
 */
private fun extractAuthorizationCode(uri: String): String {
    val parameters = Url(uri).parameters
    val authorizationCode = parameters["code"]
    if (authorizationCode != null) return authorizationCode

    val error = parameters["error"]
        ?: throw ConnectionException("OAuth 2 authorization endpoint did not return a valid error response: $uri")

    throw OAuthException(
        error.decodeURLQueryComponent(plusIsSpace = true),
        parameters["error_description"],
        parameters["error_uri"]
    )
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

private fun createRandomAlphanumericString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (0 until length).map { allowedChars.random() }.joinToString("")
}
