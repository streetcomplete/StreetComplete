package de.westnordost.streetcomplete.quests.bike_parking_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBikeParkingType : OsmFilterQuestType<BikeParkingType>() {

    override val elementFilter = """
        nodes, ways with
          amenity = bicycle_parking
          and access !~ private|no
          and !bicycle_parking
    """
    override val changesetComment = "Specify bicycle parking types"
    override val wikiLink = "Key:bicycle_parking"
    override val icon = R.drawable.quest_bicycle_parking
    override val title = Res.string.quest_bicycle_parking_type_title
    override val achievements = listOf(BICYCLIST)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with amenity = bicycle_parking")

    @Composable
    override fun Form(onAnswer: (BikeParkingType) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = BikeParkingType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
            favoriteKey = "AddBikeParkingTypeForm",
        )
    }

    override fun applyAnswerTo(answer: BikeParkingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["bicycle_parking"] = answer.osmValue
    }
}
