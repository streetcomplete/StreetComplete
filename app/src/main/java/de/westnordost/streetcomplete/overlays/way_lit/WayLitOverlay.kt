package de.westnordost.streetcomplete.overlays.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.createLitStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit

class WayLitOverlay : Overlay {

    override val title = R.string.overlay_lit
    override val icon = R.drawable.ic_quest_lantern
    override val changesetComment = "Specify whether ways are lit"
    override val wikiLink: String = "Key:lit"
    override val achievements = listOf(PEDESTRIAN)
    override val hidesQuestTypes = setOf(AddWayLit::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .filter("ways, relations with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}")
            .map { it to getStyle(it) }

    override fun createForm(element: Element?) = WayLitOverlayForm()
}

private fun getStyle(element: Element): Style {
    val lit = createLitStatus(element.tags)
    // not set but indoor or private -> do not highlight as missing
    val isNotSetButThatsOkay = lit == null && (isIndoor(element.tags) || isPrivateOnFoot(element))
    val color = if (isNotSetButThatsOkay) Color.INVISIBLE else lit.color
    return if (element.tags["area"] == "yes") {
        PolygonStyle(color, null)
    } else {
        PolylineStyle(StrokeStyle(color))
    }
}

private val LitStatus?.color get() = when (this) {
    LitStatus.YES,
    LitStatus.UNSUPPORTED ->   Color.LIME
    LitStatus.NIGHT_AND_DAY -> Color.AQUAMARINE
    LitStatus.AUTOMATIC ->     Color.SKY
    LitStatus.NO ->            Color.BLACK
    null ->                    Color.DATA_REQUESTED
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
