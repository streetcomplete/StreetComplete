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
import de.westnordost.streetcomplete.osm.surface.keysToBeRemovedOnSurfaceChange
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.sidewalk_surface.createSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

class PathSurfaceOverlay : Overlay {

    override val title = R.string.overlay_path_surface
    override val icon = R.drawable.ic_quest_way_surface
    override val changesetComment = "Specify path surfaces"
    override val wikiLink: String = "Key:surface"
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, BICYCLIST, OUTDOORS)
    override val hidesQuestTypes = setOf(AddPathSurface::class.simpleName!!)

    private val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + INVALID_SURFACES

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> =
        mapData.filter( """
               ways, relations with
                   highway ~ ${(ALL_PATHS).joinToString("|")}
                   and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
                   and (!cycleway:surface or cycleway:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!footway:surface or footway:surface ~ ${handledSurfaces.joinToString("|") })
                   and (segregated = yes or (!cycleway:surface and !footway:surface))
                   and (!surface:note or (surface or cycleway:surface or footway:surface or segregated = yes))
                   and (!cycleway:surface:note or cycleway:surface)
                   and (!footway:surface:note or footway:surface)
                   and !sidewalk and !sidewalk:left and !sidewalk:right and !sidewalk:both
                   and !sidewalk:both:surface and !sidewalk:right:surface and !sidewalk:left:surface and !sidewalk:surface
                   and !sidewalk:both:surface:note and !sidewalk:right:surface:note and !sidewalk:left:surface:note
               """).map { it to getStyleForStandalonePath(it) } +
       mapData.filter( """
               ways, relations with
                   highway ~ ${(ALL_ROADS).joinToString("|")}
                   and (sidewalk ~ left|right|both or sidewalk:both = yes or sidewalk:left = yes or sidewalk:right = yes)
                   and (!sidewalk:both:surface or sidewalk:both:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!sidewalk:right:surface or sidewalk:right:surface ~ ${handledSurfaces.joinToString("|") })
                   and (!sidewalk:left:surface or sidewalk:left:surface ~ ${handledSurfaces.joinToString("|") })
           """)
           .map { it to getStyleForSidewalkAsProperty(it) }

    override fun createForm(element: Element?) =
        if (element != null) {
            if (element.tags["highway"] in ALL_PATHS) PathSurfaceOverlayForm()
            else if (element.tags["highway"] in ALL_ROADS) SidewalkSurfaceOverlayForm()
            else null
        } else null
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
        is CyclewayFootwaySurfacesWithNote -> {
            if (surfaceStatus.cycleway in UNDERSPECIFED_SURFACES && surfaceStatus.cyclewayNote == null) {
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
        }
        is SingleSurface -> {
            dominatingSurface = surfaceStatus.surface
        }
        is CyclewayFootwaySurfaces -> {
            if (surfaceStatus.footway in UNDERSPECIFED_SURFACES) {
                dominatingSurface = surfaceStatus.footway
            } else {
                // cycleway is arbitrarily taken as dominating here
                // though for bicycles surface is a bit more important
                dominatingSurface = surfaceStatus.cycleway
            }
        }
        is SurfaceMissing, is SurfaceMissingWithNote -> {
            // no action needed
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in UNDERSPECIFED_SURFACES
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element))

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

    // sidewalk data is not set on road -> do not highlight as missing
    if (sidewalkSides == null) {
        return PolylineStyle(StrokeStyle(Color.INVISIBLE))
    }

    val sidewalkSurface = createSidewalkSurface(element.tags)
    val leftColor =
        if (sidewalkSides.left != Sidewalk.YES) Color.INVISIBLE
        else sidewalkSurface?.left.color
    val rightColor =
        if (sidewalkSides.right != Sidewalk.YES) Color.INVISIBLE
        else sidewalkSurface?.right.color

    if (leftColor == Color.DATA_REQUESTED || rightColor == Color.DATA_REQUESTED) {
        // yes, there is an edge case where one side has data set, one unset
        // and it will be not shown
        if (isPrivateOnFoot(element)) {
            return PolylineStyle(StrokeStyle(Color.INVISIBLE))
        }
    }

    return PolylineStyle(
        stroke = null,
        strokeLeft = StrokeStyle(leftColor),
        strokeRight = StrokeStyle(rightColor)
    )
}

private val SurfaceAndNote?.color: String get() =
    if (this?.value in UNDERSPECIFED_SURFACES && this?.note != null) Color.BLACK
    else this?.value.color

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
