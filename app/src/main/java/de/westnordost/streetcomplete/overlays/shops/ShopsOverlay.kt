package de.westnordost.streetcomplete.overlays.shops

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PointStyle
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopType
import de.westnordost.streetcomplete.quests.shop_type.SpecifyShopType
import de.westnordost.streetcomplete.util.getNameLabel

class ShopsOverlay(private val featureDictionary: FeatureDictionary): Overlay {

    override val title = R.string.overlay_shops
    override val icon = R.drawable.ic_quest_shop
    override val changesetComment = "Add shops etc."
    override val wikiLink: String = "Key:shop"
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val hidesQuestTypes = setOf(
        AddPlaceName::class.simpleName!!,
        SpecifyShopType::class.simpleName!!,
        CheckShopType::class.simpleName!!
    )
    override val isCreateNodeEnabled = true

    override fun getStyledElements(mapData: MapDataWithGeometry) =
        mapData
            .filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)
            .map { element ->
                val feature = featureDictionary.byTags(element.tags).find().firstOrNull()

                val icon = feature?.icon ?: "ic_preset_make_shop" // TODO or dot
                val label = getNameLabel(element.tags)

                val style = if (element is Node) {
                    PointStyle(icon, name)
                } else {
                    PolygonStyle(Color.INVISIBLE, icon, label)
                }
                element to style
            }

    override fun createForm(element: Element?) = ShopsOverlayForm()
}
