import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/** Update a resources file that specifies the current translation completeness for every language */
open class UpdateAppTranslationCompletenessTask : AUpdateFromPOEditorTask() {

    @get:Input var targetFiles: ((androidResCode: String) -> String)? = null

    @TaskAction fun run() {
        val targetFiles = targetFiles ?: return

        val localizationStatus = fetchLocalizations {
            LocalizationStatus(it["code"] as String, it["percentage"] as Int)
        }
        for (status in localizationStatus) {
            val languageCode = status.languageCode
            val completedPercentage = status.completedPercentage

            val javaLanguageTag = bcp47LanguageTagToJavaLanguageTag(languageCode)
            val androidResCodes = javaLanguageTagToAndroidResCodes(javaLanguageTag)

            // create a metadata file that describes how complete the translation is
            for (androidResCode in androidResCodes) {
                // exclude default translation
                if (androidResCode == "en-rUS") continue
                val targetFile = File(targetFiles(androidResCode))
                File(targetFile.parent).mkdirs()
                targetFile.writeText("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                      <integer name="translation_completeness">${completedPercentage}</integer>
                    </resources>
                    
                    """.trimIndent())
            }
        }
    }
}

private data class LocalizationStatus(val languageCode: String, val completedPercentage: Int)