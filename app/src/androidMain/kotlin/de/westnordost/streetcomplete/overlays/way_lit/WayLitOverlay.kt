package de.westnordost.streetcomplete.overlays.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.parseLitStatus
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit

class WayLitOverlay : Overlay, AndroidOverlay {

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

private fun getStyle(element: Element): OverlayStyle {
    val lit = parseLitStatus(element.tags)
    // not set but indoor or private -> do not highlight as missing
    val isNotSetButThatsOkay = lit == null && (isIndoor(element.tags) || isPrivateOnFoot(element))
    val color = if (isNotSetButThatsOkay) OverlayColor.Invisible else lit.color
    return if (element.tags["area"] == "yes") {
        OverlayStyle.Polygon(color, null)
    } else {
        OverlayStyle.Polyline(OverlayStyle.Stroke(color))
    }
}

private val LitStatus?.color get() = when (this) {
    LitStatus.YES  ->          OverlayColor.Lime
    LitStatus.UNSUPPORTED ->   OverlayColor.Purple
    LitStatus.NIGHT_AND_DAY -> OverlayColor.Aquamarine
    LitStatus.AUTOMATIC ->     OverlayColor.Sky
    LitStatus.NO ->            OverlayColor.Black
    null ->                    OverlayColor.Red
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
