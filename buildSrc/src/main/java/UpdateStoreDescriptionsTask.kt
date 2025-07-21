import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/** Update the metadata that contain the store descriptions for the app (for F-Droid) */
open class UpdateStoreDescriptionsTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:Input lateinit var targetDir: String

    @TaskAction fun run() {
        val languageCodes = fetchAvailableLocalizations(apiToken, projectId).map { it.code }

        for (languageCode in languageCodes) {
            if (languageCode.lowercase() == "en-us") continue
            println(languageCode)
            val translations = fetchLocalizationJson(apiToken, projectId, languageCode)

            File("$targetDir/$languageCode").mkdirs()
            translations["store_listing_short_description"]?.let {
                File("$targetDir/$languageCode/short_description.txt").writeText(it)
            }
            translations["store_listing_full_description"]?.let {
                File("$targetDir/$languageCode/full_description.txt").writeText(it)
            }
        }
    }
}
