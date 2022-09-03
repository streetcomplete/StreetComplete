package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
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
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddSidewalkSurface

class SidewalkSurfaceOverlay : Overlay {

    private val parentQuest = AddSidewalkSurface()
    override val title = R.string.overlay_sidewalk_surface
    override val icon = parentQuest.icon
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + Surface.surfaceReplacements.keys
        return mapData
           .filter( """ways, relations with
               highway ~ ${(ALL_ROADS).joinToString("|")}
               and (sidewalk=both or sidewalk=left or sidewalk=right or sidewalk:left=yes or sidewalk:right=yes)
               and (!sidewalk:both:surface or sidewalk:both:surface ~ ${handledSurfaces.joinToString("|") })
               and (!sidewalk:right:surface or sidewalk:right:surface ~ ${handledSurfaces.joinToString("|") })
               and (!sidewalk:left:surface or sidewalk:left:surface ~ ${handledSurfaces.joinToString("|") })
               """)
            // TODO exclude say sidewalk=gibberish sidewalk:left=yes ways?
           .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }.map { it to getSidewalkStyle(it) }
    }

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }
    // https://taginfo.openstreetmap.org/search?q=surface
    val supportedSurfaceKeys = listOf(
        // supported in this overlay, but not all
        "sidewalk:both:surface", "sidewalk:right:surface", "sidewalk:left:surface",
        // "sidewalk:surface" - not supported here

        // supported in all surface overlay
        "surface", "footway:surface", "cycleway:surface",
        "check_date:surface", "check_date:footway:surface", "check_date:cycleway:surface", // verify that it is supported TODO
        "source:surface", "source:footway:surface", "source:cycleway:surface", // verify that it is removed on change TODO
        "surface:colour", //  12K - remove on change? Ignore support? TODO
        "surface:note" // "note:surface" is not supported. TODO: actually support
    )

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    override fun createForm(element: Element) = SidewalkSurfaceOverlayForm()
}

private fun getSidewalkStyle(element: Element): PolylineStyle {
    val sidewalkSides = createSidewalkSides(element.tags)
    // not set but on road that usually has no sidewalk or it is private -> do not highlight as missing
    if (sidewalkSides == null || isPrivateOnFoot(element)) {
        return PolylineStyle(Color.INVISIBLE)
    }

    val leftSurfaceString = element.tags["sidewalk:both:surface"] ?: element.tags["sidewalk:left:surface"]
    val rightSurfaceString = element.tags["sidewalk:both:surface"] ?: element.tags["sidewalk:right:surface"]
    val leftSurfaceObject = Surface.values().find { it.osmValue == leftSurfaceString }
    val rightSurfaceObject = Surface.values().find { it.osmValue == rightSurfaceString }
    val leftColor = if (sidewalkSides.left != Sidewalk.YES) {
        Color.INVISIBLE
    } else {
        leftSurfaceObject.color
    }
    val rightColor = if (sidewalkSides.right != Sidewalk.YES) {
        Color.INVISIBLE
    } else {
        rightSurfaceObject.color
    }
    return PolylineStyle(
        color = null,
        colorLeft = leftColor,
        colorRight = rightColor
    )
}
