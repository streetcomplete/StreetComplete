package de.westnordost.streetcomplete.overlays.street_furniture

import androidx.core.content.ContentProviderCompat.requireContext
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.IS_DISUSED_STREET_FURNITURE_EXPRESSION
import de.westnordost.streetcomplete.osm.IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.util.getNameLabel

class StreetFurnitureOverlay(private val getFeature: (tags: Map<String, String>) -> Feature?) : Overlay {

    override val title = R.string.overlay_street_furniture
    override val icon = R.drawable.ic_quest_apple // TODO
    override val changesetComment = "Survey street furniture and similar objects"
    override val wikiLink: String = "Street furniture"
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val isCreateNodeEnabled = true

    override val sceneUpdates = listOf(
        "layers.buildings.draw.buildings-style.extrude" to "false",
        "layers.buildings.draw.buildings-outline-style.extrude" to "false"
    )

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .filter(IS_STREET_FURNITURE_INCLUDING_DISUSED_EXPRESSION)
            .mapNotNull { element ->
                val feature = getFeature(element.tags)
                val iconIdentifier = feature?.icon
                if ( iconIdentifier == null) {
                    if (IS_DISUSED_STREET_FURNITURE_EXPRESSION.matches(element)) {
                        val icon = "ic_preset_maki_marker_stroked"
                        val label = null
                        val style = if (element is Node) {
                            PointStyle(icon, label)
                        } else {
                            PolygonStyle(Color.INVISIBLE, icon, label)
                        }
                        element to style
                    } else {
                        null
                    }
                } else {
                    val icon = "ic_preset_" + iconIdentifier.replace('-', '_')
                    val label = getNameLabel(element.tags)

                    val style = if (element is Node) {
                        PointStyle(icon, label)
                    } else {
                        PolygonStyle(Color.INVISIBLE, icon, label)
                    }
                    element to style
                }
            }

    override fun createForm(element: Element?) = StreetFurnitureOverlayForm()
}
