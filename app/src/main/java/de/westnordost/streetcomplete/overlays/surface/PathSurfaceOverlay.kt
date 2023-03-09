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
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.createSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.createSurfaceAndNote
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
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
                or area != yes and (sidewalk ~ left|right|both or sidewalk:both = yes or sidewalk:left = yes or sidewalk:right = yes)
            """).map { it to getStyle(it) }

    /* cycleways with any sidewalk tagged on the way are common tagging for road-like cycleways in
       Netherlands, when tapping on these, we want to be able to specify the sidewalk surface.
       The cycleway (=road) surface can be tagged in the RoadSurfaceOverlay
     */

    override fun createForm(element: Element?): AbstractOverlayForm? {
        val e = element ?: return null

        val sidewalk = createSidewalkSides(e.tags)
        val hasAnySidewalk = sidewalk?.left == Sidewalk.YES || sidewalk?.right == Sidewalk.YES
        val isArea = element.tags["area"] == "yes"

        return when {
            !isArea && hasAnySidewalk -> SidewalkSurfaceOverlayForm()
            else ->                      PathSurfaceOverlayForm()
        }
    }
}

private fun getStyle(element: Element): Style {
    val sidewalk = createSidewalkSides(element.tags)
    val hasAnySidewalk = sidewalk?.left == Sidewalk.YES || sidewalk?.right == Sidewalk.YES
    val isArea = element.tags["area"] == "yes"

    if (!isArea && hasAnySidewalk) {
        val sidewalkSurface = createSidewalkSurface(element.tags)

        val leftColor = getSidewalkColor(sidewalk?.left, sidewalkSurface?.left, element)
        val rightColor = getSidewalkColor(sidewalk?.right, sidewalkSurface?.right, element)

        return PolylineStyle(
            stroke = null,
            strokeLeft = StrokeStyle(leftColor),
            strokeRight = StrokeStyle(rightColor)
        )
    } else {
        val isSegregated = element.tags["segregated"] == "yes"
        val color = if (isSegregated) {
            val footwayColor = createSurfaceAndNote(element.tags, "footway").getColor(element)
            val cyclewayColor = createSurfaceAndNote(element.tags, "cycleway").getColor(element)
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
            createSurfaceAndNote(element.tags).getColor(element)
        }
        return if (isArea) PolygonStyle(color) else PolylineStyle(StrokeStyle(color))
    }
}

private fun getSidewalkColor(sidewalk: Sidewalk?, surface: SurfaceAndNote?, element: Element): String =
    if (sidewalk == Sidewalk.YES) surface.getColor(element) else Color.INVISIBLE
