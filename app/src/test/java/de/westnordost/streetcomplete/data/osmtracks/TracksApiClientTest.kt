package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import java.time.Instant
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFailsWith

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagined server
// response
class TracksApiClientTest {

    private val trackpoint = Trackpoint(LatLon(1.23, 3.45), Instant.now().toEpochMilli(), 1f, 1f)

    private val allowEverything = mock<UserLoginSource>()
    private val allowNothing = mock<UserLoginSource>()
    private val anonymous = mock<UserLoginSource>()

    init {
        on(allowEverything.accessToken).thenReturn(OsmDevApi.ALLOW_EVERYTHING_TOKEN)
        on(allowNothing.accessToken).thenReturn(OsmDevApi.ALLOW_NOTHING_TOKEN)
        on(anonymous.accessToken).thenReturn(null)
    }

    @Test fun `throws exception on insufficient privileges`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> { client(anonymous).create(listOf(trackpoint)) }
        assertFailsWith<AuthorizationException> { client(allowNothing).create(listOf(trackpoint)) }
    }

    // disabled this test, because I get an email about the successfull GPX upload every time this
    // test is executed
    @Ignore
    @Test fun `create works without error`(): Unit = runBlocking {
        client(allowEverything).create(listOf(trackpoint))
    }

    private fun client(userLoginSource: UserLoginSource) =
        TracksApiClient(HttpClient(), OsmDevApi.URL, userLoginSource, TracksSerializer())
}

