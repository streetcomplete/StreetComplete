package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.surface.Surface.*

enum class Surface(val osmValue: String) {
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    CONCRETE_PLATES("concrete:plates"),
    CONCRETE_LANES("concrete:lanes"),
    FINE_GRAVEL("fine_gravel"),
    PAVING_STONES("paving_stones"),
    COMPACTED("compacted"),
    DIRT("dirt"),
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

    // extra values, recording duplicates
    MUD("mud"), // valid value, but not displayed specially in StreetComplete
    EARTH("earth"), // synonym of "dirt", maybe more clear
    // this values ideally would be removed from OpenStreetMap, but while they remain
    // we want to handle them somehow
    SOIL("soil"),
    PAVING_STONES_WITH_WEIRD_SUFFIX("paving_stones:30"),
    COBLLESTONE_FLATTENED("cobblestone:flattened"),
    BRICK("brick"),
    BRICKS("bricks");
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
    DIRT, GRASS, SAND, ROCK
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

val UNDERSPECIFED_SURFACES = GENERIC_ROAD_SURFACES + GENERIC_AREA_SURFACES + null

val Surface.shouldBeDescribed: Boolean get() = this in UNDERSPECIFED_SURFACES
