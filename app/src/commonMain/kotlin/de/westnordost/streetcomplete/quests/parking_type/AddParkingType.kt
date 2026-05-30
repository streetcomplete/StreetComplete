package de.westnordost.streetcomplete.quests.parking_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddParkingType : OsmFilterQuestType<ParkingType>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = parking
          and (!parking or parking = yes)
    """
    override val changesetComment = "Specify parking types"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = Res.drawable.quest_parking
    override val title = Res.string.quest_parkingType_title
    override val achievements = listOf(CAR)

    @Composable
    override fun Form(onAnswer: (QuestAnswer<ParkingType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = ParkingType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onAnswer = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: ParkingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["parking"] = answer.osmValue
    }
}
