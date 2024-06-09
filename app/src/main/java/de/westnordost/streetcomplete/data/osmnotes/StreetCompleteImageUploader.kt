package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ConnectionException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.defaultForFile
import io.ktor.http.isSuccess
import io.ktor.util.cio.readChannel
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class PhotoUploadResponse(
    @SerialName("future_url")
    val futureUrl: String
)

/** Upload and activate a list of image paths to an instance of the
 * <a href="https://github.com/streetcomplete/sc-photo-service">StreetComplete image hosting service</a>
 */
class StreetCompleteImageUploader(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    /** Upload list of images.
     *
     *  @throws ImageUploadServerException when there was a server error on upload (server error)
     *  @throws ImageUploadClientException when the server rejected the upload request (client error)
     *  @throws ConnectionException if it is currently not reachable (no internet etc) */
    suspend fun upload(imagePaths: List<String>): List<String> {
        val imageLinks = ArrayList<String>()

        for (path in imagePaths) {
            val file = File(path)
            if (!file.exists()) continue

            try {
                val response = httpClient.post(baseUrl + "upload.php") {
                    contentType(ContentType.defaultForFile(file))
                    header("Content-Transfer-Encoding", "binary")
                    setBody(file.readChannel())
                }

                val status = response.status
                val body = response.body<String>()
                if (status.isSuccess()) {
                    try {
                        val parsedResponse = json.decodeFromString<PhotoUploadResponse>(body)
                        imageLinks.add(parsedResponse.futureUrl)
                    } catch (e: SerializationException) {
                        throw ImageUploadServerException("Upload Failed: Unexpected response \"$body\"")
                    }
                } else {
                    if (status.value in 500..599) {
                        throw ImageUploadServerException("Upload failed: Error code $status, Message: \"$body\"")
                    } else {
                        throw ImageUploadClientException("Upload failed: Error code $status, Message: \"$body\"")
                    }
                }
            } catch (e: IOException) {
                throw ConnectionException("Upload failed", e)
            }
        }

        return imageLinks
    }

    /** Activate the images in the given note.
     *  @throws ImageUploadServerException when there was a server error on upload (server error)
     *  @throws ImageUploadClientException when the server rejected the upload request (client error)
     *  @throws ConnectionException if it is currently not reachable (no internet etc)  */
    suspend fun activate(noteId: Long) {
        try {
            val response = httpClient.post(baseUrl + "activate.php") {
                contentType(ContentType.Application.Json)
                setBody("{\"osm_note_id\": $noteId}")
            }

            val status = response.status
            if (status == HttpStatusCode.Gone) {
                // it's gone if the note does not exist anymore. That's okay, it should only fail
                // if we might want to try again later.
            } else if (!status.isSuccess()) {
                val error = response.body<String>()
                if (status.value in 500..599) {
                    throw ImageUploadServerException("Error code $status, Message: \"$error\"")
                } else {
                    throw ImageUploadClientException("Error code $status, Message: \"$error\"")
                }
            }
        } catch (e: IOException) {
            throw ConnectionException("", e)
        }
    }
}

class ImageUploadServerException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

class ImageUploadClientException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)
