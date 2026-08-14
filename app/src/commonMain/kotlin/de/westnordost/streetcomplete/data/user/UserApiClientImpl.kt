package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.asSource
import kotlinx.io.buffered

class UserApiClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userAccessTokenSource: UserAccessTokenSource,
    private val userApiParser: UserApiParser,
) : UserApiClient {

    override suspend fun getMine(): UserInfo = wrapApiClientExceptions {
        val response = httpClient.get(baseUrl + "user/details") {
            userAccessTokenSource.accessToken?.let { bearerAuth(it) }
            expectSuccess = true
        }
        val source = response.bodyAsChannel().asSource().buffered()
        return userApiParser.parseUsers(source).first()
    }

    override suspend fun get(userId: Long): UserInfo? = wrapApiClientExceptions {
        try {
            val response = httpClient.get(baseUrl + "user/$userId") { expectSuccess = true }
            val source = response.bodyAsChannel().asSource().buffered()
            return userApiParser.parseUsers(source).first()
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Gone, HttpStatusCode.NotFound -> return null
                else -> throw e
            }
        }
    }
}
