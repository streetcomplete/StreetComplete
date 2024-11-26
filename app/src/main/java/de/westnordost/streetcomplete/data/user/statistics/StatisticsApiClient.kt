package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get

/** Client for the statistics service
 *  https://github.com/streetcomplete/sc-statistics-service/ */
class StatisticsApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val statisticsParser: StatisticsParser
) {
    /** Get the statistics for the given user id
     *
     * @throws ConnectionException on connection or server error */
    suspend fun get(osmUserId: Long): Statistics = wrapApiClientExceptions {
        val response = httpClient.get("$baseUrl?user_id=$osmUserId") { expectSuccess = true }
        return statisticsParser.parse(response.body())
    }
}
