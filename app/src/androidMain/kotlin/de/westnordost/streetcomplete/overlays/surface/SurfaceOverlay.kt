package de.westnordost.streetcomplete.overlays.surface

import androidx.compose.ui.graphics.Color
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

class SurfaceOverlay : Overlay, AndroidOverlay {

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

private fun getStyle(element: Element): OverlayStyle {
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
                OverlayColor.Red -> 0
                OverlayColor.Invisible -> 1
                OverlayColor.Black -> 2
                else -> 3
            }
        }
    } else {
        parseSurface(tags["surface"]).getColor(element)
    }
    return if (isArea) OverlayStyle.Polygon(color) else OverlayStyle.Polyline(OverlayStyle.Stroke(color))
}

private fun Surface?.getColor(element: Element): Color =
    this?.color
        ?: if (surfaceTaggingNotExpected(element)) OverlayColor.Invisible else OverlayColor.Red

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
    ASPHALT, CONCRETE,
        -> OverlayColor.Blue
    PAVING_STONES,
        -> OverlayColor.Sky
    CONCRETE_LANES, SETT,
        -> OverlayColor.Cyan
    UNHEWN_COBBLESTONE, GRASS_PAVER,
        -> OverlayColor.Aquamarine
    COMPACTED, FINE_GRAVEL,
        -> OverlayColor.Teal
    DIRT, MUD, GROUND, WOODCHIPS,
        -> OverlayColor.Orange
    GRASS,
        -> OverlayColor.Lime // greenish colour for grass is deliberate
    SAND,
        -> OverlayColor.Gold // yellowish color for sand is deliberate
    GRAVEL, PEBBLES, ROCK,
        // very different from above but unlikely to be used in same places, i.e. below are usually on bridges
    WOOD, METAL,
        -> OverlayColor.Purple
    UNSUPPORTED, PAVED, UNPAVED,
        // not encountered in normal situations, get the same as generic surface
    CLAY, ARTIFICIAL_TURF, RUBBER, ACRYLIC,
        -> OverlayColor.Black
}

private fun surfaceTaggingNotExpected(element: Element) =
    isIndoor(element.tags) || isPrivateOnFoot(element) || isLink(element.tags)

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"

private fun isLink(tags: Map<String, String>): Boolean =
    tags["path"] == "link" ||
    tags["footway"] == "link" ||
    tags["cycleway"] == "link" ||
    tags["bridleway"] == "link"
