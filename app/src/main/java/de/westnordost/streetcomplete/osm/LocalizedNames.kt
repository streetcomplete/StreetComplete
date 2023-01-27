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
    // applying a list of localized names always replaces the entire list of localized names, i.e.
    // anything not specified gets deleted.
    for (key in tags.keys) {
        val isLocalizedName = namePattern.matches(key)
        if (isLocalizedName) tags.remove(key)
    }
    tags.remove("int_name")

    if (isEmpty()) return

    // if it has names, it is not noname...
    tags.remove("noname")
    tags.remove("name:signed")

    // language is only specified explicitly in OSM (usually) if there is more than one name specified
    if (size == 1) {
        tags["name"] = first().name
    } else {
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
}

private val namePattern = Regex("name(?::([a-z]{2,3}(?:-[a-zA-Z]{4})?))?")
