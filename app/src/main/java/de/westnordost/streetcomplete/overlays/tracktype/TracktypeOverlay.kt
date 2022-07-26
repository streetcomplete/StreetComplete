package de.westnordost.streetcomplete.overlays.tracktype

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.osm.Tracktype
import de.westnordost.streetcomplete.osm.createTracktypeStatus

class TracktypeOverlay : Overlay {

    private val parentQuest = AddTracktype()
    override val title = R.string.overlay_tracktype
    override val icon = parentQuest.icon
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .filter("ways with tracktype=* or highway=track")
            .map { it to getStyle(it) }

    override fun createForm(element: Element) = TracktypeOverlayForm()
}

private fun getStyle(element: Element): Style {
    val grade = createTracktypeStatus(element.tags)
    // not set but private -> do not highlight as missing
    val isNotSetButThatsOkay = grade == null && isPrivateOnFoot(element)
    val color = if (isNotSetButThatsOkay) Color.INVISIBLE else grade.color
    val label = if ("tracktype" in element.tags) {
        element.tags["tracktype"]
    } else {
        null
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color, label) else PolylineStyle(color, null, null, label)
}

private val Tracktype?.color get() = when (this) {
    Tracktype.GRADE1 -> "#33ff00"
    Tracktype.GRADE2 -> "#ccff00"
    Tracktype.GRADE3 -> "#00eeff"
    Tracktype.GRADE4 -> "#f59709"
    Tracktype.GRADE5 -> "#dd1111"
    null ->             Color.UNSPECIFIED
}
