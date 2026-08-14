package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.asSource
import kotlinx.io.buffered

class StatisticsApiClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val statisticsParser: StatisticsParser
) : StatisticsApiClient {
    override suspend fun get(osmUserId: Long): Statistics = wrapApiClientExceptions {
        val response = httpClient.get("$baseUrl?user_id=$osmUserId") { expectSuccess = true }
        return statisticsParser.parse(response.bodyAsChannel().asSource().buffered())
    }
}
