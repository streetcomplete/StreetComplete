package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface

class PathSurfaceOverlay : Overlay {

    private val parentQuest = AddPathSurface()
    override val title = R.string.overlay_path_surface
    override val icon = parentQuest.icon
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + Surface.surfaceReplacements.keys
        return mapData
           .filter( """ways, relations with
               (
                   highway ~ ${(ALL_PATHS).joinToString("|")}
                   and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
                   and (!cycleway:surface or cycleway:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!footway:surface or footway:surface ~ ${handledSurfaces.joinToString("|") })
                   and (segregated = yes or (!cycleway:surface and !footway:surface))
                   and !sidewalk:both:surface and !sidewalk:right:surface and !sidewalk:left:surface and !sidewalk:surface
               )
               or
               (
                   highway ~ ${(ALL_ROADS).joinToString("|")}
                   and (sidewalk=both or sidewalk=left or sidewalk=right or sidewalk:left=yes or sidewalk:right=yes)
                   and (!sidewalk:both:surface or sidewalk:both:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!sidewalk:right:surface or sidewalk:right:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!sidewalk:left:surface or sidewalk:left:surface ~ ${handledSurfaces.joinToString("|") })
               )
               """) // TODO: review all supported tags and skip some of them in either clause
           .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }.map { it to getStyle(it) }
    }

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }
    // https://taginfo.openstreetmap.org/search?q=surface
    val supportedSurfaceKeys = listOf(
        // note that sidewalk surface keys such as
        // are skipped for path query
        // some people tag combined footway-cycleway as cycleway with sidewalk...
        "sidewalk:both:surface", "sidewalk:right:surface", "sidewalk:left:surface", "sidewalk:surface",

        // supported here
        "footway:surface", "cycleway:surface",
        // really rare, but added by StreetComplete so also should be supported by it to allow editing added data
        "cycleway:surface:note", "footway:surface:note", // TODO: verify support

        "surface",
        "check_date:surface", "check_date:footway:surface", "check_date:cycleway:surface", // verify that it is supported TODO
        "source:surface", "source:footway:surface", "source:cycleway:surface", // verify that it is removed on change TODO
        "surface:colour", // verify that it is removed on change TODO
        "surface:note"  // TODO: verify support
    )

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    override fun createForm(element: Element?) =
        if (element != null) {
            if (element.tags["highway"] in ALL_PATHS) UniversalSurfaceOverlayForm()
            else if (element.tags["highway"] in ALL_ROADS) SidewalkSurfaceOverlayForm()
            else null
        } else null
}

private fun getStyle(element: Element): Style {
    if (element.tags["highway"] in ALL_PATHS) {
        return getStyleForStandalonePath(element)
    } else {
        return getStyleForSidewalkAsProperty(element)
    }
}
private fun getStyleForStandalonePath(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    val badSurfaces = listOf(null, PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA)
    var dominatingSurface: Surface? = null
    var noteProvided: String? = null
    var keyOfDominatingSurface: String? = null // TODO likely replace by translated value or skip it
    when (surfaceStatus) {
        is SingleSurfaceWithNote -> {
            dominatingSurface = surfaceStatus.surface
            noteProvided = surfaceStatus.note
            keyOfDominatingSurface = "surface"
        }
        is CyclewayFootwaySurfacesWithNote -> if (surfaceStatus.cycleway in badSurfaces && surfaceStatus.cyclewayNote == null) {
            // the worst case: so lets present it
            dominatingSurface = surfaceStatus.cycleway
            noteProvided = surfaceStatus.cyclewayNote
            keyOfDominatingSurface = "cycleway:surface"
        } else if (surfaceStatus.footway in badSurfaces) {
            // cycleway surface either has as bad data (also bad surface) or a bit better (bad surface with note)
            dominatingSurface = surfaceStatus.footway
            keyOfDominatingSurface = "footway:surface"
            noteProvided = surfaceStatus.footwayNote
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = surfaceStatus.cycleway
            keyOfDominatingSurface = "cycleway:surface"
        }
        is SingleSurface -> {
            dominatingSurface = surfaceStatus.surface
            keyOfDominatingSurface = "surface"
        }
        is CyclewayFootwaySurfaces -> if (surfaceStatus.footway in badSurfaces) {
            dominatingSurface = surfaceStatus.footway
            keyOfDominatingSurface = "footway:surface"
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = surfaceStatus.cycleway
            keyOfDominatingSurface = "cycleway:surface"
        }
        is SurfaceMissing -> {
            // no action needed
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in badSurfaces
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element)) || element.tags["leisure"] == "playground"

    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK // special styling needed for case where note is provided... - TODO test it in action
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color))

    // label for debugging
    // val label = element.tags[keyOfDominatingSurface]
    // return if (element.tags["area"] == "yes") PolygonStyle(color, label) else PolylineStyle(color, null, null, label)
}

private fun getStyleForSidewalkAsProperty(element: Element): PolylineStyle {
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

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
