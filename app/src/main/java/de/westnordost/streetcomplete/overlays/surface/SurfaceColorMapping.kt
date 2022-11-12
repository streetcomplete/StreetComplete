package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.overlays.Color

val Surface?.color get() = when (this) {
    // https://davidmathlogic.com/colorblind/#%23444444-%23FF0000-%231A87E6-%232FACE8-%2330D4EE-%2310C1B8-%230DA082-%23F37D1E-%23EEBD0D-%23B6EF28
    ASPHALT, CONCRETE, PAVING_STONES, WOOD, METAL -> Color.BLUE
    CONCRETE_PLATES, CONCRETE_LANES, SETT -> Color.SKY
    COMPACTED -> Color.CYAN
    UNHEWN_COBBLESTONE -> Color.AQUAMARINE
    GRASS_PAVER -> Color.TEAL
    SAND  -> Color.ORANGE
    GRASS  -> Color.LIME
    DIRT, GROUND_ROAD, GROUND_AREA, WOODCHIPS -> Color.GOLD
    FINE_GRAVEL -> "#dddddd"
    GRAVEL, PEBBLES, ROCK -> "#999999"
    CLAY, ARTIFICIAL_TURF, TARTAN -> Color.BLACK // not encountered in normal situations, get the same as surface with surface:note
    PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA, null -> Color.DATA_REQUESTED
}    /*
    // TODO create graphs as illustration of design process
    categorizing surface into 8+2 groups

    missing surface (including paved/unpaved) vs missing surface (including paved/unpaved) with note vs surface

    fundamental split:
    all: paved vs unpaved
    paved: flat and flattish vs unhewn_cobblestone
    flat and flattish paved: asphalt/concrete/paving_stones/wood/metal vs sett/concrete:plates vs concrete:lanes
    unpaved: extremely bad for cyclists (sand, grass paver), high quality (compacted), all other
    extremely bad for cyclists: sand, grass paver
    unpaved_other: dirt/gravel/pebblestone/rock/ground/woodchips/fine_gravel vs grass

    special, not really on roads: clay, artificial_turf, tartan
    */
