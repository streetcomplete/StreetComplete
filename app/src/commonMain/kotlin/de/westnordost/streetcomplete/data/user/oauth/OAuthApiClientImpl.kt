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
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class OAuthApiClientImpl(private val httpClient: HttpClient) : OAuthApiClient {
    private val json = Json { ignoreUnknownKeys = true }

    override fun getAuthorizationRequestUrl(request: OAuthAuthorizationParams): String =
        URLBuilder().takeFrom(request.authorizationUrl).apply {
            parameters.append("response_type", "code")
            parameters.append("client_id", request.clientId)
            parameters.append("scope", request.scopes.joinToString(" "))
            parameters.append("redirect_uri", request.redirectUri)
            parameters.append("code_challenge_method", "S256")
            parameters.append("code_challenge", createPKCE_S256CodeChallenge(request.codeVerifier))
            request.state?.let { parameters.append("state", it) }
        }.build().toString()

    override suspend fun getAccessToken(
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
 * S256: code_challenge = BASE64URL-ENCODE(SHA256(ASCII(code_verifier)))
 *
 * See https://www.rfc-editor.org/rfc/rfc7636
 */
@OptIn(ExperimentalEncodingApi::class)
private fun createPKCE_S256CodeChallenge(codeVerifier: String): String {
    // this encodes in UTF-8, but as the code verifier is only A-Z, a-z, 0-9, that's fine because
    // UTF-8 is a superset of ASCII
    val encodedBytes = codeVerifier.encodeToByteArray()

    val sha256 = SHA256().digest(encodedBytes)

    return Base64.UrlSafe.encode(sha256).split("=")[0]
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
