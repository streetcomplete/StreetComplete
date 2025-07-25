package de.westnordost.streetcomplete.overlays.things

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.overlays.AndroidOverlay
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isThingOrDisusedThing
import de.westnordost.streetcomplete.view.presetIconIndex

class ThingsOverlay(private val getFeature: (Element) -> Feature?) : Overlay, AndroidOverlay {

    override val title = R.string.overlay_things
    override val icon = R.drawable.ic_quest_dot
    override val changesetComment = "Survey small map features"
    override val wikiLink = "StreetComplete/Overlays#Things"
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .asSequence()
            .filter { it.isThingOrDisusedThing() }
            .mapNotNull { element ->
                // show disused things with the same icon as normal things because they usually look
                // similar (a disused telephone booth still looks like a telephone booth, etc.)
                val feature = getFeature(element)
                    ?: element.asIfItWasnt("disused")?.let { getFeature(it) }
                    ?: return@mapNotNull null

                val icon = feature.icon?.let { presetIconIndex[it] } ?: R.drawable.preset_maki_marker_stroked

                val style = if (element is Node) {
                    OverlayStyle.Point(icon)
                } else {
                    OverlayStyle.Polygon(OverlayColor.Invisible, icon)
                }
                element to style
            }

    override fun createForm(element: Element?) = ThingsOverlayForm()
}
