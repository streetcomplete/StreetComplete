package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.overlays.Color

val Surface?.color get() = when (this) {
    // https://davidmathlogic.com/colorblind/#%23444444-%23FF0000-%231A87E6-%232FACE8-%2330D4EE-%2310C1B8-%230DA082-%23F37D1E-%23EEBD0D-%23B6EF28-%23DDDDDD-%23999999
    ASPHALT, CONCRETE, PAVING_STONES, WOOD, METAL -> Color.BLUE
    CONCRETE_PLATES, CONCRETE_LANES, SETT -> Color.SKY
    COMPACTED -> Color.CYAN
    UNHEWN_COBBLESTONE -> Color.AQUAMARINE
    GRASS_PAVER -> Color.TEAL
    SAND  -> Color.ORANGE
    GRASS  -> Color.LIME
    DIRT, SOIL, EARTH, GROUND_ROAD, GROUND_AREA, WOODCHIPS -> Color.GOLD
    FINE_GRAVEL -> "#dddddd"
    GRAVEL, PEBBLES, ROCK -> "#999999"
    CLAY, ARTIFICIAL_TURF, TARTAN -> Color.BLACK // not encountered in normal situations, get the same as surface with surface:note
    PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA, null -> Color.DATA_REQUESTED
}
