package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.Surface.ARTIFICIAL_TURF
import de.westnordost.streetcomplete.osm.Surface.ASPHALT
import de.westnordost.streetcomplete.osm.Surface.CLAY
import de.westnordost.streetcomplete.osm.Surface.COMPACTED
import de.westnordost.streetcomplete.osm.Surface.CONCRETE
import de.westnordost.streetcomplete.osm.Surface.CONCRETE_LANES
import de.westnordost.streetcomplete.osm.Surface.CONCRETE_PLATES
import de.westnordost.streetcomplete.osm.Surface.DIRT
import de.westnordost.streetcomplete.osm.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.osm.Surface.GRASS
import de.westnordost.streetcomplete.osm.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.osm.Surface.GRAVEL
import de.westnordost.streetcomplete.osm.Surface.GROUND_AREA
import de.westnordost.streetcomplete.osm.Surface.GROUND_ROAD
import de.westnordost.streetcomplete.osm.Surface.METAL
import de.westnordost.streetcomplete.osm.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.Surface.PAVING_STONES
import de.westnordost.streetcomplete.osm.Surface.PEBBLES
import de.westnordost.streetcomplete.osm.Surface.ROCK
import de.westnordost.streetcomplete.osm.Surface.SAND
import de.westnordost.streetcomplete.osm.Surface.SETT
import de.westnordost.streetcomplete.osm.Surface.TARTAN
import de.westnordost.streetcomplete.osm.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.osm.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.Surface.WOOD
import de.westnordost.streetcomplete.osm.Surface.WOODCHIPS

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

val Surface.shouldBeDescribed: Boolean get() = this == PAVED_ROAD || this == UNPAVED_ROAD || this == PAVED_AREA || this == UNPAVED_AREA
