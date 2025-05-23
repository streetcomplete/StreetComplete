package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagined server
// response
class TracksApiClientTest {

    private val trackpoint = Trackpoint(LatLon(1.23, 3.45), systemTimeNow().toEpochMilliseconds(), 1f, 1f)
    private val creator = "StreetComplete test"

    @Test fun `throws exception on insufficient privileges`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> {
            client(null).create(listOf(trackpoint), creator)
        }
        assertFailsWith<AuthorizationException> {
            client(OsmDevApi.ALLOW_NOTHING_TOKEN).create(listOf(trackpoint), creator)
        }
    }

    // disabled this test, because I get an email about the successfull GPX upload every time this
    // test is executed
    @Ignore
    @Test fun `create works without error`(): Unit = runBlocking {
        client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).create(listOf(trackpoint), creator)
    }

    private fun client(token: String?) =
        TracksApiClient(
            httpClient = HttpClient(),
            baseUrl = OsmDevApi.URL,
            userAccessTokenSource = object : UserAccessTokenSource { override val accessToken = token.orEmpty() },
            tracksSerializer = TracksSerializer()
        )
}
