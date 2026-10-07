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
            val lang = if (languageTag.lowercase() == "en-us") "en" else languageTag
            val locale = Locale.forLanguageTag(lang)

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
        val stringsByKey = strings.mapValues { (iosKey, stringId) ->
            XcStringEntry(
                localizations = translations.mapValuesNotNull { (languageCode, strings) ->
                    val string = strings[stringId]
                    if (string != null) XcLocalization(stringUnit = XcStringUnit(value = string))
                    else null
                }.toSortedMap()
            )
        }.toSortedMap()

        // XCode always adds a default translation for the bundle name. So, let's already add it
        // in the task, so that XCode doesn't overwrite it.
        stringsByKey["CFBundleName"] = XcStringEntry(
            localizations = mapOf("en" to XcLocalization(stringUnit = XcStringUnit(value = "StreetComplete")))
        )

        val xcStrings = XcStrings(strings = stringsByKey)
        val xcStringsJson = json.encodeToString(xcStrings)

        // XCode parses and then overwrites the xcstrings file. It uses a particular syntax:
        // It sorts all string keys in `strings` alphabetically and then also all language tags in
        // `localizations` alphabetically.
        // Furthermore, it puts a space before each colon in associative arrays, e.g.
        // `"sourceLanguage" : "en",`
        // so, we adapt to this syntax so that XCode doesn't create actual changes that'd be
        // commited to the repository.

        val findColons = Regex("^(\\s*\".+\"): ", RegexOption.MULTILINE)
        targetFile.writeText(xcStringsJson.replace(findColons) { matchResult ->
            matchResult.groupValues[1] + " : "
        })
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
    val extractionState: String = "manual",
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
