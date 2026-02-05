package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys

val INVALID_SURFACES = setOf(
    // sorted roughly by usage count

    "cobblestone", // ambiguous (could be unhewn cobblestone or sett)

    "trail", // unclear meaning, deprecated

    "cobblestone:flattened", // sett with good smoothness, deprecated in the wiki

    "cement", // see https://community.openstreetmap.org/t/mysterious-surface-cement/5158 and https://wiki.openstreetmap.org/wiki/Tag:surface%3Dconcrete

    "hard", // badly worded: surface used for tennis hard courts, which is synthetic resin (-> acrylic)
    "hard_court", // same
    "decoturf", // that's a brand name for acrylic resin

    "rocky", // undocumented, "rock" surface is already described in wiki as rocky
    "rocks", // same, deprecated

    "paving_stones:30", // deprecated
    "paving_stones:20", // deprecated

    "turf", // ambiguous (natural turf or artificial turf)

    "yes", "none", "footway",
)

val FULLY_PAVED_SURFACES = setOf(
    "paved",
    // sealed
    "asphalt", "concrete", "chipseal", "concrete:plates", "cement",
    // paving stones
    "brick", "bricks", "paving_stones", "paving_stones:30",
    "sett", "cobblestone", "cobblestone:flattened", "unhewn_cobblestone",
    // other
    "metal", "wood", "plastic", "acrylic", "tartan", "rubber",
)

private val SOFT_NATURAL_SURFACES = setOf(
    // earthy
    "earth", "dirt", "soil", "grass", "sand", "mud",
    // other
    "snow",
)

val NATURAL_SURFACES = SOFT_NATURAL_SURFACES + setOf(
    "ground",
    "ice", "salt", "rock", "stone", "stepping_stones",
)

val UNPAVED_SURFACES = NATURAL_SURFACES + setOf(
    "unpaved",
    "compacted", "gravel", "fine_gravel", "pebblestone",
    "woodchips", "artificial_turf", "clay",
)

val PAVED_SURFACES = FULLY_PAVED_SURFACES + setOf(
    "concrete:lanes",
    "grass_paver",
    "metal_grid",
)

// very lenient, to not flag surface+trackype combinations as invalid in edge cases
val INVALID_SURFACES_FOR_TRACKTYPES = mapOf(
    "grade1" to SOFT_NATURAL_SURFACES, // could be compacted, as long as it is "solid"
    // "mostly solid" can mean a lot of things, including natural surfaces that happen to be quite solid
    // Allowing any surface for grade2 tracktype is a compromise from #6611
    //"grade2" to SOFT_NATURAL_SURFACES,
    "grade3" to FULLY_PAVED_SURFACES,
    "grade4" to FULLY_PAVED_SURFACES,
    "grade5" to FULLY_PAVED_SURFACES,
)
/** Sets the common surface of the foot- and cycleway parts into the surface tag, if any. If the
 *  surfaces of the foot- and cycleway parts have nothing in common, removes the surface tag */
fun updateCommonSurfaceFromFootAndCyclewaySurface(tags: Tags) {
    val footwaySurface = tags["footway:surface"]
    val cyclewaySurface = tags["cycleway:surface"]
    if (cyclewaySurface != null && footwaySurface != null) {
        val commonSurface = getCommonSurface(footwaySurface, cyclewaySurface)
        if (commonSurface != null) {
            parseSurface(commonSurface)?.applyTo(tags)
        } else {
            tags.remove("surface")
            tags.remove("surface:note")
            getKeysAssociatedWithSurface().forEach { tags.remove(it) }
        }
    }
}

private fun getCommonSurface(vararg surface: String?): String? = when {
    surface.any { it == null } -> null
    surface.all { it == surface.firstOrNull() } -> surface.firstOrNull()
    surface.all { it in PAVED_SURFACES } -> "paved"
    surface.all { it in UNPAVED_SURFACES } -> "unpaved"
    else -> null
}

fun getKeysAssociatedWithSurface(prefix: String = ""): Set<String> =
    setOf(
        "${prefix}surface:note",
        "${prefix}surface:grade",
        "${prefix}surface:colour",
        "source:${prefix}surface",
        "${prefix}smoothness",
        "${prefix}smoothness:date",
        "source:${prefix}smoothness",
        "${prefix}paving_stones:shape",
        "${prefix}paving_stones:pattern",
        "${prefix}paving_stones:length",
        "${prefix}paving_stones:width",
    ) +
        getLastCheckDateKeys("${prefix}surface") +
        getLastCheckDateKeys("${prefix}smoothness")
