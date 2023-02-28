package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.osm.updateWithCheckDate

enum class Surface(val osmValue: String?) {
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    CONCRETE_PLATES("concrete:plates"),
    CONCRETE_LANES("concrete:lanes"),
    FINE_GRAVEL("fine_gravel"),
    PAVING_STONES("paving_stones"),
    COMPACTED("compacted"),
    DIRT("dirt"),
    MUD("mud"),
    SETT("sett"),
    UNHEWN_COBBLESTONE("unhewn_cobblestone"),
    GRASS_PAVER("grass_paver"),
    WOOD("wood"),
    WOODCHIPS("woodchips"),
    METAL("metal"),
    GRAVEL("gravel"),
    PEBBLES("pebblestone"),
    GRASS("grass"),
    SAND("sand"),
    ROCK("rock"),
    CLAY("clay"),
    ARTIFICIAL_TURF("artificial_turf"),
    TARTAN("tartan"),
    PAVED_ROAD("paved"),
    UNPAVED_ROAD("unpaved"),
    GROUND_ROAD("ground"),
    PAVED_AREA("paved"),
    UNPAVED_AREA("unpaved"),
    GROUND_AREA("ground"),

    // extra values, handled as synonyms
    EARTH("earth"), // synonym of "dirt"
    CHIPSEAL("chipseal"), // subtype/synonym of asphalt
    METAL_GRID("metal_grid"), // not exactly the same info, but better to show it
    // this way than without info

    // these values ideally would be removed from OpenStreetMap, but while they remain
    // we want to handle them as synonyms
    SOIL("soil"), // synonym of earth and dirt
    PAVING_STONES_WITH_WEIRD_SUFFIX("paving_stones:30"),
    COBBLESTONE_FLATTENED("cobblestone:flattened"),
    BRICK("brick"),
    BRICKS("bricks"),

    // various possibly valid surfaces not supported as duplicates
    UNKNOWN_SURFACE(null),
}

val COMMON_SPECIFIC_PAVED_SURFACES = listOf(
    ASPHALT, CONCRETE, CONCRETE_PLATES, CONCRETE_LANES,
    PAVING_STONES, SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
    WOOD, METAL
)

val COMMON_SPECIFIC_UNPAVED_SURFACES = listOf(
    COMPACTED, FINE_GRAVEL, GRAVEL, PEBBLES, WOODCHIPS
)

val GROUND_SURFACES = listOf(
    DIRT, MUD, GRASS, SAND, ROCK
)

val PITCH_SURFACES = listOf(
    GRASS, ASPHALT, SAND, CONCRETE,
    CLAY, ARTIFICIAL_TURF, TARTAN, DIRT,
    FINE_GRAVEL, PAVING_STONES, COMPACTED,
    SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
    WOOD, METAL, GRAVEL, PEBBLES,
    ROCK, PAVED_AREA, UNPAVED_AREA, GROUND_AREA
)

val GENERIC_ROAD_SURFACES = listOf(
    PAVED_ROAD, UNPAVED_ROAD, GROUND_ROAD
)

val GENERIC_AREA_SURFACES = listOf(
    PAVED_AREA, UNPAVED_AREA, GROUND_AREA
)

val UNDERSPECIFED_SURFACES = GENERIC_ROAD_SURFACES + GENERIC_AREA_SURFACES + null - GROUND_ROAD - GROUND_AREA

val Surface.shouldBeDescribed: Boolean get() = this in UNDERSPECIFED_SURFACES

val Surface.unknownSurface: Boolean get() = this == UNKNOWN_SURFACE

fun Surface.applyTo(tags: Tags, prefix: String? = null, updateCheckDate: Boolean = true, note: String?) {
    val pre = if (prefix != null) "$prefix:" else ""
    val key = "${pre}surface"
    val osmValue = osmValue
    if (osmValue == null) {
        throw IllegalArgumentException("callers of applyTo are obligated to provide only taggable data")
    }
    val previousOsmValue = tags[key]

    var replacesTracktype = false
    if (prefix == null) {
        replacesTracktype = tags.containsKey("tracktype")
            && isSurfaceAndTracktypeMismatching(osmValue, tags["tracktype"]!!)

        if (replacesTracktype) {
            tags.remove("tracktype")
            tags.removeCheckDatesForKey("tracktype")
        }
    }

    // remove smoothness (etc) tags if surface was changed
    // or surface can be treated as outdated
    if ((previousOsmValue != null && previousOsmValue != osmValue) || replacesTracktype) {
        for (target in keysToBeRemovedOnSurfaceChange(pre)) {
            tags.remove(target)
        }
    }

    // update surface + check date
    if (updateCheckDate) {
        tags.updateWithCheckDate(key, osmValue)
    } else {
        tags[key] = osmValue
    }

    // add/remove note - used to describe generic surfaces
    if (note != null) {
        tags["$key:note"] = note
    } else {
        tags.remove("$key:note")
    }

    // clean up old source tags - source should be in changeset tags
    tags.remove("source:$key")
}

fun SurfaceAnswer.updateSegregatedFootAndCycleway(tags: Tags) {
    val footwaySurface = tags["footway:surface"]
    val cyclewaySurface = tags["cycleway:surface"]
    if (cyclewaySurface != null && footwaySurface != null) {
        val commonSurface = when {
            footwaySurface == cyclewaySurface -> this
            footwaySurface in ANYTHING_FULLY_PAVED && cyclewaySurface in ANYTHING_FULLY_PAVED -> SurfaceAnswer(Surface.PAVED_ROAD)
            else -> null
        }
        if (commonSurface != null) {
            commonSurface.applyTo(tags)
        } else {
            removeSurface(tags)
        }
    }
}

private fun removeSurface(tags: Tags) {
    tags.remove("surface")
    tags.remove("surface:note")
    tags.remove("source:surface")
    tags.removeCheckDatesForKey("surface")
    tags.remove("surface:grade")
    tags.remove("smoothness")
    tags.remove("smoothness:date")
    tags.remove("source:smoothness")
    tags.removeCheckDatesForKey("smoothness")
}
