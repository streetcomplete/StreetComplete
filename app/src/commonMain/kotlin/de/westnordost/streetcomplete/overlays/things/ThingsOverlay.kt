package de.westnordost.streetcomplete.overlays.things

import androidx.compose.runtime.Composable
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayAction
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.iconDrawableResource
import de.westnordost.streetcomplete.osm.things.isThingOrDisusedThing
import de.westnordost.streetcomplete.resources.*

class ThingsOverlay(private val getFeature: (Element) -> Feature?) : Overlay {

    override val title = Res.string.overlay_things
    override val icon = Res.drawable.quest_dot
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

                val icon = feature.iconDrawableResource ?: Res.drawable.preset_maki_marker_stroked

                val style = if (element is Node) {
                    OverlayStyle.Point(icon)
                } else {
                    OverlayStyle.Polygon(OverlayColor.Invisible, icon)
                }
                element to style
            }

    @Composable
    override fun Form(on: (OverlayAction) -> Unit, element: Element?, geometry: ElementGeometry, countryInfo: CountryInfo) {
        element?.let { ThingsOverlayForm(on, element, geometry, countryInfo) }
    }
}
