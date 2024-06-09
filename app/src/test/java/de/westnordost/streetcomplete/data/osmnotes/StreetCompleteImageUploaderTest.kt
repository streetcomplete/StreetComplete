package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ConnectionException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StreetCompleteImageUploaderTest {
    private val mockEngine = MockEngine { request -> when (String(request.body.toByteArray())) {
        // Upload requests
        "valid\n" -> respondOk("{\"future_url\": \"market.jpg\"}")
        "invalid\n" -> respondError(HttpStatusCode.InternalServerError)
        "" -> respondBadRequest()
        "ioexception\n" -> throw IOException("Unable to connect")

        // Activate requests
        "{\"osm_note_id\": 180}" -> respondOk()
        "{\"osm_note_id\": 190}" -> respondError(HttpStatusCode.InternalServerError)
        "{\"osm_note_id\": 200}" -> respondBadRequest()
        "{\"osm_note_id\": 210}" -> throw IOException("Unable to connect")

        else -> throw Exception("Invalid request body")
    } }
    private val uploader = StreetCompleteImageUploader(HttpClient(mockEngine), "http://example.com/" )

    @Test
    fun `upload makes POST request with file contents`() = runBlocking {
        uploader.upload(listOf("src/test/resources/image_uploader/valid.jpg"))

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals(HttpMethod.Post, mockEngine.requestHistory[0].method)
        assertEquals("http://example.com/upload.php", mockEngine.requestHistory[0].url.toString())
        assertEquals(ContentType.Image.JPEG, mockEngine.requestHistory[0].body.contentType)
        assertEquals("binary", mockEngine.requestHistory[0].headers["Content-Transfer-Encoding"])
    }

    @Test
    fun `upload returns future_url value from response`() = runBlocking {
        val uploads = uploader.upload(listOf("src/test/resources/image_uploader/valid.jpg"))

        assertContentEquals(listOf("market.jpg"), uploads)
    }

    @Test
    fun `upload throws ImageUploadServerException on 500 error`() = runBlocking {
        val exception = assertFailsWith(ImageUploadServerException::class) {
            uploader.upload(listOf("src/test/resources/image_uploader/invalid.jpg"))
        }

        assertEquals("Upload failed: Error code 500 Internal Server Error, Message: \"Internal Server Error\"", exception.message)
    }

    @Test
    fun `upload throws ImageUploadClientException on 400 error`() = runBlocking {
        val exception = assertFailsWith(ImageUploadClientException::class) {
            uploader.upload(listOf("src/test/resources/image_uploader/empty.jpg"))
        }

        assertEquals("Upload failed: Error code 400 Bad Request, Message: \"Bad Request\"", exception.message)
    }

    @Test
    fun `upload performs no requests with missing file`() = runBlocking {
        assertContentEquals(listOf(), uploader.upload(listOf("no-such-file-at-this-path.jpg")))
        assertEquals(0, mockEngine.requestHistory.size)
    }

    @Test
    fun `upload throws ConnectionException on IOException`(): Unit = runBlocking {
        assertFailsWith(ConnectionException::class) {
            uploader.upload(listOf("src/test/resources/image_uploader/ioexception.jpg"))
        }
    }

    @Test
    fun `activate makes POST request with note ID`() = runBlocking {
        uploader.activate(180)

        assertEquals(1, mockEngine.requestHistory.size)
        assertEquals("http://example.com/activate.php", mockEngine.requestHistory[0].url.toString())
        assertEquals(ContentType.Application.Json, mockEngine.requestHistory[0].body.contentType)
        assertEquals("{\"osm_note_id\": 180}", String(mockEngine.requestHistory[0].body.toByteArray()))
    }

    @Test
    fun `activate throws ImageUploadServerException on 500 error`() = runBlocking {
        val exception = assertFailsWith(ImageUploadServerException::class) {
            uploader.activate(190)
        }

        assertEquals("Error code 500 Internal Server Error, Message: \"Internal Server Error\"", exception.message)
    }

    @Test
    fun `activate throws ImageUploadClientException on 400 error`() = runBlocking {
        val exception = assertFailsWith(ImageUploadClientException::class) {
            uploader.activate(200)
        }

        assertEquals("Error code 400 Bad Request, Message: \"Bad Request\"", exception.message)
    }

    @Test
    fun `activate throws ConnectionException on IOException`(): Unit = runBlocking {
        assertFailsWith(ConnectionException::class) {
            uploader.activate(210)
        }
    }
}
