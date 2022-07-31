package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.quests.surface.Surface.ARTIFICIAL_TURF
import de.westnordost.streetcomplete.quests.surface.Surface.ASPHALT
import de.westnordost.streetcomplete.quests.surface.Surface.CLAY
import de.westnordost.streetcomplete.quests.surface.Surface.COMPACTED
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE_LANES
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE_PLATES
import de.westnordost.streetcomplete.quests.surface.Surface.DIRT
import de.westnordost.streetcomplete.quests.surface.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.quests.surface.Surface.GRASS
import de.westnordost.streetcomplete.quests.surface.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.quests.surface.Surface.GRAVEL
import de.westnordost.streetcomplete.quests.surface.Surface.GROUND_AREA
import de.westnordost.streetcomplete.quests.surface.Surface.GROUND_ROAD
import de.westnordost.streetcomplete.quests.surface.Surface.METAL
import de.westnordost.streetcomplete.quests.surface.Surface.PAVED_AREA
import de.westnordost.streetcomplete.quests.surface.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.quests.surface.Surface.PAVING_STONES
import de.westnordost.streetcomplete.quests.surface.Surface.PEBBLES
import de.westnordost.streetcomplete.quests.surface.Surface.ROCK
import de.westnordost.streetcomplete.quests.surface.Surface.SAND
import de.westnordost.streetcomplete.quests.surface.Surface.SETT
import de.westnordost.streetcomplete.quests.surface.Surface.TARTAN
import de.westnordost.streetcomplete.quests.surface.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.quests.surface.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.quests.surface.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.quests.surface.Surface.WOOD
import de.westnordost.streetcomplete.quests.surface.Surface.WOODCHIPS

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
}

val PAVED_SURFACES = listOf(
    ASPHALT, CONCRETE, CONCRETE_PLATES, CONCRETE_LANES,
    PAVING_STONES, SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
    WOOD, METAL
)

val UNPAVED_SURFACES = listOf(
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

val Surface.shouldBeDescribed: Boolean get() = this == PAVED_ROAD || this == UNPAVED_ROAD || this == PAVED_AREA || this == UNPAVED_AREA
