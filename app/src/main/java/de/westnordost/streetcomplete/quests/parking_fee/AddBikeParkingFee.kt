package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags

class AddBikeParkingFee : OsmFilterQuestType<FeeAndMaxStay>() {

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
    override val icon = R.drawable.ic_quest_bicycle_parking_fee
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_fee_title

    override fun createForm() = AddParkingFeeForm()

    override fun applyAnswerTo(answer: FeeAndMaxStay, tags: Tags, timestampEdited: Long) =
        answer.applyTo(tags)
}
