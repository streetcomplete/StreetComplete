package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddParkingFee(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<FeeAnswer>(o) {

    override val tagFilters = """
        nodes, ways, relations with amenity = parking
        and !fee
        and !fee:conditional
        and access ~ yes|customers|public
    """
    override val commitMessage = "Add whether there is a parking fee"
    override val icon = R.drawable.ic_quest_parking_fee

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_fee_title

    override fun createForm() = AddParkingFeeForm()

    override fun applyAnswerTo(answer: FeeAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is HasFee   -> changes.add("fee", "yes")
            is HasNoFee -> changes.add("fee", "no")
            is HasFeeAtHours -> {
                changes.add("fee", "no")
                changes.add("fee:conditional", "yes @ (${answer.hours.joinToString(";")})")
            }
            is HasFeeExceptAtHours -> {
                changes.add("fee", "yes")
                changes.add("fee:conditional", "no @ (${answer.hours.joinToString(";")})")
            }
        }
    }
}
