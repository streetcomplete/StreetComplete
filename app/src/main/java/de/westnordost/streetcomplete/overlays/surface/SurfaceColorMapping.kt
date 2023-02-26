package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.SurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.osm.surface.UNDERSPECIFED_SURFACES
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
 *    should be on border between these two groups
 *
 * - asphalt, concrete, paving stones are associated with grey
 *   but colouring these surfaces as grey resulted in really depressing, unfunny
 *   and soul-crushing display in cities where everything is well paved.
 *   Blue is more fun and less sad (while it will not convince anyone
 *   that concrete desert is a good thing).
 *
 * - highly unusual (for roads and paths) surfaces also got black colour
 * - due to running out of colors all well paved surfaces get the same colour
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
    WOOD, METAL, METAL_GRID
                       -> Color.SKY
    CONCRETE_PLATES, CONCRETE_LANES, SETT, COBBLESTONE_FLATTENED
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
    CLAY, ARTIFICIAL_TURF, TARTAN
                       -> Color.BLACK // not encountered in normal situations, get the same as surface with surface:note
    PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA, null
                       -> Color.DATA_REQUESTED
    UNKNOWN_SURFACE    -> Color.BLACK
}

fun SurfaceWithNote.getColor(element: Element): String {
    if (element.tags["highway"] in listOf("motorway", "motorway_link")) {
        // assume motorways to be well paved (ASPHALT or CONCRETE)
        // in tests equality of color of ASPHALT and CONCRETE is checked
        // (to find relevant test search for motorway or motorway_link)
        // allowing us to do this
        return ASPHALT.color
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = value in UNDERSPECIFED_SURFACES
    val isNotSetButThatIsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element))
    if (isNotSetButThatIsOkay) {
        return Color.INVISIBLE
    }
    return when (value) {
        is Surface -> {
            if (note != null) {
                Color.BLACK
            } else {
                value.color
            }
        }
        null -> {
            Color.DATA_REQUESTED
        }
    }
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
