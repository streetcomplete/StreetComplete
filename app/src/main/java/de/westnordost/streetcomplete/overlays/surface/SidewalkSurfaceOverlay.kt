package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlayForm
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

    override fun createForm(element: Element?) =
        if (element != null && element.tags["highway"] in ALL_ROADS) SidewalkSurfaceOverlayForm()
        else null
}

private fun getSidewalkStyle(element: Element): PolylineStyle {
    val sidewalkSides = createSidewalkSides(element.tags)
    // not set but on road that usually has no sidewalk or it is private -> do not highlight as missing
    if (sidewalkSides == null || isPrivateOnFoot(element)) {
        return PolylineStyle(StrokeStyle(Color.INVISIBLE))
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
        stroke = null,
        strokeLeft = StrokeStyle(leftColor),
        strokeRight = StrokeStyle(rightColor)
    )
}
