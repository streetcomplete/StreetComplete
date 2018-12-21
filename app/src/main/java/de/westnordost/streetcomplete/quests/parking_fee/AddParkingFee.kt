package de.westnordost.streetcomplete.quests.parking_fee

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddParkingFee(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes, ways, relations with
        amenity = parking and !fee and !fee:conditional and
        access ~ yes|customers|public
    """
    override val commitMessage = "Add whether there is a parking fee"
    override val icon = R.drawable.ic_quest_parking_fee

    override fun createForm() = AddParkingFeeForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val hasFee = answer.getBoolean(AddParkingFeeForm.FEE)
        changes.add("fee", hasFee.toYesNo())

        if (answer.containsKey(AddParkingFeeForm.FEE_CONDITONAL_HOURS)) {
            val hours = answer.getString(AddParkingFeeForm.FEE_CONDITONAL_HOURS)
            changes.add("fee:conditional", (!hasFee).toYesNo() + " @ (" + hours + ")")
        }
    }

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_fee_title

    private fun Boolean.toYesNo() = if(this) "yes" else "no"
}
