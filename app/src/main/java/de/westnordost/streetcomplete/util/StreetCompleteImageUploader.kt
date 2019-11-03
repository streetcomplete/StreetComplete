package de.westnordost.streetcomplete.util

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

import de.westnordost.streetcomplete.ApplicationConstants

/** Uploads a list of image paths to an instance of https://github.com/exploide/sc-photo-service */
class StreetCompleteImageUploader(private val baseUrl: String) {

    private val TAG = "ImageUploader"

    fun upload(imagePaths: List<String>?): List<String> {
        val imageLinks = ArrayList<String>()

        for (path in imagePaths.orEmpty()) {
            val file = File(path)
            if (!file.exists()) continue 
        
            try {
                val httpConnection = createConnection("upload.php")
                httpConnection.requestMethod = "POST"
                httpConnection.setRequestProperty("Content-Type", URLConnection.guessContentTypeFromName(file.path))
                httpConnection.setRequestProperty("Content-Transfer-Encoding", "binary")
                httpConnection.setRequestProperty("Content-Length", file.length().toString())

                httpConnection.outputStream.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

                val status = httpConnection.responseCode
                if (status == HttpURLConnection.HTTP_OK) {
                    val response = httpConnection.inputStream.bufferedReader().use { it.readText() }
                    try {
                        val jsonResponse = JSONObject(response)
                        val url = jsonResponse.getString("future_url")
                        imageLinks.add(url)
                    } catch (e: JSONException) {
                        imageLinks.add("(error: upload failed)")
                        Log.e(TAG, "Upload Failed: Unexpected response \"$response\"", e)
                    }
                } else {
                    imageLinks.add("(error: upload failed)")
                    val error = httpConnection.errorStream.bufferedReader().use { it.readText() }
                    Log.e(TAG, "Upload failed: Error code $status, Message: \"$error\"")
                }

                httpConnection.disconnect()
            } catch (e: IOException) {
                imageLinks.add("(error: upload failed)")
                Log.e(TAG, "Upload failed", e)
            }
            // an error here should neither crash the app nor make the whole note upload
            // fail but should not be silently ignored -> middle ground: include error message
            // in note (comment) and log to console
        }

        return imageLinks
    }

    fun activate(noteId: Long) {
        try {
            val connection = createConnection("activate.php")
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "Content-Type: application/json")
            connection.outputStream.bufferedWriter().use { it.write("{\"osm_note_id\": $noteId}") }

            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Activation successful: \"$response\"")
            } else {
                val error = connection.errorStream.bufferedReader().use { it.readText() }
                Log.e(TAG, "Activation failed: Error code $status, Message: \"$error\"")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Activation failed", e)
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

