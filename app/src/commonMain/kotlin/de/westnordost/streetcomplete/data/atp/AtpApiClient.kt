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
     *
     * @return the incoming atp entries
     */
    suspend fun getAllAtpEntries(bounds: BoundingBox): List<AtpEntry> = wrapApiClientExceptions {
        val gathered = mutableListOf<AtpEntry>()

        // TODO add tests
        for (longitudeAnchor in floor(bounds.min.longitude).toInt()..floor(bounds.min.longitude).toInt()) {
            for (latitudeAnchor in floor(bounds.min.latitude).toInt()..floor(bounds.min.latitude).toInt()) {
                val url = baseUrl + "lat_${latitudeAnchor}/lon_${longitudeAnchor}_gathered.geojson"
                try {
                    val response = httpClient.get(url) { expectSuccess = true }
                    val source = response.bodyAsChannel().asSource().buffered()
                    gathered += atpApiParser.parseAtpEntries(source)
                } catch (e: ClientRequestException) {
                    throw e
                }
            }
        }
        return gathered
    }

}
