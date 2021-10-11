package de.westnordost.streetcomplete.data.osmnotes

import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.ConnectionException

/** Upload and activate a list of image paths to an instance of the
 * <a href="https://github.com/exploide/sc-photo-service">StreetComplete image hosting service</a>
 */
class StreetCompleteImageUploader(private val baseUrl: String) {

    /** Upload list of images.
     *
     *  @throws ImageUploadServerException when there was a server error on upload (server error)
     *  @throws ImageUploadClientException when the server rejected the upload request (client error)
     *  @throws ConnectionException if it is currently not reachable (no internet etc) */
    fun upload(imagePaths: List<String>): List<String> {
        val imageLinks = ArrayList<String>()

        for (path in imagePaths) {
            val file = File(path)
            if (!file.exists()) continue

            try {
                val connection = createConnection("upload.php")
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", URLConnection.guessContentTypeFromName(file.path))
                connection.setRequestProperty("Content-Transfer-Encoding", "binary")
                connection.setRequestProperty("Content-Length", file.length().toString())
                connection.outputStream.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    try {
                        val jsonResponse = JSONObject(response)
                        val url = jsonResponse.getString("future_url")
                        imageLinks.add(url)
                    } catch (e: JSONException) {
                        throw ImageUploadServerException("Upload Failed: Unexpected response \"$response\"")
                    }
                } else {
                    val error = connection.errorStream.bufferedReader().use { it.readText() }.toInt()
                    if (error / 100 == 5)
                        throw ImageUploadServerException("Upload failed: Error code $status, Message: \"$error\"")
                    else
                        throw ImageUploadClientException("Upload failed: Error code $status, Message: \"$error\"")
                }
                connection.disconnect()
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
    fun activate(noteId: Long) {
        try {
            val connection = createConnection("activate.php")
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "Content-Type: application/json")
            connection.outputStream.bufferedWriter().use { it.write("{\"osm_note_id\": $noteId}") }

            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_GONE) {
                // it's gone if the note does not exist anymore. That's okay, it should only fail
                // if we might want to try again later.
            }
            else if (status != HttpURLConnection.HTTP_OK) {
                val error = connection.errorStream.bufferedReader().use { it.readText() }
                if (status / 100 == 5)
                    throw ImageUploadServerException("Error code $status, Message: \"$error\"")
                else
                    throw ImageUploadClientException("Error code $status, Message: \"$error\"")
            }
            connection.disconnect()
        } catch (e: IOException) {
            throw ConnectionException("", e)
        }
    }

    private fun createConnection(url: String): HttpURLConnection {
        val connection = URL(baseUrl + url).openConnection() as HttpURLConnection
        connection.useCaches = false
        connection.doOutput = true
        connection.doInput = true
        connection.setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT)
        return connection
    }
}

class ImageUploadServerException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)

class ImageUploadClientException(message: String? = null, cause: Throwable? = null)
    : RuntimeException(message, cause)
