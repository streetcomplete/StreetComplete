package de.westnordost.streetcomplete.overlays.places

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.util.getNameLabel

class PlacesOverlay(private val getFeature: (Element) -> Feature?) : Overlay {

    override val title = R.string.overlay_places
    override val icon = R.drawable.ic_quest_shop
    override val changesetComment = "Survey shops, places etc."
    override val wikiLink = null
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val hidesQuestTypes = setOf(
        AddPlaceName::class.simpleName!!,
        SpecifyShopType::class.simpleName!!,
        CheckShopType::class.simpleName!!
    )
    override val isCreateNodeEnabled = true

    override val sceneUpdates = listOf(
        "layers.buildings.draw.buildings-style.extrude" to "false",
        "layers.buildings.draw.buildings-outline-style.extrude" to "false"
    )

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .asSequence()
            .filter { it.isPlaceOrDisusedPlace() }
            .map { element ->
                val feature = getFeature(element)

                val icon = "ic_preset_" + (feature?.icon ?: "maki-shop").replace('-', '_')
                val label = getNameLabel(element.tags)

                val style = if (element is Node) {
                    PointStyle(icon, label)
                } else {
                    PolygonStyle(Color.INVISIBLE, icon, label)
                }
                element to style
            } +
        // additionally show entrances but no addresses as they are already shown on the background
        mapData
            .filter("""
                nodes with
                  entrance
                  and !(addr:housenumber or addr:housename or addr:conscriptionnumber or addr:streetnumber)
            """)
            .map { it to PointStyle(icon = null, label = "◽") }

    override fun createForm(element: Element?) =
        if (element == null || element.isPlaceOrDisusedPlace()) PlacesOverlayForm() else null
}
