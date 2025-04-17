package de.westnordost.streetcomplete.overlays.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.*

import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style

class MtbScaleOverlay : Overlay {

    override val title = R.string.overlay_mtb_scale
    override val icon = R.drawable.ic_quest_mtb
    override val changesetComment = "Specify mtb scale"
    override val wikiLink: String = "Key:mtb:scale"
    override val achievements: List<EditTypeAchievement> =
        listOf(BICYCLIST, OUTDOORS)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> =
        mapData.filter(
            """
        ways with
          highway ~ path|track
          and ( access !~ no|private or foot ~ yes|permissive|designated or bicycle ~ yes|permissive|designated)
          and (!lit or lit = no)
          and surface ~ "grass|sand|dirt|soil|fine_gravel|compacted|wood|gravel|pebblestone|rock|ground|earth|mud|woodchips|snow|ice|salt|stone"
        """
        ).map {
            it to getStyle(it)
        }

    override fun createForm(element: Element?): AbstractOverlayForm = MtbScaleOverlayForm()

    private fun getStyle(element: Element): Style {
        val color = MtbScale.entries.find { it.osmValue == element.tags["mtb:scale"]?.take(1) }.color

        return PolylineStyle(StrokeStyle(color))
    }

    private val MtbScale?.color
        get() = when (this) {
            null -> Color.DATA_REQUESTED
            MtbScale.ZERO -> "#DBECC0"
            MtbScale.ONE -> "#8CC63E"
            MtbScale.TWO -> "#00B2E6"
            MtbScale.THREE -> "#FECB1B"
            MtbScale.FOUR -> "#F47922"
            MtbScale.FIVE -> "#874D99"
            MtbScale.SIX -> "#000000"
        }
}
