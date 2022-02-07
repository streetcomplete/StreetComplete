import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

open class UpdateNsiPresetsTask : DefaultTask() {
    @get:Input var targetDir: String? = null
    @get:Input var version: String? = null

    @TaskAction
    fun run() {
        val targetDir = targetDir ?: return
        val version = version ?: return

        val presetsUrl = URL("https://raw.githubusercontent.com/osmlab/name-suggestion-index/$version/dist/presets/nsi-id-presets.min.json")
        val nsiPresetsJson = Parser.default().parse(presetsUrl.openStream()) as JsonObject
        // NSI uses (atm) a slightly different format than the normal presets: The presets are in
        // a sub-object called "presets"
        val presets = nsiPresetsJson.obj("presets")!!
        // since we already read the JSON and it is so large, let's drop some properties that are
        // (currently) not used, to make it a bit smaller: icon, imageURL
        for (preset in presets.values.filterIsInstance<JsonObject>()) {
            preset.remove("icon")
            preset.remove("imageURL")
        }
        val byCountryMap = mutableMapOf<String?, JsonObject>()
        for (entry in presets.entries) {
            val key = entry.key
            val preset = entry.value as JsonObject
            val locationSet = preset["locationSet"] as? JsonObject
            val include = locationSet?.get("include") as? JsonArray<*>
            val exclude = locationSet?.get("exclude")
            val includeContains001 = include?.any { it as? String == "001" } == true
            val includeCanBeParsed = include.orEmpty().all { it is String && (it == "001" || it.length == 2) }
            if (includeCanBeParsed) {
                if (include != null && exclude == null && !includeContains001) {
                    for (country in include) {
                        byCountryMap.getOrPut(country as String) { JsonObject() }[key] = preset
                    }
                } else {
                    byCountryMap.getOrPut(null) { JsonObject() }[key] = preset
                }
            }
        }

        for ((country, jsonObject) in byCountryMap.entries) {
            val name = "$targetDir/presets${ if (country != null) "-$country" else "" }.json"
            File(name).writeText(jsonObject.toJsonString())
        }
    }
}
