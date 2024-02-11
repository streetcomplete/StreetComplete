package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.ApplicationConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.header

/** Downloads statistics from the backend */
class StatisticsDownloader(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val statisticsParser: StatisticsParser
) {
    suspend fun download(osmUserId: Long): Statistics {
        val response = httpClient.get("$baseUrl?user_id=$osmUserId") {
            expectSuccess = true
            header("User-Agent", ApplicationConstants.USER_AGENT)
        }
        return statisticsParser.parse(response.body())
    }
}
