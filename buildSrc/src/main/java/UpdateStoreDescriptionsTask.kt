import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update the metadata that contain the store descriptions for the app (for F-Droid) */
open class UpdateStoreDescriptionsTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:OutputDirectory lateinit var targetDir: File

    @TaskAction fun run() {
        require(apiToken != "invalid") { "POEditor API token must be set" }

        val languageTags = fetchAvailableLocalizations(apiToken, projectId).map { it.code }

        for (languageTag in languageTags) {
            val locale = Locale.forLanguageTag(transformPOEditorLanguageTag(languageTag))

            // en-us is the source language
            if (locale == Locale.US) continue

            val actualLanguageTag = locale.toLanguageTag()

            print(languageTag)
            if (languageTag != actualLanguageTag) print(" -> " + actualLanguageTag)
            println()

            val translations = fetchLocalizationJson(apiToken, projectId, languageTag)

            val dir = targetDir.resolve(languageTag)
            dir.mkdirs()
            translations["store_listing_short_description"]?.let {
                dir.resolve("short_description.txt").writeText(it)
            }
            translations["store_listing_full_description"]?.let {
                dir.resolve("full_description.txt").writeText(it)
            }
        }
    }
}
