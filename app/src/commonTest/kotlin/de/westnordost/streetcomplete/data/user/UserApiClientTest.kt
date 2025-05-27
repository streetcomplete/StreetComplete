package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.testutils.OsmDevApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

// other than some other APIs we are speaking to, we do not control the OSM API, so I think it is
// more effective to test with the official test API instead of mocking some imagined server
// response
class UserApiClientTest {

    @Test
    fun get(): Unit = runBlocking {
        val info = client(null).get(3625)

        assertNotNull(info)
        assertEquals(3625, info.id)
        assertEquals("westnordost", info.displayName)
        assertNotNull(info.profileImageUrl)
    }

    @Test
    fun getMine(): Unit = runBlocking {
        val info = client(OsmDevApi.ALLOW_EVERYTHING_TOKEN).getMine()

        assertNotNull(info)
        assertEquals(3625, info.id)
        assertEquals("westnordost", info.displayName)
        assertNotNull(info.profileImageUrl)
    }

    @Test
    fun `getMine fails when not logged in`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> { client(null).getMine() }
    }

    private fun client(token: String?) =
        UserApiClient(
            httpClient = HttpClient(),
            baseUrl = OsmDevApi.URL,
            userAccessTokenSource = object : UserAccessTokenSource { override val accessToken = token.orEmpty() },
            userApiParser = UserApiParser()
        )
}
