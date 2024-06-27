package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.user.UserApiClient
import de.westnordost.streetcomplete.data.user.UserInfo
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class AvatarsDownloaderTest {
    private val mockEngine = MockEngine { request ->
        when (request.url.encodedPath) {
            "/NotFound" -> respondError(HttpStatusCode.NotFound)
            "/ConnectionError" -> throw IOException("Cannot connect")
            else -> respondOk("Image Content")
        }
    }
    private val tempFolder = Files.createTempDirectory("images").toFile()
    private val userApi: UserApiClient = mock()
    private val downloader = AvatarsDownloader(HttpClient(mockEngine), userApi, tempFolder)

    @Test
    fun `download makes GET request to profileImageUrl`() = runBlocking {
        val user = user()
        on(userApi.get(user.id)).thenReturn(user)

        downloader.download(listOf(user.id))

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals(user.profileImageUrl, mockEngine.requestHistory[0].url.toString())
        assertEquals(HttpMethod.Get, mockEngine.requestHistory[0].method)
    }

    @Test
    fun `download copies HTTP response from profileImageUrl into tempFolder`() = runBlocking {
        val user = user()
        on(userApi.get(user.id)).thenReturn(user)

        downloader.download(listOf(user.id))

        assertEquals("Image Content", tempFolder.resolve("100").readText())
    }

    @Test
    fun `download does not throw exception on HTTP NotFound`() = runBlocking {
        val user = user(profileImageUrl = "http://example.com/NotFound")
        on(userApi.get(user.id)).thenReturn(user)

        downloader.download(listOf(user.id))

        assertEquals(404, mockEngine.responseHistory[0].statusCode.value)
    }

    @Test
    fun `download does not throw exception on networking error`() = runBlocking {
        val user = user(profileImageUrl = "http://example.com/ConnectionError")
        on(userApi.get(user.id)).thenReturn(user)

        downloader.download(listOf(user.id))

        assertEquals(0, mockEngine.responseHistory.size)
    }

    @Test
    fun `download does not make HTTP request if profileImageUrl is NULL`() = runBlocking {
        val user = user(profileImageUrl = null)
        on(userApi.get(user.id)).thenReturn(user)

        downloader.download(listOf(user.id))

        assertEquals(0, mockEngine.requestHistory.size)
    }

    private fun user(profileImageUrl: String? = "http://example.com/BigImage.png") = UserInfo(
        id = 100,
        displayName = "Map Enthusiast 530",
        profileImageUrl = profileImageUrl,
    )
}
