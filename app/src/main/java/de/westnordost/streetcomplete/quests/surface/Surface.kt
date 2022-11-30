package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.ARTIFICIAL_TURF
import de.westnordost.streetcomplete.osm.surface.Surface.ASPHALT
import de.westnordost.streetcomplete.osm.surface.Surface.CLAY
import de.westnordost.streetcomplete.osm.surface.Surface.COMPACTED
import de.westnordost.streetcomplete.osm.surface.Surface.CONCRETE
import de.westnordost.streetcomplete.osm.surface.Surface.CONCRETE_LANES
import de.westnordost.streetcomplete.osm.surface.Surface.CONCRETE_PLATES
import de.westnordost.streetcomplete.osm.surface.Surface.DIRT
import de.westnordost.streetcomplete.osm.surface.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.osm.surface.Surface.GRASS
import de.westnordost.streetcomplete.osm.surface.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.osm.surface.Surface.GRAVEL
import de.westnordost.streetcomplete.osm.surface.Surface.GROUND_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.GROUND_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.METAL
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.PAVING_STONES
import de.westnordost.streetcomplete.osm.surface.Surface.PEBBLES
import de.westnordost.streetcomplete.osm.surface.Surface.ROCK
import de.westnordost.streetcomplete.osm.surface.Surface.SAND
import de.westnordost.streetcomplete.osm.surface.Surface.SETT
import de.westnordost.streetcomplete.osm.surface.Surface.TARTAN
import de.westnordost.streetcomplete.osm.surface.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.WOOD
import de.westnordost.streetcomplete.osm.surface.Surface.WOODCHIPS
import de.westnordost.streetcomplete.osm.surface.UNDERSPECIFED_SURFACES

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

val Surface.shouldBeDescribed: Boolean get() = this in UNDERSPECIFED_SURFACES
