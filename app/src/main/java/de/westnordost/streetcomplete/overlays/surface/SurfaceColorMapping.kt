package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.isComplete
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
val Surface.color get() = when (this) {
    ASPHALT, CHIPSEAL, CONCRETE
                       -> Color.BLUE
    PAVING_STONES, PAVING_STONES_WITH_WEIRD_SUFFIX, BRICK, BRICKS
                       -> Color.SKY
    CONCRETE_PLATES, CONCRETE_LANES, SETT, COBBLESTONE_FLATTENED
                       -> Color.CYAN
    UNHEWN_COBBLESTONE, GRASS_PAVER
                       -> Color.AQUAMARINE
    COMPACTED, FINE_GRAVEL
                       -> Color.TEAL
    DIRT, SOIL, EARTH, MUD, GROUND, WOODCHIPS
                       -> Color.ORANGE
    GRASS              -> Color.LIME // greenish colour for grass is deliberate
    SAND               -> Color.GOLD // yellowish color for sand is deliberate
                                     // sand and grass are strongly associated with
                                     // this colors
    GRAVEL, PEBBLES, ROCK,
    // very different from above but unlikely to be used in same places, i.e. below are usually on bridges
    WOOD, METAL, METAL_GRID
                       -> Color.GRAY
    UNKNOWN,
    PAVED, UNPAVED, // overriden in getColor of note is note is not present
    // not encountered in normal situations, get the same as surface with surface:note
    CLAY, ARTIFICIAL_TURF, TARTAN
                       -> Color.BLACK
}

fun SurfaceAndNote?.getColor(element: Element): String =
    if (isComplexSurfaceLanes(element.tags)) {
            Color.BLACK // same as other complex surfaces, e.g. surface=unpaved with surface:note=*
    } else if (this?.isComplete != true) {
        // not set but indoor, private or just a "virtual" link -> do not highlight as missing
        if (isIndoor(element.tags) || isPrivateOnFoot(element) || isLink(element.tags)) {
            Color.INVISIBLE
        } else {
            Color.DATA_REQUESTED
        }
    } else {
        surface!!.color
    }

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"

private fun isLink(tags: Map<String, String>): Boolean =
    tags["path"] == "link"
    || tags["footway"] == "link"
    || tags["cycleway"] == "link"
    || tags["bridleway"] == "link"

private fun isComplexSurfaceLanes(tags: Map<String, String>): Boolean =
    tags["surface:lanes"] != null || tags["surface:lanes:forward"] != null || tags["surface:lanes:backward"] != null || tags["surface:lanes:both_lanes"] != null
