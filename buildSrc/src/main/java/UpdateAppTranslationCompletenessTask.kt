import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update a resources file that specifies the current translation completeness for every language
 *  and checks if a language should be included that isn't yet */
open class UpdateAppTranslationCompletenessTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:Input lateinit var languageCodes: Collection<String>
    @get:Input var mustIncludeLanguagePercentage: Int = 80
    @get:Input lateinit var targetFiles: ((androidResCode: String) -> String)

    @TaskAction fun run() {
        val targetFiles = targetFiles
        val exportLanguages = languageCodes.map { Locale.forLanguageTag(it) }

        val localizations = fetchAvailableLocalizations(apiToken, projectId)
        for (status in localizations) {
            val locale = Locale.forLanguageTag(status.code)
            val completedPercentage = status.percentage

            if (!exportLanguages.any { it == locale } && locale != Locale.US) {
                if (completedPercentage >= mustIncludeLanguagePercentage) {
                    throw Exception(
                        "App has been translated $completedPercentage% to " +
                        "${locale.displayLanguage} (${locale.language}) " +
                        " but the language is not included in the app."
                    )
                }
            }

            val androidResCodes = locale.transformPOEditorLanguageTag().toAndroidResCodes()

            // create a metadata file that describes how complete the translation is
            for (androidResCode in androidResCodes) {
                // exclude default translation
                if (androidResCode == "en-rUS") continue
                val targetFile = File(targetFiles(androidResCode))
                File(targetFile.parent).mkdirs()
                targetFile.writeText("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                      <string name="translation_completeness">$completedPercentage</string>
                    </resources>

                """.trimIndent())
            }
        }
    }
}
