import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Export the strings used for iOS to the xcstrings file */
open class UpdateIosAppTranslationsTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:Input lateinit var strings: Map<String, String>
    @get:Input lateinit var languageCodes: Collection<String>
    @get:OutputFile lateinit var targetFile: File

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    @TaskAction fun run() {
        val exportLanguages = languageCodes.map { Locale.forLanguageTag(it) }

        val languageTags = fetchAvailableLocalizations(apiToken, projectId).map { it.code }

        // language code -> (string id -> translation string)
        val translations = HashMap<String, Map<String, String>>()

        for (languageTag in languageTags) {
            val locale = Locale.forLanguageTag(languageTag)

            if (!exportLanguages.any { it == locale }) continue

            val actualLanguageTag = locale.transformPOEditorLanguageTag().toLanguageTag()

            print(languageTag)
            if (actualLanguageTag != languageTag) print(" -> " + actualLanguageTag)
            println()

            translations[actualLanguageTag] = fetchLocalizationJson(apiToken, projectId, languageTag)
        }

        // iosKey: e.g. "NSLocationWhenInUseUsageDescription"
        // stringId: e.g. "no_location_permission_warning"
        // languageCode: e.g. "zh-Hant"
        // strings: e.g. mapOf("no_location_permission_warning" to "To show your position on the map and download data your vicinity.", …)
        val strings = XcStrings(
            strings = strings.mapValues { (iosKey, stringId) ->
                XcStringEntry(
                    localizations = translations.mapValuesNotNull { (languageCode, strings) ->
                        val string = strings[stringId]
                        if (string != null) XcLocalization(stringUnit = XcStringUnit(value = string))
                        else null
                    }
                )
            }
        )

        targetFile.writeText(json.encodeToString(strings))
    }
}

@Serializable
data class XcStrings(
    val sourceLanguage: String = "en",
    val strings: Map<String, XcStringEntry>,
    val version: String = "1.2",
)

@Serializable
data class XcStringEntry(
    val localizations: Map<String, XcLocalization>,
)

@Serializable
data class XcLocalization(
    val stringUnit: XcStringUnit,
)

@Serializable
data class XcStringUnit(
    val state: String = "translated",
    val value: String,
)

@Suppress("UNCHECKED_CAST")
private fun <K, V, R> Map<K, V>.mapValuesNotNull(transform: (Map.Entry<K, V>) -> R?): Map<K, R> =
    mapValues(transform).filterValues { it != null } as Map<K, R>
