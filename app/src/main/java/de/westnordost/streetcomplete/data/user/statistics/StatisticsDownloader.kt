package de.westnordost.streetcomplete.data.user.statistics

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get

/** Downloads statistics from the backend */
class StatisticsDownloader(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val statisticsParser: StatisticsParser
) {
    suspend fun download(osmUserId: Long): Statistics {
        val response = httpClient.get("$baseUrl?user_id=$osmUserId") { expectSuccess = true }
        return statisticsParser.parse(response.body())
    }
}
