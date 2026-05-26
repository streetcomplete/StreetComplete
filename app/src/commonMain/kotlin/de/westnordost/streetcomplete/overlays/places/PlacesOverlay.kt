package de.westnordost.streetcomplete.overlays.places

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.edits.ElementEditAction
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.OverlayColor
import de.westnordost.streetcomplete.data.overlays.OverlayStyle
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.places.isDisusedPlace
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.view.presetIconIndex

class PlacesOverlay(private val getFeature: (Element) -> Feature?) : Overlay {

    override val title = Res.string.overlay_places
    override val icon = Res.drawable.quest_shop
    override val changesetComment = "Survey shops, places etc."
    override val wikiLink = "StreetComplete/Overlays#Places"
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val hidesQuestTypes = setOf(
        SpecifyShopType::class.simpleName!!,
        CheckShopType::class.simpleName!!
    )
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .asSequence()
            .filter { it.isPlaceOrDisusedPlace() }
            .map { element ->
                // show disused places always with the icon for "disused shop" icon
                val icon = getFeature(element)?.icon?.let { presetIconIndex[it] }
                    ?: (if (element.isDisusedPlace()) R.drawable.preset_fas_store_alt_slash else null)
                    ?: R.drawable.preset_maki_shop

                val label = getNameLabel(element.tags)

                val style = if (element is Node) {
                    OverlayStyle.Point(icon, label)
                } else {
                    OverlayStyle.Polygon(OverlayColor.Invisible, icon, label)
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
            .map { it to OverlayStyle.Point(icon = null, label = "◽") }

    @Composable
    override fun Form(
        onEdit: (ElementEditAction) -> Unit,
        element: Element?,
        geometry: ElementGeometry,
        countryInfo: CountryInfo
    ) {
        // this check is necessary because the form shall not be shown for entrances
        val isNewOrPlace = remember(element) { element == null || element.isPlaceOrDisusedPlace() }

        if (isNewOrPlace) {
            PlacesOverlayForm(onEdit, element, geometry, countryInfo)
        }
    }
}
