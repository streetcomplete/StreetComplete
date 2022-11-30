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
import de.westnordost.streetcomplete.osm.surface.INVALID_SURFACES
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.SurfaceMissingWithNote
import de.westnordost.streetcomplete.osm.surface.UNDERSPECIFED_SURFACES
import de.westnordost.streetcomplete.osm.surface.associatedKeysToBeRemovedOnChange
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
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + INVALID_SURFACES
        return mapData
           .filter( """ways, relations with
               (
                   highway ~ ${(ALL_PATHS).joinToString("|")}
                   and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
                   and (!cycleway:surface or cycleway:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!footway:surface or footway:surface ~ ${handledSurfaces.joinToString("|") })
                   and (segregated = yes or (!cycleway:surface and !footway:surface))
                   and (!surface:note or (surface or cycleway:surface or footway:surface or segregated=yes))
                   and (!cycleway:surface:note or cycleway:surface)
                   and (!footway:surface:note or footway:surface)
                   and !sidewalk and !sidewalk:left and !sidewalk:right and !sidewalk:both
                   and !sidewalk:both:surface and !sidewalk:right:surface and !sidewalk:left:surface and !sidewalk:surface
                   and !sidewalk:both:surface:note and !sidewalk:right:surface:note and !sidewalk:left:surface:note
               )
               or
               (
                   highway ~ ${(ALL_ROADS).joinToString("|")}
                   and (sidewalk=both or sidewalk=left or sidewalk=right or sidewalk:left=yes or sidewalk:right=yes)
                   and (!sidewalk:both:surface or sidewalk:both:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!sidewalk:right:surface or sidewalk:right:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!sidewalk:left:surface or sidewalk:left:surface ~ ${handledSurfaces.joinToString("|") })
               )
               """)
           .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }.map { it to getStyle(it) }
    }

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }
    // https://taginfo.openstreetmap.org/search?q=surface
    val supportedSurfaceKeys = listOf(
        // note that elements with sidewalk surface keys are exluded from path in the query above
        // some people tag combined footway-cycleway as cycleway with sidewalk...
        "sidewalk:both:surface", "sidewalk:right:surface", "sidewalk:left:surface", "sidewalk:surface",
        "sidewalk:both:surface:note", "sidewalk:right:surface:note", "sidewalk:left:surface:note",

        // supported in this overlay, not in all of them
        "footway:surface", "cycleway:surface",
        // really rare, but added by StreetComplete so also should be supported by it to allow editing added data
        "cycleway:surface:note", "footway:surface:note",

        // supported in both surface overlays
        "surface", "surface:note"
    ) + associatedKeysToBeRemovedOnChange("") +
    associatedKeysToBeRemovedOnChange("cycleway:") + associatedKeysToBeRemovedOnChange("fotway:")

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    override fun createForm(element: Element?) =
        if (element != null) {
            if (element.tags["highway"] in ALL_PATHS) PathSurfaceOverlayForm()
            else if (element.tags["highway"] in ALL_ROADS) SidewalkSurfaceOverlayForm()
            else null
        } else null
}

private fun getStyle(element: Element): Style {
    return if (element.tags["highway"] in ALL_PATHS) {
        getStyleForStandalonePath(element)
    } else {
        getStyleForSidewalkAsProperty(element)
    }
}
private fun getStyleForStandalonePath(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    var dominatingSurface: Surface? = null
    var noteProvided: String? = null
    when (surfaceStatus) {
        is SingleSurfaceWithNote -> {
            dominatingSurface = surfaceStatus.surface
            noteProvided = surfaceStatus.note
        }
        is CyclewayFootwaySurfacesWithNote -> if (surfaceStatus.cycleway in UNDERSPECIFED_SURFACES && surfaceStatus.cyclewayNote == null) {
            // the worst case possible - bad surface without note: so lets present it
            dominatingSurface = surfaceStatus.cycleway
            noteProvided = surfaceStatus.cyclewayNote
        } else if (surfaceStatus.footway in UNDERSPECIFED_SURFACES) {
            // cycleway surface either has
            // data as bad as this one (also bad surface, without note)
            // or even worse (bad surface without note, while here maybe there is a note)
            dominatingSurface = surfaceStatus.footway
            noteProvided = surfaceStatus.footwayNote
        } else if (surfaceStatus.cycleway in UNDERSPECIFED_SURFACES) {
            // so footway has no bad surface, while cycleway has bad surface
            // lets take worse one
            dominatingSurface = surfaceStatus.cycleway
            noteProvided = surfaceStatus.cyclewayNote
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = surfaceStatus.cycleway
        }
        is SingleSurface -> {
            dominatingSurface = surfaceStatus.surface
        }
        is CyclewayFootwaySurfaces -> if (surfaceStatus.footway in UNDERSPECIFED_SURFACES) {
            dominatingSurface = surfaceStatus.footway
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = surfaceStatus.cycleway
        }
        is SurfaceMissing -> {
            // no action needed
        }
        is SurfaceMissingWithNote -> {
            // no action needed
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in UNDERSPECIFED_SURFACES
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element)) || element.tags["leisure"] == "playground"

    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color))
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
    val leftNote = if (element.tags["sidewalk:left:surface"] != null) { element.tags["sidewalk:left:surface"] } else { element.tags["sidewalk:both:surface"] }
    val rightNote = if (element.tags["sidewalk:right:surface"] != null) { element.tags["sidewalk:right:surface"] } else { element.tags["sidewalk:both:surface"] }
    val leftIsNotSet = leftSurfaceObject in UNDERSPECIFED_SURFACES
    val rightIsNotSet = rightSurfaceObject in UNDERSPECIFED_SURFACES
    val leftColor = if (sidewalkSides.left != Sidewalk.YES) {
        Color.INVISIBLE
    } else if (leftIsNotSet && leftNote != null) {
        Color.BLACK
    } else {
        leftSurfaceObject.color
    }
    val rightColor = if (sidewalkSides.right != Sidewalk.YES) {
        Color.INVISIBLE
    } else if (rightIsNotSet && rightNote != null) {
        Color.BLACK
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
