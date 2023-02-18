package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.createSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.UNDERSPECIFED_SURFACES
import de.westnordost.streetcomplete.osm.surface.UnknownSurface
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddFootwayPartSurface
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddSidewalkSurface

class PathSurfaceOverlay : Overlay {

    override val title = R.string.overlay_path_surface
    override val icon = R.drawable.ic_quest_way_surface
    override val changesetComment = "Specify path surfaces"
    override val wikiLink: String = "Key:surface"
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, BICYCLIST, OUTDOORS)
    override val hidesQuestTypes = setOf(
        AddPathSurface::class.simpleName!!,
        AddFootwayPartSurface::class.simpleName!!,
        AddCyclewayPartSurface::class.simpleName!!,
        AddSidewalkSurface::class.simpleName!!,
    )

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> =
        mapData.filter( """
               ways, relations with
                   highway ~ ${(ALL_PATHS).joinToString("|")}
                   and (segregated = yes or (!cycleway:surface and !footway:surface and !cycleway:surface:note and !footway:surface:note))
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
           """)
           .map { it to getStyleForSidewalkAsProperty(it) }

    override fun createForm(element: Element?) =
        if (element != null) {
            if (element.tags["highway"] in ALL_PATHS) {
                PathSurfaceOverlayForm()
            } else if (element.tags["highway"] in ALL_ROADS) {
                SidewalkSurfaceOverlayForm()
            } else {
                null
            }
        } else {
            null
        }
}

private fun getStyleForStandalonePath(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    val dominatingSurface = if (element.tags["segregated"] == "yes") {
        // filters guarantee that without segregated=yes there is actually no split
        listOf(surfaceStatus.cycleway, surfaceStatus.footway).sortedBy {
            // take worst case for showing
            // prefer to take cycleway if both are tagged
            if (it.value in UNDERSPECIFED_SURFACES && it.note == null) {
                0
            } else if (it.value is UnknownSurface) {
                1
            } else if (it.value in UNDERSPECIFED_SURFACES) {
                2
            } else {
                3
            }
        }.first()
    } else {
        surfaceStatus.main
    }

    val color = dominatingSurface.getColor(element)
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
        if (sidewalkSides.left != Sidewalk.YES) {
            Color.INVISIBLE
        } else {
            sidewalkSurface?.left?.getColor(element) ?: Color.DATA_REQUESTED
        }
    val rightColor =
        if (sidewalkSides.right != Sidewalk.YES) {
            Color.INVISIBLE
        } else {
            sidewalkSurface?.right?.getColor(element) ?: Color.DATA_REQUESTED
        }

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
    if (this?.value in UNDERSPECIFED_SURFACES && this?.note != null) {
        Color.BLACK
    } else {
        this?.value.color
    }
