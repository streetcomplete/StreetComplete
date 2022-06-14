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

fun isSurfaceAndTracktypeMismatching(surface: String, tracktype: String): Boolean {
    if (tracktype == "grade1" && surface in ANYTHING_UNPAVED) {
        return true
    }
    if (tracktype == "grade2" && surface in SOFT_SURFACES) {
        return true
    }
    if ((tracktype == "grade3" || tracktype == "grade4" || tracktype == "grade5") && surface in ANYTHING_FULLY_PAVED) {
        return true
    }
    return false
}
