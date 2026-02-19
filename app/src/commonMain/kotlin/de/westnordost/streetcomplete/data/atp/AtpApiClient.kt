package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.asSource
import kotlinx.io.buffered
import kotlin.math.floor

/**
 * Communicates with separate API providing ATP entries.
 * TODO: also sends info when entries were wonky
 */
class AtpApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    //private val userAccessTokenSource: UserAccessTokenSource,
    private val atpApiParser: AtpApiParser
) {
    /**
     * Retrieve all atp entries in the given area
     *
     * @param bounds the area within where ATP entries should be queried.
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     * @throws IllegalArgumentException if the bounds cross the 180th meridian.
     *
     * @return the incoming atp entries
     */
    suspend fun getAllAtpEntries(bounds: BoundingBox): List<AtpEntry> = wrapApiClientExceptions {
        if (bounds.crosses180thMeridian) {
            throw IllegalArgumentException("Bounding box crosses 180th meridian")
        }
        val gathered = mutableListOf<AtpEntry>()
        // example: https://bbox-filter-for-atp.bulwersator-cloudflare.workers.dev/api/entries?lat_min=50&lat_max=50.05&lon_min=19.9&lon_max=20.1
        val url = baseUrl + "entries?lat_min=${bounds.min.latitude}&lat_max=${bounds.max.latitude}&lon_min=${bounds.min.longitude}&lon_max=${bounds.max.longitude}"
        try {
            val response = httpClient.get(url) { expectSuccess = true }
            val source = response.bodyAsChannel().asSource().buffered()
            gathered += atpApiParser.parseAtpEntries(source)
        } catch (e: ClientRequestException) {
            throw e
        }
        return gathered
    }

}
