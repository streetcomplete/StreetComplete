package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.surface.createMainSurfaceStatus
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

class RoadSurfaceOverlay : Overlay {

    override val title = R.string.overlay_road_surface
    override val icon = R.drawable.ic_quest_street_surface
    override val changesetComment = "Specify road surfaces"
    override val wikiLink: String = "Key:surface"
    override val achievements = listOf(CAR, BICYCLIST)
    override val hidesQuestTypes = setOf(AddRoadSurface::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        return mapData
            .filter("""
                ways, relations with
                  highway ~ ${(ALL_ROADS).joinToString("|")}
                  and (!surface:note or surface)
            """)
            .map { it to getStyle(it) }
    }

    override fun createForm(element: Element?) =
        if (element != null && element.tags["highway"] in ALL_ROADS) {
            RoadSurfaceOverlayForm()
        } else {
            null
        }
}

private fun getStyle(element: Element): Style {
    val surfaceStatus = createMainSurfaceStatus(element.tags)
    val color = surfaceStatus.getItsColor(element)
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color), null, null)
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
