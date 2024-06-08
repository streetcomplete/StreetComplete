package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ConnectionException
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PhotoServiceApiClientTest {

    private val picture = "src/test/resources/hai_phong_street.jpg"

    @Test
    fun `upload makes POST request with file contents and returns response`() = runBlocking {
        val mockEngine = MockEngine { respondOk("{\"future_url\": \"market.jpg\"}") }
        val client = PhotoServiceApiClient(HttpClient(mockEngine), "http://example.com/")

        val response = client.upload(listOf(picture))

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals(HttpMethod.Post, mockEngine.requestHistory[0].method)
        assertEquals("http://example.com/upload.php", mockEngine.requestHistory[0].url.toString())
        assertEquals(ContentType.Image.JPEG, mockEngine.requestHistory[0].body.contentType)
        assertEquals("binary", mockEngine.requestHistory[0].headers["Content-Transfer-Encoding"])

        assertContentEquals(listOf("market.jpg"), response)
    }

    @Test
    fun `upload throws ConnectionException on such errors`(): Unit = runBlocking {
        val pics = listOf("src/test/resources/hai_phong_street.jpg")

        assertFailsWith(ConnectionException::class) {
            client(MockEngine { throw IOException() }).upload(pics)
        }
        assertFailsWith(ConnectionException::class) {
            client(MockEngine { respondError(HttpStatusCode.InternalServerError) }).upload(pics)
        }
        assertFailsWith(ConnectionException::class) {
            client(MockEngine { throw SerializationException() }).upload(pics)
        }
    }

    @Test
    fun `upload performs no requests with missing file`() = runBlocking {
        val mockEngine = MockEngine { respondOk() }
        val client = PhotoServiceApiClient(HttpClient(mockEngine), "http://example.com/")

        assertContentEquals(listOf(), client.upload(listOf("no-such-file-at-this-path.jpg")))
        assertEquals(0, mockEngine.requestHistory.size)
    }

    @Test
    fun `activate makes POST request with note ID`() = runBlocking {
        val mockEngine = MockEngine { respondOk() }
        val client = PhotoServiceApiClient(HttpClient(mockEngine), "http://example.com/")

        client.activate(123)

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals("http://example.com/activate.php", mockEngine.requestHistory[0].url.toString())
        assertEquals(ContentType.Application.Json, mockEngine.requestHistory[0].body.contentType)
        assertEquals("{\"osm_note_id\": 123}", String(mockEngine.requestHistory[0].body.toByteArray()))
    }

    @Test
    fun `activate throws ConnectionException on such errors`(): Unit = runBlocking {
        assertFailsWith(ConnectionException::class) {
            client(MockEngine { respondError(HttpStatusCode.InternalServerError) }).activate(1)
        }
        assertFailsWith(ConnectionException::class) {
            client(MockEngine { throw IOException() }).activate(1)
        }
    }

    @Test
    fun `activate ignores error code 410 (gone)`(): Unit = runBlocking {
        client(MockEngine { respondError(HttpStatusCode.Gone) }).activate(1)
    }

    private fun client(engine: HttpClientEngine) =
        PhotoServiceApiClient(HttpClient(engine), "http://example.com/")
}
