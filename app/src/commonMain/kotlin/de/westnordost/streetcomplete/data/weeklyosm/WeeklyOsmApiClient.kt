package de.westnordost.streetcomplete.data.weeklyosm

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.asSource
import kotlinx.datetime.Instant
import kotlinx.io.buffered

class WeeklyOsmApiClient(
    private val httpClient: HttpClient,
    private val parser: WeeklyOsmRssFeedParser,
) {
    suspend fun getLastPublishDate(): Instant? {
        val response = httpClient.get("https://weeklyosm.eu/feed")
        val source = response.bodyAsChannel().asSource().buffered()
        return parser.parseLastPublishDate(source)
    }
}
