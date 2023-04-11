package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys

val INVALID_SURFACES = setOf(
    "cobblestone", // https://wiki.openstreetmap.org/wiki/Tag%3Asurface%3Dcobblestone
    "cement", // https://community.openstreetmap.org/t/mysterious-surface-cement/5158 and https://wiki.openstreetmap.org/wiki/Tag:surface%3Dconcrete
)

val SOFT_SURFACES = setOf(
    "earth", "dirt", "soil", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips"
)

val ANYTHING_UNPAVED = SOFT_SURFACES + setOf(
    "ground", // see https://community.openstreetmap.org/t/is-tracktype-grade2-also-for-trails-with-large-naturally-occuring-pieces-of-rock/96850
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver"
)

val ANYTHING_FULLY_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone", "chipseal",
    "brick", "bricks", "cobblestone:flattened", "paving_stones:30",
)

val ANYTHING_PAVED = ANYTHING_FULLY_PAVED + setOf(
    "concrete:lanes"
)

val INVALID_SURFACES_FOR_TRACKTYPES = mapOf(
    "grade1" to ANYTHING_UNPAVED,
    "grade2" to SOFT_SURFACES,
    "grade3" to ANYTHING_FULLY_PAVED,
    "grade4" to ANYTHING_FULLY_PAVED,
    "grade5" to ANYTHING_FULLY_PAVED,
)

/** @return whether the given tag value for [surface] contradicts the tag value for [tracktype].
 *  E.g. surface=asphalt but tracktype=grade5. */
fun isSurfaceAndTracktypeConflicting(surface: String, tracktype: String?): Boolean =
    INVALID_SURFACES_FOR_TRACKTYPES[tracktype]?.contains(surface) == true

/** Sets the common surface of the foot- and cycleway parts into the surface tag, if any. If the
 *  surfaces of the foot- and cycleway parts have nothing in common, removes the surface tag */
fun updateCommonSurfaceFromFootAndCyclewaySurface(tags: Tags) {
    val footwaySurface = tags["footway:surface"]
    val cyclewaySurface = tags["cycleway:surface"]
    if (cyclewaySurface != null && footwaySurface != null) {
        val commonSurface = getCommonSurface(footwaySurface, cyclewaySurface)
        if (commonSurface != null) {
            SurfaceAndNote(createSurface(commonSurface), tags["surface:note"]).applyTo(tags)
        } else {
            tags.remove("surface")
            tags.remove("surface:note")
            getKeysAssociatedWithSurface().forEach { tags.remove(it) }
        }
    }
}

private fun getCommonSurface(vararg surface: String?): String?  = when {
    surface.any { it == null } -> null
    surface.all { it == surface.firstOrNull() } -> surface.firstOrNull()
    surface.all { it in ANYTHING_PAVED } -> "paved"
    surface.all { it in ANYTHING_UNPAVED } -> "unpaved"
    else -> null
}

fun getKeysAssociatedWithSurface(prefix: String = ""): Set<String> =
    setOf(
        "${prefix}surface:grade",
        "${prefix}surface:colour",
        "source:${prefix}surface",
        "${prefix}smoothness",
        "${prefix}smoothness:date",
        "source:${prefix}smoothness",
    ) +
        getLastCheckDateKeys("${prefix}surface") +
        getLastCheckDateKeys("${prefix}smoothness")
