package de.westnordost.streetcomplete.overlays.way_lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.lit.createLitStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolylineStyle

class WayLitOverlay : Overlay {

    override val title = R.string.overlay_lit
    override val icon = R.drawable.ic_quest_lantern
    override val changesetComment = "Add whether way is lit"
    override val wikiLink: String = "Key:lit"
    override val achievements = listOf(PEDESTRIAN)

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .filter("ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}")
            .map { it to PolylineStyle(createLitStatus(it.tags).color) }

    override fun createForm(element: Element) = WayLitOverlayForm()
}

private val LitStatus?.color: String get() = when (this) {
    LitStatus.YES,
    LitStatus.UNSUPPORTED ->   "#ccff00"
    LitStatus.NIGHT_AND_DAY -> "#33ff00"
    LitStatus.AUTOMATIC ->     "#00aaff"
    LitStatus.NO ->            "#555555"
    null ->                    Color.UNSPECIFIED
}

// TODO ideally, indoor ways should be invisible too
