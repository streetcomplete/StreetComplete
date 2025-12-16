package de.westnordost.streetcomplete.quests.parking_charge

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.fee.applyTo
import de.westnordost.streetcomplete.osm.maxstay.applyTo

class AddMotorcycleParkingCharge : OsmFilterQuestType<ParkingChargeAnswer>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways, relations with amenity = motorcycle_parking
        and access ~ yes|customers|public
        and (
            !fee and !fee:conditional
            or fee older today -8 years
        )
    """
    override val changesetComment = "Specify motorcycle parking fees"
    override val wikiLink = "Tag:amenity=motorcycle_parking"
    override val icon = R.drawable.quest_motorcycle_parking_fee
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_fee_title

    override fun createForm() = AddParkingChargeForm()

    override fun applyAnswerTo(answer: ParkingChargeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        answer.fee.applyTo(tags)
        answer.maxstay?.applyTo(tags)
    }
}
