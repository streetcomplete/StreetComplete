package de.westnordost.streetcomplete.overlays.things

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.isDisusedThing
import de.westnordost.streetcomplete.osm.isThing
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.view.presetIconIndex

class ThingsOverlay(private val getFeature: (Element) -> Feature?) : Overlay {

    override val title = R.string.overlay_things
    override val icon = R.drawable.ic_quest_dot
    override val changesetComment = "Survey small map features"
    override val wikiLink = null
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .asSequence()
            .filter { it.isThing() || it.isDisusedThing() }
            .mapNotNull { element ->
                val feature = getFeature(element)
                    ?: element.asIfItWasnt("disused")?.let { getFeature(it) }
                    ?: return@mapNotNull null

                val icon = feature.icon?.let { presetIconIndex[it] } ?: R.drawable.ic_preset_maki_marker_stroked

                val style = if (element is Node) {
                    PointStyle(icon)
                } else {
                    PolygonStyle(Color.INVISIBLE, icon)
                }
                element to style
            }

    override fun createForm(element: Element?) = ThingsOverlayForm()
}
