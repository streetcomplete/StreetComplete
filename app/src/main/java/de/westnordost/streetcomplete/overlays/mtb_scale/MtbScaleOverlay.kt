package de.westnordost.streetcomplete.overlays.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style

class MtbScaleOverlay : Overlay {

    override val title = R.string.overlay_mtb_scale
    override val icon = R.drawable.ic_quest_mtb
    override val changesetComment = "Specify MTB difficulty"
    override val wikiLink: String = "Key:mtb:scale"
    override val achievements = listOf(BICYCLIST, OUTDOORS)
    override val defaultDisabledMessage = R.string.default_disabled_overlay_domain_expert

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData.filter("""
            ways with
              highway ~ path|track|bridleway
              and (
                access !~ no|private
                or foot ~ yes|permissive|designated
                or bicycle ~ yes|permissive|designated
              )
              and (
                surface ~ ${UNPAVED_SURFACES.joinToString("|")}|wood
                or (highway = track and tracktype and tracktype != grade1)
              )
        """).map { it to getStyle(it) }

    override fun createForm(element: Element?) = MtbScaleOverlayForm()

    private fun getStyle(element: Element): Style {
        val color = parseMtbScale(element.tags).color
            ?: if (isMtbTaggingExpected(element.tags)) Color.DATA_REQUESTED else null
        return PolylineStyle(stroke = color?.let { StrokeStyle(it) })
    }
}

private fun isMtbTaggingExpected(tags: Map<String, String>) =
    tags["mtb"] == "designated" || tags["mtb"] == "yes"

private val MtbScale?.color get() = when (this?.value) {
    0 -> Color.BLUE
    1 -> Color.SKY
    2 -> Color.CYAN
    3 -> Color.LIME
    4 -> Color.GOLD
    5 -> Color.ORANGE
    6 -> Color.BLACK
    else -> null
}
