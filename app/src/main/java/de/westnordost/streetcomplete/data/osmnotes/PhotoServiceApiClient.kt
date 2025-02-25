package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.defaultForFile
import io.ktor.util.cio.readChannel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** Upload and activate a list of image paths to an instance of the
 *  https://github.com/streetcomplete/sc-photo-service
 */
class PhotoServiceApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Upload list of images.
     *
     *  @throws ConnectionException on connection or server error */
    suspend fun upload(imagePaths: List<String>): List<String> = wrapApiClientExceptions {
        val imageLinks = ArrayList<String>()

        for (path in imagePaths) {
            val file = File(path)
            if (!file.exists()) continue

            val response = httpClient.post(baseUrl + "upload.php") {
                contentType(ContentType.defaultForFile(file))
                header("Content-Transfer-Encoding", "binary")
                setBody(file.readChannel())
                expectSuccess = true
            }

            val body = response.body<String>()
            val parsedResponse = json.decodeFromString<PhotoUploadResponse>(body)
            imageLinks.add(parsedResponse.futureUrl)
        }

        return imageLinks
    }

    /** Activate the images in the given note.
     *
     *  @throws ConnectionException on connection or server error */
    suspend fun activate(noteId: Long): Unit = wrapApiClientExceptions {
        try {
            httpClient.post(baseUrl + "activate.php") {
                contentType(ContentType.Application.Json)
                setBody("{\"osm_note_id\": $noteId}")
                expectSuccess = true
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Gone) {
                // it's gone if the note does not exist anymore. That's okay, it should only fail
                // if we might want to try again later.
            } else {
                throw e
            }
        }
    }
}

@Serializable
private data class PhotoUploadResponse(
    @SerialName("future_url")
    val futureUrl: String
)
