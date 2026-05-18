package de.westnordost.streetcomplete.quests.parking_fee

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddBikeParkingFee : OsmFilterQuestType<ParkingFeeAnswer>() {

    // element selection logic by @DerDings in #2507
    override val elementFilter = """
        nodes, ways, relations with amenity = bicycle_parking
        and access ~ yes|customers|public
        and (
            name
            or bicycle_parking ~ building|lockers|shed
            or capacity >= 100
        )
        and (
            !fee and !fee:conditional
            or fee older today -8 years
        )
    """
    override val changesetComment = "Specify bicycle parking fees"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.quest_bicycle_parking_fee
    override val title = Res.string.quest_bicycle_parking_fee_title
    override val achievements = listOf(BICYCLIST)

    @Composable
    override fun Form(onAnswer: (ParkingFeeAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddParkingFeeForm(onAnswer, countryInfo)
    }

    override fun applyAnswerTo(answer: ParkingFeeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.applyTo(tags)
    }
}
