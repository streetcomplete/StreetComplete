package de.westnordost.streetcomplete.quests.parking_fee

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddParkingFee : OsmFilterQuestType<ParkingFeeAnswer>() {

    override val elementFilter = """
        nodes, ways, relations with amenity = parking
        and access ~ yes|customers|public
        and (
            !fee and !fee:conditional
            or fee older today -8 years
        )
    """
    override val changesetComment = "Specify whether parking requires a fee"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = Res.drawable.quest_parking_fee
    override val title = Res.string.quest_parking_fee_title
    override val achievements = listOf(CAR)

    @Composable
    override fun Form(onAnswer: (QuestAnswer<ParkingFeeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddParkingFeeForm(onAnswer, countryInfo)
    }

    override fun applyAnswerTo(answer: ParkingFeeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
