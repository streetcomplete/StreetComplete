import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/** Base task class to pull translations and other info from POEditor crowd translation platform */
abstract class AUpdateFromPOEditorTask : DefaultTask() {

    @get:Input var apiToken: String? = null

    private val baseParams get() = "api_token=$apiToken&id=97843"

    /** Fetch the localization for the given language code in the given format and do something
     *  with the contents */
    protected fun <T> fetchLocalization(languageCode: String, format: String, block: (InputStream) -> T): T {
        val url = URL(fetchLocalizationDownloadUrl(languageCode, format))
        return url.retryingQuotaConnection(null, block)
    }

    /** Fetch the download URL for the given language code. Handle quota. */
    private fun fetchLocalizationDownloadUrl(languageCode: String, format: String): String {
        return URL("https://api.poeditor.com/v2/projects/export").retryingQuotaConnection({ connection ->
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.outputStream.bufferedWriter().use { it.write(
                "$baseParams&language=${languageCode.toLowerCase(Locale.US)}&type=$format&filters=translated"
            ) }
        }) { inputStream ->
            val response = Parser.default().parse(inputStream) as JsonObject
            (response.obj("result")!!)["url"] as String
        }
    }

    /** Fetch language codes of available translations from POEditor API */
    protected fun <T> fetchLocalizations(mapping: (JsonObject) -> T): List<T> {
        return URL("https://api.poeditor.com/v2/languages/list").retryingQuotaConnection({ connection ->
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.outputStream.bufferedWriter().use { it.write(baseParams) }
        }) { inputStream ->
            val response = Parser.default().parse(inputStream) as JsonObject
            response.obj("result")!!.array<JsonObject>("languages")!!.map {
                mapping(it)
            }
        }
    }

    // this is for waiting and retrying for quota to replenish when querying POEditor API... :-|
    private fun <T> URL.retryingQuotaConnection(setup: ((HttpURLConnection) -> Unit)? = null, block: (InputStream) -> T): T {
        val maxWait = 12
        var i = 0
        while(i++ < maxWait) {
            val connection = openConnection() as HttpURLConnection
            setup?.invoke(connection)
            if (connection.responseCode == 429) {
                connection.disconnect()
                Thread.sleep(5000)
            } else {
                val result = block(connection.inputStream)
                connection.disconnect()
                return result
            }
        }
        throw Exception("POEditor API continues to report http status code 429")
    }
}