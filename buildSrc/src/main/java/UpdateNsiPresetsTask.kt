import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

/** Download and split the brand presets from the name suggestion index by countries they are in:
 *  Instead of one big presets file, sort those brands that only exist in certain countries into own
 *  files (presets-DE.json etc.).
 *
 *  This is done because the name suggestion index presets JSON became so big (10MB and growing)
 *  that it really slows down startup time. Many brand presets do not actually need to be loaded at
 *  all because they exist only in select countries and the user will only be editing usually in one
 *  country per session anyways. */
open class UpdateNsiPresetsTask : DefaultTask() {
    @get:Input lateinit var targetDir: String
    @get:Input lateinit var version: String

    @TaskAction
    fun run() {
        val presetsUrl = URL("https://raw.githubusercontent.com/osmlab/name-suggestion-index/$version/dist/presets/nsi-id-presets.min.json")
        val nsiPresetsJson = Parser.default().parse(presetsUrl.openStream()) as JsonObject
        /* NSI uses (atm) a slightly different format than the normal presets: The presets are in
           a sub-object called "presets" */
        val presets = nsiPresetsJson.obj("presets")!!
        /* since we already read the JSON and it is so large, let's drop some properties that are
           (currently) not used, to make it a bit smaller: icon, imageURL */
        for (preset in presets.values.filterIsInstance<JsonObject>()) {
            preset.remove("icon")
            preset.remove("imageURL")
        }

        // remove presets with locationSets that cannot be parsed by osmfeatures library
        presets.values.retainAll { value ->
            val locationSet = (value as JsonObject)["locationSet"] as? JsonObject
            val include = locationSet?.get("include") as? JsonArray<Any?>
            val exclude = locationSet?.get("exclude") as? JsonArray<Any?>
            // remove unsupported includes. This ensures that presets that are *also* shown in
            // unsupported regions are at least shown in those that are supported (see #6267).
            // But only if then at least one include is left (because no includes would mean
            // "available everywhere").
            if (include != null) {
                val validIncludes = include.filter { countryCodeIsParsable(it) }
                if (validIncludes != include && validIncludes.isNotEmpty()) {
                    include.clear()
                    include.addAll(validIncludes)
                }
            }
            // ALL (remaining) includes and excludes must be parsable. E.g. a preset that should be
            // available in all of USA except in a 50km radius around Whateverdale should be
            // excluded completely, as the alternative would be to also show it in Whateverdale and
            // that would be a problem (cause there is usually good reason why it was excluded)
            return@retainAll include.orEmpty().all { countryCodeIsParsable(it) }
                && exclude.orEmpty().all { countryCodeIsParsable(it) }
        }

        // expand M49 codes and transform US-NY.geojson etc. strings to US-NY etc.
        for (value in presets.values) {
            val locationSet = (value as JsonObject)["locationSet"] as? JsonObject ?: continue
            val include = locationSet["include"] as? JsonArray<String>
            val exclude = locationSet["exclude"] as? JsonArray<String>
            if (include != null) transform(include)
            if (exclude != null) transform(exclude)
            // remove "locationSet": { "include": "001" } because that's the default
            if (include?.singleOrNull() == "001" && exclude == null) {
                value.remove("locationSet")
            }
        }

        // sort into separate files
        val byCountryMap = mutableMapOf<String?, JsonObject>()
        for (entry in presets.entries) {
            val key = entry.key
            val preset = entry.value as JsonObject
            val locationSet = preset["locationSet"] as? JsonObject
            val include = locationSet?.get("include") as? JsonArray<*>
            val includeContains001 = include?.any { it as? String == "001" } == true
            if (include != null && !includeContains001) {
                for (country in include) {
                    byCountryMap.getOrPut(country as String) { JsonObject() }[key] = preset
                }
            } else {
                byCountryMap.getOrPut(null) { JsonObject() }[key] = preset
            }
        }

        for ((country, jsonObject) in byCountryMap.entries) {
            val name = "$targetDir/presets${ if (country != null) "-${country.uppercase()}" else "" }.json"
            File(name).writeText(jsonObject.toJsonString())
        }
    }
}

private fun countryCodeIsParsable(code: Any?): Boolean =
    code is String
        && (ISO3166_1_ALPHA2_REGEX.matches(code)
            || ISO3166_2_REGEX.matches(code)
            || UN_M49_REGEX.matches(code)
        )

private fun transform(codes: MutableList<String>) {
    expandM49Codes(codes)
    transformSubdivisions(codes)
    removeDuplicates(codes)
}

private fun transformSubdivisions(codes: MutableList<String>) {
    for (i in 0 until codes.size) {
        codes[i] = transformSubdivision(codes[i])
    }
}

/** transform "us-ny.geojson" to "us-ny" */
private fun transformSubdivision(code: String): String {
    val match = ISO3166_2_REGEX.matchEntire(code) ?: return code
    val iso3166_1_alpha2 = match.groupValues[1]
    val iso3166_2 = match.groupValues[2]
    return if (iso3166_1_alpha2 in SUPPORTED_SUBDIVISIONS) "$iso3166_1_alpha2-$iso3166_2" else iso3166_1_alpha2
}

val UN_M49_REGEX = Regex("[0-9]{3}")
val ISO3166_1_ALPHA2_REGEX = Regex("([a-z]{2})")
val ISO3166_2_REGEX = Regex("([a-z]{2})-([a-z0-9]{1,3})(\\.geojson)?")

val SUPPORTED_SUBDIVISIONS = setOf("us", "ca", "in", "au", "cn")

/** resolve M49 codes to country codes */
private fun expandM49Codes(codes: MutableList<String>) {
    var i = 0
    while (i < codes.size) {
        val cc = codes[i]
        val expandedCodes = M49Codes[cc]
        if (expandedCodes != null) {
            codes.removeAt(i)
            codes.addAll(i, expandedCodes.map { it.lowercase() })
        } else {
            ++i
        }
    }
}

private fun removeDuplicates(codes: MutableList<String>) {
    val set = codes.toSet()
    codes.clear()
    codes.addAll(set)
}
