import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.net.URL
import com.beust.klaxon.Parser
import com.beust.klaxon.JsonObject
import java.io.ByteArrayInputStream
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Update the presets metadata and its translations for use with the de.westnordost:osmfeatures library */
open class GenerateTagSuggestions : DefaultTask() {
    @get:Input var targetDir: String? = null
    @get:Input var version: String? = null

    @TaskAction fun run() {
        val targetDir = targetDir ?: return
        val version = version ?: return

        val presetsText = fetchPresets(version)
        val fieldsText = fetchFields(version)
        val fieldsJson = Parser.default().parse(ByteArrayInputStream(fieldsText.toByteArray())) as? JsonObject ?: return
        val presetsJson = Parser.default().parse(ByteArrayInputStream(presetsText.toByteArray())) as? JsonObject ?: return

        val valueSuggestionsByKey = mutableMapOf<String, MutableSet<String>>() // start as set, and the parse it from json as list
        fieldsJson.forEach { (field, v) ->
            if (v !is JsonObject) return@forEach
            val options = v.array<String>("options")
            if (options != null) {
                // simply suggest the options
                val keys = v.array("keys") ?: v.string("key")?.let { listOf(it) } ?: return@forEach
                keys.forEach { valueSuggestionsByKey.getOrPut(it) { mutableSetOf() }.addAll(options) }
            } else {
                val type = v.string("type") ?: return@forEach
                val key = v.string("key") ?: return@forEach
                if (type == "check") {
                    valueSuggestionsByKey[key] = mutableSetOf("yes", "no")
                    return@forEach
                }
                if (type != "combo" && type != "typeCombo" && type != "semiCombo") return@forEach
                // need to assemble the suggestions from presets.json
                // usually this is in subspace of field
                // but sometimes field is in a subspace!
                val valueSet = mutableSetOf<String>()
                presetsJson.filterKeys { it.startsWith("$field/") || it.contains("/$field/") }.forEach inner@{ (_, v2) ->
                    if (v2 !is JsonObject) return@inner
                    val tags = v2.obj("tags") ?: return@inner
                    tags.string(key)?.let { if (it != "*") valueSet.add(it) }
                }
                if (valueSet.isEmpty()) return@forEach
                valueSuggestionsByKey.getOrPut(key) { mutableSetOf() }.addAll(valueSet)
            }
        }

        File(targetDir).mkdirs()
        File("$targetDir/valueSuggestionsByKey.json").writeText(Json.encodeToString(valueSuggestionsByKey))

        val keySuggestionsForFeature = mutableMapOf<String, Pair<Set<String>?, Set<String>?>>()
        presetsJson.forEach { (k, v) ->
            if (v !is JsonObject) return@forEach
            val fields = mutableSetOf<String>()
            v.array<String>("fields")?.forEach inner@{ field ->
                if (field.startsWith('{')) {
                    fields.add(field)  // fields from other feature id, consider when reading
                    return@inner
                }
                val jsonField = fieldsJson[field] as? JsonObject ?: return@inner
                if (jsonField.string("type") == "multiCombo") return@inner // don't show incomplete keys like "payment:"
                jsonField.array<String>("keys")?.let { fields.addAll(it) } ?: // prefer "keys" over "key", though usually only one is present
                    jsonField.string("key")?.let { fields.add(it) }
            }
            val moreFields = mutableSetOf<String>()
            v.array<String>("moreFields")?.forEach inner@{ field ->
                if (field.startsWith('{')) {
                    moreFields.add(field)  // fields from other feature id, consider when reading
                    return@inner
                }
                val jsonField = fieldsJson[field] as? JsonObject ?: return@inner
                if (jsonField.string("type") == "multiCombo") return@inner // don't show incomplete keys like "payment:"
                jsonField.array<String>("keys")?.let { moreFields.addAll(it) } ?: // prefer "keys" over "key", though usually only one is present
                    jsonField.string("key")?.let { moreFields.add(it) }
            }
            if (fields.isEmpty() && moreFields.isEmpty()) return@forEach
            if (keySuggestionsForFeature.containsKey(k)) throw IllegalStateException("duplicate key in presets.json")
            keySuggestionsForFeature[k] = fields.takeIf { it.isNotEmpty() } to moreFields.takeIf { it.isNotEmpty() }
        }
        File("$targetDir/keySuggestionsForFeature.json").writeText(Json.encodeToString(keySuggestionsForFeature))
    }

    /** Fetch iD presets */
    private fun fetchPresets(version: String): String {
        val presetsUrl = "https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/$version/dist/presets.json"
        return URL(presetsUrl).readText()
    }

    /** Fetch iD fields */
    private fun fetchFields(version: String): String {
        val fieldsUrl = "https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/$version/dist/fields.json"
        return URL(fieldsUrl).readText()
    }

}
