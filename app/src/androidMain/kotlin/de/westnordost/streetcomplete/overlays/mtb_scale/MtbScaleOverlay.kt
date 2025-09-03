package de.westnordost.streetcomplete.overlays.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale
import de.westnordost.streetcomplete.osm.mtb_scale.parseMtbScale
import de.westnordost.streetcomplete.osm.surface.UNPAVED_SURFACES

class MtbScaleOverlay : Overlay, AndroidOverlay {

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

    private fun getStyle(element: Element): OverlayStyle {
        val mtbScale = parseMtbScale(element.tags)
        val color = mtbScale.color
            ?: if (isMtbTaggingExpected(element)) OverlayColor.Red else null
        return OverlayStyle.Polyline(
            stroke = color?.let { OverlayStyle.Stroke(it) },
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
    0 -> OverlayColor.Blue
    1 -> OverlayColor.Cyan
    2 -> OverlayColor.Lime
    3 -> OverlayColor.Gold
    4 -> OverlayColor.Orange
    5 -> OverlayColor.Purple
    6 -> OverlayColor.Black
    else -> null
}
