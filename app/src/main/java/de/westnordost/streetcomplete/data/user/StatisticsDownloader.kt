package de.westnordost.streetcomplete.data.user

import com.esotericsoftware.yamlbeans.YamlReader
import de.westnordost.streetcomplete.ApplicationConstants
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class StatisticsDownloader(private val baseUrl: String) {

    fun download(osmUserId: Long): Statistics? {
        val connection = createConnection("download")
        connection.requestMethod = "GET"
        connection.doInput = true
        connection.doOutput = true
        connection.outputStream.writeText("id=$osmUserId")
        return when (connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                YamlReader(connection.inputStream.bufferedReader()).read(Statistics::class.java)
            }
            HttpURLConnection.HTTP_ACCEPTED -> {
                null
            }
            else -> {
                val errorMessage = connection.responseMessage
                val errorDescription = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw IOException("${connection.responseCode} $errorMessage: $errorDescription")
            }
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

private fun OutputStream.writeText(text: String) {
    bufferedWriter().use { writer ->
        writer.write(text)
        writer.close()
    }
}
