package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.R

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

