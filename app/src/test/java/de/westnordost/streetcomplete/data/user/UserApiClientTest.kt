package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.testutils.OsmDevApi
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
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
    private val allowEverything = mock<UserLoginSource>()
    private val allowNothing = mock<UserLoginSource>()
    private val anonymous = mock<UserLoginSource>()

    init {
        on(allowEverything.accessToken).thenReturn(OsmDevApi.ALLOW_EVERYTHING_TOKEN)
        on(allowNothing.accessToken).thenReturn(OsmDevApi.ALLOW_NOTHING_TOKEN)
        on(anonymous.accessToken).thenReturn(null)
    }

    @Test
    fun get(): Unit = runBlocking {
        val info = client(anonymous).get(3625)

        assertNotNull(info)
        assertEquals(3625, info.id)
        assertEquals("westnordost", info.displayName)
        assertNotNull(info.profileImageUrl)
    }

    @Test
    fun getMine(): Unit = runBlocking {
        val info = client(allowEverything).getMine()

        assertNotNull(info)
        assertEquals(3625, info.id)
        assertEquals("westnordost", info.displayName)
        assertNotNull(info.profileImageUrl)
    }

    @Test
    fun `getMine fails when not logged in`(): Unit = runBlocking {
        assertFailsWith<AuthorizationException> { client(anonymous).getMine() }
    }

    private fun client(userLoginSource: UserLoginSource) =
        UserApiClient(HttpClient(), OsmDevApi.URL, userLoginSource, UserApiParser())
}
