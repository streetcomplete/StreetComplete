package de.westnordost.streetcomplete.quests.bike_rental_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalTypeAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBikeRentalType : OsmFilterQuestType<BikeRentalTypeAnswer>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bicycle_rental
          and access !~ private|no
          and (!bicycle_rental or bicycle_rental = yes)
          and !shop
    """
    override val changesetComment = "Specify bicycle rental types"
    override val wikiLink = "Key:bicycle_rental"
    override val icon = Res.drawable.quest_bicycle_rental
    override val title = Res.string.quest_bicycle_rental_type_title
    override val achievements = listOf(BICYCLIST)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bicycle_rental")

    @Composable
    override fun Form(onAnswer: (BikeRentalTypeAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = BikeRentalTypeAnswer.entries,
            itemsPerRow = 2,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: BikeRentalTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            DOCKING_STATION -> {
                tags["bicycle_rental"] = "docking_station"
            }
            DROPOFF_POINT -> {
                tags["bicycle_rental"] = "dropoff_point"
            }
            HUMAN -> {
                tags["bicycle_rental"] = "shop"
                tags["shop"] = "rental"
            }
            BIKE_SHOP_WITH_RENTAL -> {
                tags.remove("amenity")
                tags["shop"] = "bicycle"
                tags["service:bicycle:rental"] = "yes"
            }
        }
    }
}
