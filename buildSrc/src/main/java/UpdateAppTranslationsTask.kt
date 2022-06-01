
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update the Android string resources (translations) for all the given language codes */
open class UpdateAppTranslationsTask : AUpdateFromPOEditorTask() {

    @get:Input var languageCodes: Collection<String>? = null
    @get:Input var targetFiles: ((androidResCode: String) -> String)? = null

    @TaskAction fun run() {
        val targetFiles = targetFiles ?: return
        val exportLanguages = languageCodes?.map { Locale.forLanguageTag(it) }

        val languageTags = fetchLocalizations { it.string("code")!! }
        for (languageTag in languageTags) {
            val locale = Locale.forLanguageTag(languageTag)

            if (exportLanguages != null && !exportLanguages.any { it == locale }) continue
            // en-us is the source language
            if (locale == Locale.US) continue

            val androidResCodes = locale.transformPOEditorLanguageTag().toAndroidResCodes()

            print(languageTag)
            if (androidResCodes.singleOrNull() != languageTag) print(" -> " + androidResCodes.joinToString(", "))
            println()

            // download the translation and save it in the appropriate directory
            val text = fetchLocalization(languageTag, "android_strings") { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            }
            for (androidResCode in androidResCodes) {
                File(targetFiles(androidResCode)).writeText(text)
            }
        }
    }
}
