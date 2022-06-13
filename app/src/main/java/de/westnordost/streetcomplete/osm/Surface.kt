package de.westnordost.streetcomplete.osm

val SOFT_SURFACES = setOf("ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips")

val ANYTHING_UNPAVED = SOFT_SURFACES + setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
)

val ANYTHING_FULLY_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone"
)

val ANYTHING_PAVED = ANYTHING_FULLY_PAVED + setOf(
    "concrete:lanes"
)

fun isSurfaceAndTractypeMismatching(surface: String, tracktype: String): Boolean {
    if (tracktype == "grade1") {
        if (ANYTHING_UNPAVED.contains(surface)) {
            return true
        }
    }
    if (tracktype == "grade2") {
        if (SOFT_SURFACES.contains(surface)) {
            return true
        }
    }
    if (tracktype == "grade3" || tracktype == "grade4" || tracktype == "grade5") {
        if (ANYTHING_FULLY_PAVED.contains(surface)) {
            return true
        }
    }
    return false
}
