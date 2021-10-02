package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BICYCLIST

class AddBikeParkingFee : OsmFilterQuestType<FeeAnswer>() {

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
    override val commitMessage = "Add whether there is a bike parking fee"
    override val wikiLink = "Tag:amenity=bicycle_parking"
    override val icon = R.drawable.ic_quest_bicycle_parking_fee

    override val questTypeAchievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_fee_title

    override fun createForm() = AddParkingFeeForm()

    override fun applyAnswerTo(answer: FeeAnswer, changes: StringMapChangesBuilder) = answer.applyTo(changes)
}
