package de.westnordost.streetcomplete.quests.road_name.data

/** OSM tags to map of language code -> name.
 *
 *  For example:
 *
 *  "name:de": "Hauptstraße"
 *  "name": "Hauptstraße"
 *  "int_name": "main road"
 *  "name:de-Cyrl": "Хауптстра"
 *
 *  becomes
 *
 *  "de": "Hauptstraße"
 *  "": "Hauptstraße"
 *  "international": "main road"
 *  "de-Cyrl": "Хауптстра"
 *
 *  Tags that are not two- or three-letter ISO 639 language codes appended with an optional 4-letter
 *  ISO 15924 code, such as name:left, name:etymology, name:source etc., are ignored
 *  */
fun Map<String,String>.toRoadNameByLanguage(): Map<String, String>? {
    val result = mutableMapOf<String,String>()
    val namePattern = Regex("name(?::([a-z]{2,3}(?:-[a-zA-Z]{4})?))?")
    for ((key, value) in this) {
        val m = namePattern.matchEntire(key)
        if (m != null) {
            val languageTag = m.groupValues[1]
            result[languageTag] = value
        } else if(key == "int_name") {
            result["international"] = value
        }
    }
    return if (result.isEmpty()) null else result
}
