import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/** Update the metadata that contain the store descriptions for the app (for F-Droid) */
open class UpdateStoreDescriptionsTask : AUpdateFromPOEditorTask() {

    @get:Input var targetDir: String? = null

    @TaskAction fun run() {
        val targetDir = targetDir ?: return

        val languageCodes = fetchLocalizations { it["code"] as String }

        for (languageCode in languageCodes) {
            if (languageCode.toLowerCase(Locale.US) == "en-us") continue
            println(languageCode)
            fetchLocalization(languageCode, "key_value_json") { inputStream ->
                val translations = Parser.default().parse(inputStream) as JsonObject

                File("$targetDir/$languageCode").mkdirs()
                translations.string("store_listing_short_description")?.let {
                    File("$targetDir/$languageCode/short_description.txt").writeText(it)
                }
                translations.string("store_listing_full_description")?.let {
                    File("$targetDir/$languageCode/full_description.txt").writeText(it)
                }
            }
        }
    }
}