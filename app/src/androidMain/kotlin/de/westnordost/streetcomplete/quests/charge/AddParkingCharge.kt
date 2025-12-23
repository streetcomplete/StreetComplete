package de.westnordost.streetcomplete.quests.charge

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

class AddParkingCharge : OsmFilterQuestType<ParkingChargeAnswer>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with amenity = parking
        and access ~ yes|customers|public
        and fee = yes
        and (
            !charge and !charge:conditional
            or charge older today -18 months
        )
    """

    override val changesetComment = "Add parking charges"
    override val wikiLink = "Key:charge"
    override val icon = R.drawable.quest_parking_charge
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_charge_title

    override fun createForm() = AddParkingChargeForm()

    override fun applyAnswerTo(answer: ParkingChargeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is SimpleCharge -> {
                // Format: "1.50 EUR/hour"
                tags["charge"] = "${answer.amount} ${answer.currency}/${answer.timeUnit}"
                tags.updateCheckDateForKey("charge")
            }
            is ItVaries -> {
                tags["charge:conditional"] = answer.conditional
                tags.updateCheckDateForKey("charge:conditional")
            }
        }
    }
}
