package de.westnordost.streetcomplete.osm

import kotlinx.serialization.Serializable

@Serializable
data class LocalizedName(var languageTag: String, var name: String)

/** OSM tags to map of language code -> name.
 *
 *  For example:
 *
 *  "name:de": "Hauptstraße"
 *  "name": "Hauptstraße"
 *  "int_name": "main road"
 *  "unrelated tag": "blabla"
 *  "name:de-Cyrl": "Хауптстра"
 *
 *  becomes
 *
 *  "": "Hauptstraße"
 *  "de": "Hauptstraße"
 *  "international": "main road"
 *  "de-Cyrl": "Хауптстра"
 *
 *  Tags that are not two- or three-letter ISO 639 language codes appended with an optional 4-letter
 *  ISO 15924 code, such as name:left, name:etymology, name:source etc., are ignored
 *  */
fun createLocalizedNames(tags: Map<String, String>): List<LocalizedName>? {
    val result = ArrayList<LocalizedName>()
    val namePattern = Regex("name(?::([a-z]{2,3}(?:-[a-zA-Z]{4})?))?")
    for ((key, value) in tags) {
        val m = namePattern.matchEntire(key)
        if (m != null) {
            val languageTag = m.groupValues[1]
            val name = LocalizedName(languageTag, value)
            // main name is always the first
            if (languageTag == "") {
                result.add(0, name)
            } else {
                result.add(name)
            }
        } else if (key == "int_name") {
            result.add(LocalizedName("international", value))
        }
    }

    return if (result.isEmpty()) null else result
}

fun List<LocalizedName>.applyTo(tags: Tags) {
    if (isEmpty()) return

    // language is only specified explicitly in OSM (usually) if there is more than one name specified
    if (size == 1) {
        tags["name"] = first().name
        return
    }

    for ((language, name) in this) {
        val key = when (language) {
            "" -> "name"
            "international" -> "int_name"
            else -> "name:$language"
        }
        tags[key] = name
    }

    // but if there is more than one language, ensure that a "main" name is also specified
    if (find { it.languageTag == "" } == null) {
        // use the name specified in the topmost row for that
        tags["name"] = first().name
    }
}
