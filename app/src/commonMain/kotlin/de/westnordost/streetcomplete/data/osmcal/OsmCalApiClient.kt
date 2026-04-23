package de.westnordost.streetcomplete.data.osmcal

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.asSource
import kotlinx.io.buffered

class OsmCalApiClient(
    private val httpClient: HttpClient,
    private val parser: OsmCalParser,
) {
    suspend fun getEvents(): List<CalendarEvent> {
        val response = httpClient.get("https://osmcal.org/api/v2/events/")
        val source = response.bodyAsChannel().asSource().buffered()
        return parser.parse(source)
    }
}
