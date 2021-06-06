package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.quests.surface.Surface.*

enum class Surface(val osmValue: String) {
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    FINE_GRAVEL("fine_gravel"),
    PAVING_STONES("paving_stones"),
    COMPACTED("compacted"),
    DIRT("dirt"),
    SETT("sett"),
    // https://forum.openstreetmap.org/viewtopic.php?id=61042
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
    PAVED("paved"),
    UNPAVED("unpaved"),
    GROUND("ground"),
}

val PAVED_SURFACES = listOf(
    ASPHALT, CONCRETE, PAVING_STONES,
    SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
    WOOD, METAL
)

val UNPAVED_SURFACES = listOf(
    COMPACTED, FINE_GRAVEL, GRAVEL, PEBBLES
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
    ROCK
)

val GENERIC_SURFACES = listOf(
    PAVED, UNPAVED, GROUND
)

val Surface.shouldBeDescribed: Boolean get() = this == PAVED || this == UNPAVED
