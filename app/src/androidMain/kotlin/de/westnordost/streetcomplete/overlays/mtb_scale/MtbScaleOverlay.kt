package de.westnordost.streetcomplete.overlays.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
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
              and mtb != no
              and (
                surface ~ ${UNPAVED_SURFACES.joinToString("|")}|wood
                or (highway = track and tracktype and tracktype != grade1)
              )
        """).map { it to getStyle(it) }

    override fun createForm(element: Element?) = MtbScaleOverlayForm()

    private fun getStyle(element: Element): Style {
        val mtbScale = parseMtbScale(element.tags)
        val color = mtbScale.color
            ?: if (isMtbTaggingExpected(element)) Color.DATA_REQUESTED else null
        return PolylineStyle(
            stroke = color?.let { StrokeStyle(it) },
            label = mtbScale?.value.toString()
        )
    }
}

private val mtbTaggingExpectedFilter by lazy { """
    ways with
      mtb ~ designated|yes
      or mtb:scale:uphill
      or mtb:scale:imba
""".toElementFilterExpression() }

private fun isMtbTaggingExpected(element: Element) =
    mtbTaggingExpectedFilter.matches(element)

private val MtbScale?.color get() = when (this?.value) {
    0 -> Color.BLUE
    1 -> Color.CYAN
    2 -> Color.LIME
    3 -> Color.GOLD
    4 -> Color.ORANGE
    5 -> Color.PURPLE
    6 -> Color.BLACK
    else -> null
}
