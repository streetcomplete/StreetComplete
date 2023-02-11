package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.overlays.Color

/*
 * Design considerations:
 * - use standard colour set, for consistency and for better support for colour-blind people
 *  - it is OK to use some extra colours if absolutely needed
 *
 * - red is reserved for missing data
 * - black is reserved for generic surface with note tag
 *
 * - similar surface should have similar colours
 *  - all paved ones are one such group
 *  - all unpaved ones are another
 *  - grass paver and compacted being high quality unpaved, unhewn cobblestone being low quality unpaved
 *    should be on border between this two groups
 *
 * - asphalt, concrete, paving stones are associated with grey
 *   but colouring this surfaces as grey resulted in really depressing, unfun
 *   and soul-crushing display in cities where everything is well paved.
 *   Blue is more fun and less sad (while it will not convince anyone
 *   that concrete desert is a good thing).
 *
 * - highly unusual (for roads and paths) surfaces also got black colour
 * - due to running out of colors all well paved surface get the same colour
 * - due to running out of colours fine gravel, gravel, pebbles, rock got gray surface
 * - dashes were tested but due to limitation of Tangram were not working well and were
 *   incapable of replacing colour coding
 *
 * - ideally, sand would have colour close to yellow and grass colour close to green caused
 *   by extremely strong association between surface and colour
 */
val Surface?.color get() = when (this) {
    ASPHALT, CHIPSEAL, CONCRETE, PAVING_STONES, PAVING_STONES_WITH_WEIRD_SUFFIX, BRICK, BRICKS
                       -> Color.BLUE
    WOOD, METAL        -> Color.SKY
    CONCRETE_PLATES, CONCRETE_LANES, SETT, COBLLESTONE_FLATTENED
                       -> Color.CYAN
    UNHEWN_COBBLESTONE, GRASS_PAVER -> Color.AQUAMARINE
    COMPACTED, FINE_GRAVEL
                       -> Color.TEAL
    SAND               -> Color.ORANGE
    GRASS              -> Color.LIME
    DIRT, SOIL, EARTH, MUD, GROUND_ROAD, GROUND_AREA, WOODCHIPS
                       -> Color.GOLD
    GRAVEL, PEBBLES, ROCK
                       -> Color.GRAY
    CLAY, ARTIFICIAL_TURF, TARTAN, UNIDENTIFIED
                       -> Color.BLACK // not encountered in normal situations, get the same as surface with surface:note
    PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA, null
                       -> Color.DATA_REQUESTED
}
