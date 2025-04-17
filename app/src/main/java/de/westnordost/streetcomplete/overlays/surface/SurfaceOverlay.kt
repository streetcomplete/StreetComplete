package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

class SurfaceOverlay : Overlay {

    override val title = R.string.overlay_surface
    override val icon = R.drawable.ic_quest_street_surface
    override val changesetComment = "Specify surfaces"
    override val wikiLink: String = "Key:surface"
    override val achievements = listOf(CAR, PEDESTRIAN, WHEELCHAIR, BICYCLIST, OUTDOORS)
    override val hidesQuestTypes = setOf(
        AddRoadSurface::class.simpleName!!,
        AddPathSurface::class.simpleName!!,
        AddFootwayPartSurface::class.simpleName!!,
        AddCyclewayPartSurface::class.simpleName!!,
    )

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData.filter("""
            ways, relations with
                highway ~ ${(ALL_PATHS + ALL_ROADS).joinToString("|")}
        """).map { it to getStyle(it) }

    override fun createForm(element: Element?) = SurfaceOverlayForm()
}

private fun getStyle(element: Element): Style {
    val tags = element.tags
    val isArea = tags["area"] == "yes"
    val isSegregated = tags["segregated"] == "yes"
    val isPath = tags["highway"] in ALL_PATHS

    val color = if (isPath && isSegregated) {
        val footwayColor = parseSurface(tags["footway:surface"]).getColor(element)
        val cyclewayColor = parseSurface(tags["cycleway:surface"]).getColor(element)
        // take worst case for showing
        listOf(footwayColor, cyclewayColor).minBy { color ->
            when (color) {
                Color.DATA_REQUESTED -> 0
                Color.INVISIBLE -> 1
                Color.BLACK -> 2
                else -> 3
            }
        }
    } else {
        parseSurface(tags["surface"]).getColor(element)
    }
    return if (isArea) PolygonStyle(color) else PolylineStyle(StrokeStyle(color))
}

private fun Surface?.getColor(element: Element): String =
    this?.color ?: if (surfaceTaggingNotExpected(element)) Color.INVISIBLE else Color.DATA_REQUESTED

/*
 * Design considerations:
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
private val Surface.color get() = when (this) {
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
    GRASS
        -> Color.LIME // greenish colour for grass is deliberate
    SAND
        -> Color.GOLD // yellowish color for sand is deliberate
    GRAVEL, PEBBLES, ROCK,
        // very different from above but unlikely to be used in same places, i.e. below are usually on bridges
    WOOD, METAL, METAL_GRID
        -> Color.GRAY
    UNKNOWN, PAVED, UNPAVED,
        // not encountered in normal situations, get the same as generic surface
    CLAY, ARTIFICIAL_TURF, TARTAN, RUBBER, ACRYLIC, HARD
        -> Color.BLACK
}

private fun surfaceTaggingNotExpected(element: Element) =
    isIndoor(element.tags) || isPrivateOnFoot(element) || isLink(element.tags)

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"

private fun isLink(tags: Map<String, String>): Boolean =
    tags["path"] == "link"
        || tags["footway"] == "link"
        || tags["cycleway"] == "link"
        || tags["bridleway"] == "link"
