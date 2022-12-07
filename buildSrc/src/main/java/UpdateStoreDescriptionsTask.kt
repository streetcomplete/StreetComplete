import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update the metadata that contain the store descriptions for the app (for F-Droid) */
open class UpdateStoreDescriptionsTask : DefaultTask() {

    @get:Input var projectId: String? = null
    @get:Input var apiToken: String? = null
    @get:Input var targetDir: String? = null

    @TaskAction fun run() {
        val targetDir = targetDir ?: return
        val apiToken = apiToken ?: return
        val projectId = projectId ?: return

        val languageCodes = fetchAvailableLocalizations(apiToken, projectId).map { it.code }

        for (languageCode in languageCodes) {
            if (languageCode.toLowerCase(Locale.US) == "en-us") continue
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
