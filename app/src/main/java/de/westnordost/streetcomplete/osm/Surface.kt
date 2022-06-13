package de.westnordost.streetcomplete.osm

val ANYTHING_UNPAVED = setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
    "ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips"
)

val ANYTHING_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:lanes", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone"
)

fun isSurfaceAndTractypeMismatching(surface: String, tracktype: String): Boolean {
    if (tracktype == "grade1") {
        if (arrayOf("sand",
                "gravel",
                "fine_gravel",
                "compacted",
                "grass",
                "earth",
                "dirt",
                "mud",
                "pebbles",
                "unpaved").contains(surface)
        ) {
            return true
        }
    }
    if (tracktype == "grade2") {
        if (surface == "sand" ||
            surface == "grass" ||
            surface == "earth" ||
            surface == "dirt" ||
            surface == "mud"
        ) {
            return true
        }
    }
    if (tracktype == "grade3" || tracktype == "grade4" || tracktype == "grade5") {
        if (surface == "asphalt" ||
            surface == "concrete" ||
            surface == "paving_stones" ||
            surface == "paved"
        ) {
            return true
        }
    }
    return false
}
