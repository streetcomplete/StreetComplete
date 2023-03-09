package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.surface.createSurfaceAndNote
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

class RoadSurfaceOverlay : Overlay {

    override val title = R.string.overlay_road_surface
    override val icon = R.drawable.ic_quest_street_surface
    override val changesetComment = "Specify road surfaces"
    override val wikiLink: String = "Key:surface"
    override val achievements = listOf(CAR, BICYCLIST)
    override val hidesQuestTypes = setOf(AddRoadSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> =
        mapData.filter("""
            ways, relations with
                highway ~ ${(ALL_ROADS).joinToString("|")}
                or area != yes and (sidewalk ~ left|right|both or sidewalk:both = yes or sidewalk:left = yes or sidewalk:right = yes)
            """)
            .map { it to getStyle(it) }

    /* cycleways with any sidewalk tagged on the way (common tagging for road-like cycleways in
       Netherlands) are treated like roads here because in the PathSurfaceOverlay, one would instead
       specify the surface of the sidewalk only
     */

    override fun createForm(element: Element?) = RoadSurfaceOverlayForm()
}

private fun getStyle(element: Element): Style {
    val surface = createSurfaceAndNote(element.tags)
    var color = surface.getColor(element)
    if (color == Color.DATA_REQUESTED && isMotorway(element.tags)) {
        color = Color.INVISIBLE
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color))
}

private fun isMotorway(tags: Map<String, String>): Boolean =
    tags["highway"] == "motorway" || tags["highway"] == "motorway_link"
