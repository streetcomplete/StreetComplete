package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagines server
// response
class TracksApiClientIntegrationTest {

    private val trackpoint = Trackpoint(LatLon(1.23, 3.45), Instant.now().toEpochMilli(), 1f, 1f)

    @Test fun `throws exception on insufficient privileges`(): Unit = runBlocking {
        val userLoginSource = mock<UserLoginSource>()
        on(userLoginSource.accessToken)
            .thenReturn(null)
            .thenReturn(OsmDevApi.ALLOW_NOTHING_TOKEN)
            .thenReturn("unknown")

        val api = TracksApiClient(HttpClient(CIO), OsmDevApi.URL, userLoginSource)

        assertFailsWith<AuthorizationException> { api.create(listOf(trackpoint)) }
        assertFailsWith<AuthorizationException> { api.create(listOf(trackpoint)) }
        assertFailsWith<AuthorizationException> { api.create(listOf(trackpoint)) }
    }

    @Test fun `create works without error`(): Unit = runBlocking {
        val userLoginSource = mock<UserLoginSource>()
        on(userLoginSource.accessToken).thenReturn(OsmDevApi.ALLOW_EVERYTHING_TOKEN)

        val api = TracksApiClient(HttpClient(CIO), OsmDevApi.URL, userLoginSource)

        api.create(listOf(trackpoint))
    }
}

