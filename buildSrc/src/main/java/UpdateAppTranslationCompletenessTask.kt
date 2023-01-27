import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update a resources file that specifies the current translation completeness for every language */
open class UpdateAppTranslationCompletenessTask : DefaultTask() {

    @get:Input var projectId: String? = null
    @get:Input var apiToken: String? = null
    @get:Input var targetFiles: ((androidResCode: String) -> String)? = null

    @TaskAction fun run() {
        val targetFiles = targetFiles ?: return
        val apiToken = apiToken ?: return
        val projectId = projectId ?: return

        val localizations = fetchAvailableLocalizations(apiToken, projectId)
        for (status in localizations) {
            val locale = Locale.forLanguageTag(status.code).transformPOEditorLanguageTag()
            val completedPercentage = status.percentage

            val androidResCodes = locale.toAndroidResCodes()

            // create a metadata file that describes how complete the translation is
            for (androidResCode in androidResCodes) {
                // exclude default translation
                if (androidResCode == "en-rUS") continue
                val targetFile = File(targetFiles(androidResCode))
                File(targetFile.parent).mkdirs()
                targetFile.writeText("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                      <integer name="translation_completeness">$completedPercentage</integer>
                    </resources>

                """.trimIndent())
            }
        }
    }
}
