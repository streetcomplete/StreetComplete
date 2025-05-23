package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.testutils.OsmDevApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagined server
// response
class ChangesetApiClientTest {

    @Test fun `open throws exception on insufficient privileges`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> {
            client(null).open(mapOf())
        }
        assertFailsWith<AuthorizationException> {
            client(OsmDevApi.ALLOW_NOTHING_TOKEN).open(mapOf())
        }
    }

    @Test fun `open and close works without error`(): Unit = runBlocking {
        val client = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN)

        val id = client.open(mapOf("testKey" to "testValue"))
        client.close(id)

        assertFailsWith<ConflictException> { client.close(id) }
    }

    @Test fun `close throws exception on insufficient privileges`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> { client(null).close(1) }
        assertFailsWith<AuthorizationException> { client(OsmDevApi.ALLOW_NOTHING_TOKEN).close(1) }
    }

    private fun client(token: String?) =
        ChangesetApiClient(
            httpClient = HttpClient(),
            baseUrl = OsmDevApi.URL,
            userAccessTokenSource = object : UserAccessTokenSource { override val accessToken = token.orEmpty() },
            serializer = ChangesetApiSerializer()
        )
}
