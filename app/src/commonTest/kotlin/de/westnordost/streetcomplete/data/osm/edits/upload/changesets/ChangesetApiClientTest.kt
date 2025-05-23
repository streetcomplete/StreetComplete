package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagined server
// response
class ChangesetApiClientTest {
    private val allowEverything = mock<UserLoginSource>()
    private val allowNothing = mock<UserLoginSource>()
    private val anonymous = mock<UserLoginSource>()

    init {
        on(allowEverything.accessToken).thenReturn(OsmDevApi.ALLOW_EVERYTHING_TOKEN)
        on(allowNothing.accessToken).thenReturn(OsmDevApi.ALLOW_NOTHING_TOKEN)
        on(anonymous.accessToken).thenReturn(null)
    }

    @Test fun `open throws exception on insufficient privileges`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> { client(anonymous).open(mapOf()) }
        assertFailsWith<AuthorizationException> { client(allowNothing).open(mapOf()) }
    }

    @Test fun `open and close works without error`(): Unit = runBlocking {
        val id = client(allowEverything).open(mapOf("testKey" to "testValue"))
        client(allowEverything).close(id)
        assertFailsWith<ConflictException> { client(allowEverything).close(id) }
    }

    @Test fun `close throws exception on insufficient privileges`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> { client(anonymous).close(1) }
        assertFailsWith<AuthorizationException> { client(allowNothing).close(1) }
    }

    private fun client(userLoginSource: UserLoginSource) =
        ChangesetApiClient(HttpClient(), OsmDevApi.URL, userLoginSource, ChangesetApiSerializer())
}
