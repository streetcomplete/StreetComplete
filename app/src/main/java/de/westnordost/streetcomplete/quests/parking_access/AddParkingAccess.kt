package de.westnordost.streetcomplete.quests.parking_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags

class AddParkingAccess : OsmFilterQuestType<ParkingAccess>() {

    // Exclude parking=street_side lacking any access tags, because most of
    // these are found alongside public access roads, and likely will be
    // access=yes by default. Leaving these in makes this quest repetitive and
    // leads to users adding lots of redundant access=yes tags to satisfy the
    // quest. parking=street_side with access=unknown seems like a valid target
    // though.
    //
    // Cf. #2408: Parking access might omit parking=street_side
    override val elementFilter = """
        nodes, ways, relations with amenity = parking
        and (
            access = unknown
            or (!access and parking !~ street_side|lane)
        )
    """
    override val changesetComment = "Specify parking access"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking_access
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_access_title2

    override fun createForm() = AddParkingAccessForm()

    override fun applyAnswerTo(answer: ParkingAccess, tags: Tags, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
